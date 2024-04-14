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
import java.sql.*;


// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource; // Create a dataSource which registered in web.

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private void getResultMetaData(ResultSet rs) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            System.out.println("There are " + md.getColumnCount() + " columns");
            for (int i = 1; i <= md.getColumnCount(); i++) {
                System.out.println("Name of column " + i + " is " + md.getColumnName(i));
                System.out.println("Type of column " + i + " is " + md.getColumnTypeName(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            Statement statement = conn.createStatement();
            String topHundredQuery = "SELECT m.id, m.title, m.year, m.director, r.rating FROM moviedb.movies m " +
                                     "LEFT JOIN moviedb.ratings r ON m.id = r.movieId " +
                                     "ORDER BY r.rating DESC LIMIT 100";
            ResultSet rs = statement.executeQuery(topHundredQuery);

            getResultMetaData(rs);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");

                // Get top 3 genres
                String queryGenres = "SELECT g.name FROM genres_in_movies gim " +
                                     "JOIN genres g ON gim.genreId = g.id " +
                                     "WHERE gim.movieId = ?";

                PreparedStatement pstmtGenres = conn.prepareStatement(queryGenres);
                pstmtGenres.setString(1, movie_id);
                ResultSet rsGenres = pstmtGenres.executeQuery();

                JsonArray topGenresArray = new JsonArray();
                for (int genreCount = 0; rs.next() && genreCount < 3; ++genreCount) {
                    topGenresArray.add(rsGenres.getString("name"));
                }
                rsGenres.close();
                pstmtGenres.close();

                // Get top 3 stars
                String queryStars = "SELECT s.name FROM stars_in_movies sim " +
                                    "JOIN stars s ON sim.starId = s.id " +
                                    "WHERE sim.movieId = ?";

                PreparedStatement pstmtStars = conn.prepareStatement(queryStars);
                pstmtStars.setString(1, movie_id);
                ResultSet rsStars = pstmtStars.executeQuery();

                JsonArray topStarsArray = new JsonArray();
                for (int starCount = 0; rs.next() && starCount < 3; ++starCount) {
                    topStarsArray.add(rsStars.getString("name"));
                }
                rsStars.close();
                pstmtStars.close();

                String movie_title = rs.getString("title");
                int movie_year = rs.getInt("year");
                String movie_director = rs.getString("director");
                double movie_rating = rs.getDouble("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.add("top_genres", topGenresArray);
                jsonObject.add("top_stars", topStarsArray);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}