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
import java.util.Map;
import java.util.HashMap;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/public/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource; // Create a dataSource which registered in web.

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
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
        StringBuilder mapAsString = new StringBuilder("{");
        for (Map.Entry<String, Integer> entry : user.getShoppingCart().entrySet()) {
            mapAsString.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
        }
        mapAsString = new StringBuilder(mapAsString.substring(0, mapAsString.length() - 2) + "}");
        System.out.println(mapAsString);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        printRequestURL(request);

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            user = new User("");
        }

        String action = request.getParameter("action");
        String movieId = request.getParameter("movie-id");

        try (Connection conn = dataSource.getConnection()) {
            if ("add".equals(action)) {
                user.addItemToCart(movieId);
            } else if ("remove".equals(action)) {
                user.removeItemFromCart(movieId);
            } else if ("delete".equals(action)) {
                user.deleteItemFromCart(movieId);
            }

            printShoppingCart(user);

        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("errorMessage", e.getMessage());
            out.write(errorObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }
}
