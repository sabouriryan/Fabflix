import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/public/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;
    private static final int MAX_PRICE = 100;
    private static final int MIN_PRICE = 10;


    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbWrite");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private void printRequestURL(HttpServletRequest request) {
        String initialUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString != null) {
            initialUrl += "?" + queryString;
        }
        System.out.println("Shopping URL: " + initialUrl);
    }

    private void printShoppingCart(User user) {
        StringBuilder mapAsString = new StringBuilder("[");
        for (Map.Entry<String, Integer> entry : user.getShoppingCart().entrySet()) {
            mapAsString.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
        }
        mapAsString = new StringBuilder(mapAsString.substring(0, mapAsString.length() - 2) + "]");
        System.out.println(mapAsString);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            user = new User("", 0);
            session.setAttribute("user",user);
        }

        try (out; Connection conn = dataSource.getConnection()) {
            String action = request.getParameter("action");
            String movieId = request.getParameter("movie-id");

            JsonObject output = new JsonObject();

            if (action != null && movieId != null) {
                updateMoviePrice(conn, movieId);
                switch (action) {
                    case "insert":
                        user.addItemToCart(movieId);
                        output.addProperty("status", "success");
                        break;
                    case "add":
                        user.addItemToCart(movieId); break;
                    case "remove":
                        user.removeItemFromCart(movieId); break;
                    case "delete":
                        user.deleteItemFromCart(movieId); break;
                }
            }

            JsonArray data = new JsonArray();
            for (Map.Entry<String, Integer> entry : user.getShoppingCart().entrySet()) {
                String cartMovieId = entry.getKey();
                int quantity = entry.getValue();
                String query = "SELECT m.title, mp.price " +
                               "FROM movies m " +
                               "JOIN movie_prices mp ON m.id = mp.movieId " +
                               "WHERE m.id = ?";

                PreparedStatement pstmtMovie = conn.prepareStatement(query);
                pstmtMovie.setString(1, cartMovieId);
                ResultSet rsMovie = pstmtMovie.executeQuery();

                JsonObject movieObject = new JsonObject();
                if (rsMovie.next()) {
                    movieObject.addProperty("movie_id", cartMovieId);
                    movieObject.addProperty("movie_title", rsMovie.getString("title"));
                    movieObject.addProperty("movie_quantity", quantity);
                    movieObject.addProperty("movie_price", rsMovie.getDouble("price"));
                    data.add(movieObject);
                }
                pstmtMovie.close();
                rsMovie.close();
            }
            output.add("data", data);

            if (!user.getShoppingCart().isEmpty()) printShoppingCart(user);
            out.write(output.toString());
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("status", "fail");
            errorObject.addProperty("errorMessage", e.getMessage());
            out.write(errorObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }

    private void updateMoviePrice(Connection conn, String movieId) throws SQLException {
        String queryCheck = "SELECT * FROM movie_prices WHERE movieId = ?";
        PreparedStatement pstmtCheck = conn.prepareStatement(queryCheck);
        pstmtCheck.setString(1, movieId);
        ResultSet rsCheck = pstmtCheck.executeQuery();

        if (!rsCheck.next()) {
            System.out.println("Giving " + movieId + " a price");
            String insertQuery = "INSERT INTO movie_prices (movieId, price) VALUES (?, ?)";
            PreparedStatement pstmtInsert = conn.prepareStatement(insertQuery);
            pstmtInsert.setString(1, movieId);
            pstmtInsert.setInt(2, generatePrice(MIN_PRICE, MAX_PRICE));
            pstmtInsert.executeUpdate();
        }
    }

    public static int generatePrice(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }
}
