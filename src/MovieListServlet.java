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
@WebServlet(name = "MovieListServlet", urlPatterns = "/public/api/movie-list")
public class MovieListServlet extends HttpServlet {
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
            System.out.println("There are " + md.getColumnCount() + " columns: ");
            for (int i = 1; i <= md.getColumnCount(); i++) {
                System.out.println("Name/Type of column " + i + " is " + md.getColumnName(i) + " " + md.getColumnTypeName(i));
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

        try (Connection conn = dataSource.getConnection()) {
            String action = request.getParameter("action");

            if (action != null) {
                // Handle search action
                if (action.equals("search")) {
                    handleSearchRequest(request, out, conn);
                }
                // Handle browse action
                else if (action.equals("browse")) {
                    handleBrowseRequest(request, out, conn);
                }
                // Invalid action
                else {
                    JsonObject errorObject = new JsonObject();
                    errorObject.addProperty("errorMessage", "Invalid action");
                    out.write(errorObject.toString());
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                JsonObject errorObject = new JsonObject();
                errorObject.addProperty("errorMessage", "Action parameter is required");
                out.write(errorObject.toString());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
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
    private void handleSearchRequest(HttpServletRequest request, PrintWriter out, Connection conn) throws SQLException {
        System.out.println("Searching for movies...");
    }

    private void handleBrowseRequest(HttpServletRequest request, PrintWriter out, Connection conn) throws SQLException {
        String genre = request.getParameter("genre");
        String startChar = request.getParameter("startChar");

        String query;
        if (genre != null) {
            query = "SELECT * FROM movies " +
                    "JOIN genres_in_movies ON movies.id = genres_in_movies.movieId " +
                    "JOIN genres ON genres_in_movies.genreId = genres.id " +
                    "WHERE genres.name = ? " +
                    "ORDER BY movies.title";
        } else if (startChar != null) {
            if (startChar.equals("*")) {
                query = "SELECT * FROM movies WHERE title REGEXP '^[^a-zA-Z0-9]' ORDER BY title";
            } else {
                query = "SELECT * FROM movies WHERE title LIKE ? ORDER BY title";
            }
        } else {
            System.out.println("No genre or starting character parameter found");
            return;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (genre != null) {
                pstmt.setString(1, genre);
            } else if (startChar != null && !startChar.equals("*")) {
                pstmt.setString(1, startChar);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                getRecordsGivenMovieIDs(rs, out, conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getRecordsGivenMovieIDs(ResultSet rs, PrintWriter out, Connection conn) throws SQLException {
        JsonArray output = new JsonArray();
        while (rs.next()) {
            String movieId = rs.getString("id");
            JsonArray topGenresArray = getTopGenres(movieId, conn);
            JsonArray topStarsArray = getTopStars(movieId, conn);

            // Construct movie object
            JsonObject movieObject = new JsonObject();
            movieObject.addProperty("movie_id", movieId);
            movieObject.addProperty("movie_title", rs.getString("title"));
            movieObject.addProperty("movie_year", rs.getInt("year"));
            movieObject.addProperty("movie_director", rs.getString("director"));
            movieObject.add("movie_genres", topGenresArray);
            movieObject.add("movie_stars", topStarsArray);
            movieObject.addProperty("movie_rating", rs.getDouble("rating"));

            output.add(movieObject);
        }
        out.write(output.toString());
    }

    private JsonArray getTopGenres(String movieId, Connection conn) throws SQLException {
        String query = "SELECT genres.name FROM genres_in_movies " +
                "JOIN genres ON genres_in_movies.genreId = genres.id " +
                "WHERE genres_in_movies.movieId = ? " +
                "LIMIT 3";
        JsonArray topGenresArray = new JsonArray();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, movieId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    topGenresArray.add(rs.getString("name"));
                }
            }
        }
        return topGenresArray;
    }

    private JsonArray getTopStars(String movieId, Connection conn) throws SQLException {
        String query = "SELECT stars.name, stars.id FROM stars_in_movies " +
                "JOIN stars ON stars_in_movies.starId = stars.id " +
                "WHERE stars_in_movies.movieId = ? " +
                "LIMIT 3";
        JsonArray topStarsArray = new JsonArray();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, movieId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    JsonObject starObject = new JsonObject();
                    starObject.addProperty("star_name", rs.getString("name"));
                    starObject.addProperty("star_id", rs.getString("id"));
                    topStarsArray.add(starObject);
                }
            }
        }
        return topStarsArray;
    }
}