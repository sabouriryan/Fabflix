import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/public/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbRead");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String star_id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + star_id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (out; Connection conn = dataSource.getConnection()) {

            String queryStarInfo = "SELECT s.name, s.birthYear FROM stars AS s WHERE s.id = ?";

            PreparedStatement pstmtStar = conn.prepareStatement(queryStarInfo);
            pstmtStar.setString(1, star_id);
            ResultSet rs = pstmtStar.executeQuery();

            rs.next();
            String star_name = rs.getString("name");
            String star_dob = rs.getString("birthYear");

            pstmtStar.close();
            rs.close();

            String queryMovies = "SELECT m.id, m.title, m.year, m.director " +
            "FROM movies AS m " +
            "JOIN stars_in_movies sim ON m.id = sim.movieId " +
            "JOIN stars s ON s.id = sim.starId " +
            "WHERE s.id = ? " +
            "ORDER BY m.year DESC, m.title";
            

            PreparedStatement pstmtMovies = conn.prepareStatement(queryMovies);
            pstmtMovies.setString(1, star_id);
            ResultSet rsMovies = pstmtMovies.executeQuery();

            JsonObject output = new JsonObject();

            JsonArray star_movies = new JsonArray();
            // Iterate through movies the star is featured in
            while (rsMovies.next()) {
                String movie_id = rsMovies.getString("id");
                String movie_title = rsMovies.getString("title");
                String movie_year = rsMovies.getString("year");
                String movie_director = rsMovies.getString("director");

                JsonObject movieObject = new JsonObject();;
                movieObject.addProperty("movie_id", movie_id);
                movieObject.addProperty("movie_title", movie_title);
                movieObject.addProperty("movie_year", movie_year);
                movieObject.addProperty("movie_director", movie_director);

                star_movies.add(movieObject);
            }
            pstmtMovies.close();
            rsMovies.close();

            output.addProperty("star_name", star_name);
            output.addProperty("star_dob", star_dob == null ? "N/A" : star_dob);
            output.add("star_movies", star_movies);

            // Write JSON string to output
            out.write(output.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
