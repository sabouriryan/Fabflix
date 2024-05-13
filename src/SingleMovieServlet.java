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

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/public/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
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
        String movie_id = request.getParameter("id");
        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + movie_id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            String movie_title = "";
            int movie_year = 0;
            String movie_director = "";
            double movie_rating = 0;

            // Get movie info
            String movieInfoQuery = "SELECT m.title, m.year, m.director FROM movies m WHERE m.id = ?";
            PreparedStatement pstmtMovie = conn.prepareStatement(movieInfoQuery);
            pstmtMovie.setString(1, movie_id);
            ResultSet rs = pstmtMovie.executeQuery();

            if (rs.next()) {
                movie_title = rs.getString("title");
                movie_year = rs.getInt("year");
                movie_director = rs.getString("director");
            }
            rs.close();
            pstmtMovie.close();

            // Get movie rating
            String movieRatingQuery = "SELECT r.rating FROM ratings r WHERE r.movieId = ?";
            PreparedStatement pstmtMovieRating = conn.prepareStatement(movieRatingQuery);
            pstmtMovieRating.setString(1, movie_id);
            ResultSet rsRating = pstmtMovieRating.executeQuery();

            if (rsRating.next()) {
                movie_rating = rsRating.getDouble("rating");
            }
            rsRating.close();
            pstmtMovieRating.close();

            // Get all genres for this movie
            String queryGenres = "SELECT g.name FROM genres_in_movies gim " +
                                 "JOIN genres g ON gim.genreId = g.id " +
                                 "WHERE gim.movieId = ?";
            PreparedStatement pstmtGenres = conn.prepareStatement(queryGenres);
            pstmtGenres.setString(1, movie_id);
            ResultSet rsGenres = pstmtGenres.executeQuery();

            JsonArray genresArray = new JsonArray();
            while (rsGenres.next()) {
                genresArray.add(rsGenres.getString("name"));
            }
            rsGenres.close();
            pstmtGenres.close();

            // Get ALL stars sorted by decreasing number of movies played
            String queryStars = "SELECT s.id AS star_id, s.name AS star_name, COUNT(sim_all.starId) AS movie_count " +
                                "FROM stars_in_movies AS sim_movie " +
                                "JOIN stars AS s ON sim_movie.starId = s.id " +
                                "JOIN stars_in_movies AS sim_all ON sim_movie.starId = sim_all.starId " +
                                "WHERE sim_movie.movieId = ? " +
                                "GROUP BY sim_movie.starId, s.name " +
                                "ORDER BY movie_count DESC, s.name";

            PreparedStatement pstmtStars = conn.prepareStatement(queryStars);
            pstmtStars.setString(1, movie_id);
            ResultSet rsStars = pstmtStars.executeQuery();

            JsonArray starsArray = new JsonArray();
            while (rsStars.next()) {
                JsonObject starObject = new JsonObject();
                starObject.addProperty("star_name", rsStars.getString("star_name"));
                starObject.addProperty("star_id", rsStars.getString("star_id"));
                starsArray.add(starObject);
            }
            rsStars.close();
            pstmtStars.close();

            JsonObject output = new JsonObject();
            output.addProperty("movie_title", movie_title);
            output.addProperty("movie_year", movie_year);
            output.addProperty("movie_director", movie_director);
            output.add("movie_genres", genresArray);
            output.add("movie_stars", starsArray);
            output.addProperty("movie_rating", movie_rating);

            // Write JSON string to output
            out.write(output.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
            // Write error message JSON object to output
            e.printStackTrace();
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
