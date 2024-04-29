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

    private void printRequestURL(HttpServletRequest request) {
        String initialUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString != null) {
            initialUrl += "?" + queryString;
        }
        System.out.println("Initial URL: " + initialUrl);
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

        printRequestURL(request);

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
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
    
        PreparedStatement preStmtSearch = conn.prepareStatement(getSearchQuery(title, year, director, starName));
    
        if (title != null) {
            preStmtSearch.setString(1, "%" + title + "%");
        }
        if (year != null) {
            preStmtSearch.setInt(2, Integer.parseInt(year));
        }
        if (director != null) {
            preStmtSearch.setString(3, "%" + director + "%");
        }
        if (starName != null) {
            preStmtSearch.setString(4, "%" + starName + "%");
        }
    
        JsonArray output = new JsonArray();
        try (ResultSet rs = preStmtSearch.executeQuery()) {
            while (rs.next()) {
                output.add(getMovieObject(rs, conn));
            }
        }
        preStmtSearch.close();
        out.write(output.toString());
    }
    
    // Modify this method to support search parameters
    private static String getSearchQuery(String title, String year, String director, String starName) {
        String query = "SELECT m.*, r.rating FROM moviedb.movies m " +
                "LEFT JOIN moviedb.ratings r ON m.id = r.movieId ";
    
        boolean whereAdded = false;
    
        if (title != null) {
            query += (whereAdded ? "AND " : "WHERE ") + "m.title LIKE ?";
            whereAdded = true;
        }
        if (year != null) {
            // Assuming 'year' is exact match
            query += (whereAdded ? "AND " : "WHERE ") + "m.year = ?";
            whereAdded = true;
        }
        if (director != null) {
            query += (whereAdded ? "AND " : "WHERE ") + "m.director LIKE ?";
            whereAdded = true;
        }
        if (starName != null) {
            query += "JOIN moviedb.stars_in_movies sim ON m.id = sim.movieId " +
                     "JOIN moviedb.stars s ON sim.starId = s.id " +
                     (whereAdded ? "AND " : "WHERE ") + "s.name LIKE ?";
        }
    
        query += " ORDER BY m.title";
    
        return query;
    }
    
    
    private void handleBrowseRequest(HttpServletRequest request, PrintWriter out, Connection conn) throws SQLException {
        String genre = request.getParameter("genre");
        String firstChar = request.getParameter("firstChar");

        PreparedStatement preStmtBrowse = conn.prepareStatement(getBrowseQuery(genre, firstChar));
        if (genre != null) {
            preStmtBrowse.setString(1, genre);
        } else if (firstChar != null && !firstChar.equals("*")) {
            preStmtBrowse.setString(1, firstChar + "%");
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
            query = "SELECT m.*, r.rating FROM moviedb.movies m " +
                    "JOIN moviedb.genres_in_movies gim ON m.id = gim.movieId " +
                    "JOIN moviedb.genres g ON gim.genreId = g.id " +
                    "LEFT JOIN moviedb.ratings r ON m.id = r.movieId " +
                    "WHERE g.name = ? " +
                    "ORDER BY m.title"; // Need to change to support pagination/sort method stated in other params
        } else if (firstChar != null && !firstChar.equals("*")) {
            query = "SELECT m.*, r.rating FROM moviedb.movies m " +
                    "LEFT JOIN moviedb.ratings r ON m.id = r.movieId " +
                    "WHERE m.title LIKE ? " +
                    "ORDER BY m.title";
        } else if (firstChar != null) {
            query = "SELECT m.*, r.rating FROM moviedb.movies m " +
                    "LEFT JOIN moviedb.ratings r ON m.id = r.movieId " +
                    "WHERE m.title REGEXP '^[^a-zA-Z0-9]' " +
                    "ORDER BY m.title";
        } else {
            query = "SELECT m.*, r.rating FROM moviedb.movies m " +
                    "LEFT JOIN moviedb.ratings r ON m.id = r.movieId " +
                    "ORDER BY m.title";
        }
        return query;
    }

    private JsonObject getMovieObject(ResultSet rs, Connection conn) throws SQLException {
        JsonObject movieObject = new JsonObject();
        String movie_id = rs.getString("id");
        movieObject.addProperty("movie_id", movie_id);
        movieObject.addProperty("movie_title", rs.getString("title"));
        movieObject.addProperty("movie_year", rs.getInt("year"));
        movieObject.addProperty("movie_director", rs.getString("director"));
        movieObject.add("movie_genres", getTopGenres(movie_id, conn));
        movieObject.add("movie_stars", getTopStars(movie_id, conn));
        movieObject.addProperty("movie_rating", rs.getDouble("rating"));
        return movieObject;
    }

    private JsonArray getTopGenres(String movieId, Connection conn) throws SQLException {
        String query = "SELECT genres.name FROM genres_in_movies " +
                "JOIN genres ON genres_in_movies.genreId = genres.id " +
                "WHERE genres_in_movies.movieId = ? " +
                "ORDER BY genres.name LIMIT 3";
        JsonArray topGenresArray = new JsonArray();
        try (PreparedStatement preStmtGenres = conn.prepareStatement(query)) {
            preStmtGenres.setString(1, movieId);
            try (ResultSet rs = preStmtGenres.executeQuery()) {
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
                "ORDER BY stars.name LIMIT 3";
        JsonArray topStarsArray = new JsonArray();
        try (PreparedStatement preStmtStars = conn.prepareStatement(query)) {
            preStmtStars.setString(1, movieId);
            try (ResultSet rs = preStmtStars.executeQuery()) {
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