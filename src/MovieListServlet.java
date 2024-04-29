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


// Declaring a WebServlet called MovieListServlet, which maps to url "public/api/movie-list"
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
            if (request.getParameter("title") != null || request.getParameter("year") != null
             || request.getParameter("director") != null || request.getParameter("starName") != null) {
                System.out.println("Server received search request");
                handleSearchRequest(request, out, conn);
            } else if (request.getParameter("genre") != null || request.getParameter("firstChar") != null) {
                System.out.println("Server received browse request");
                handleBrowseRequest(request, out, conn);
            } else {
                System.out.println("Servlet received request to movie-list, but no parameters");
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
        String firstChar = request.getParameter("firstChar");

        PreparedStatement preStmtBrowse = conn.prepareStatement(getBrowseQuery(genre, firstChar));
        if (genre != null) {
            preStmtBrowse.setString(1, genre);
        } else if (firstChar != null && !firstChar.equals("*")) {
            preStmtBrowse.setString(1, firstChar);
        }
        JsonArray output = new JsonArray();
        try (ResultSet rs = preStmtBrowse.executeQuery()) {
            while (rs.next()) {
                output.add(getMovieObject(rs, conn));
            }
        }
        preStmtBrowse.close();
        out.write(output.toString());

    }

    // Method will change to support pagination and record limit
    private static String getBrowseQuery(String genre, String firstChar) {
        String query;
        if (genre != null) {
            query = "SELECT movies.id FROM movies " +
                    "JOIN genres_in_movies ON movies.id = genres_in_movies.movieId " +
                    "JOIN genres ON genres_in_movies.genreId = genres.id " +
                    "WHERE genres.name = ? " +
                    "ORDER BY movies.title"; // Need to change to support pagination/sort method stated in other params
        } else if (firstChar != null && !firstChar.equals("*")) {
            query = "SELECT movies.id FROM movies WHERE title LIKE ? ORDER BY title";
        } else if (firstChar != null) {
            query = "SELECT movies.id FROM movies WHERE title REGEXP '^[^a-zA-Z0-9]' ORDER BY title";
        } else {
            query = "SELECT movies.id FROM movies ORDER BY title";
        }
        return query;
    }

    private JsonObject getMovieObject(ResultSet rs, Connection conn) throws SQLException {
        JsonObject movieObject = new JsonObject();
        String movie_id = rs.getString("id");
        movieObject.addProperty("movie_id", movie_id);
        movieObject.addProperty("movie_title", rs.getString("title"));
        movieObject.addProperty("movie_year", rs.getString("year"));
        movieObject.addProperty("movie_director", rs.getString("director"));
        movieObject.add("movie_genres", getTopGenres(movie_id, conn));
        movieObject.add("movie_stars", getTopStars(movie_id, conn));
        return movieObject;
    }

    private JsonArray getTopGenres(String movieId, Connection conn) throws SQLException {
        String query = "SELECT genres.name FROM genres_in_movies " +
                "JOIN genres ON genres_in_movies.genreId = genres.id " +
                "WHERE genres_in_movies.movieId = ? " +
                "LIMIT 3 ORDER BY genres.name";
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
                "LIMIT 3 ORDER BY stars.name";
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