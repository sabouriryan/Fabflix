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
import java.sql.*;
import java.util.*;


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

        //printRequestURL(request);

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(true);
        session.setAttribute("queryString", request.getQueryString());

        try (out; Connection conn = dataSource.getConnection()) {
            int page = Integer.parseInt(request.getParameter("page"));
            int pageLimit = Integer.parseInt(request.getParameter("pageLimit"));
            int sortMethod = Integer.parseInt(request.getParameter("sort"));

            if (request.getParameter("title") != null || request.getParameter("year") != null
                || request.getParameter("director") != null || request.getParameter("starName") != null) {
                System.out.println("Server received search request for " + request.getQueryString());
                handleSearchRequest(request, out, conn, page, pageLimit, sortMethod);
            } else if (request.getParameter("genre") != null || request.getParameter("firstChar") != null) {
                System.out.println("Server received browse request for " +request.getQueryString());
                handleBrowseRequest(request, out, conn, page, pageLimit, sortMethod);
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

    private static String getSortMethod(int sortMethod) {
        switch (sortMethod) {
            case 1:
                return "ORDER BY m.title ASC, r.rating ASC ";
            case 2:
                return "ORDER BY m.title ASC, r.rating DESC ";
            case 3:
                return "ORDER BY m.title DESC, r.rating ASC ";
            case 4:
                return "ORDER BY m.title DESC, r.rating DESC ";
            case 5:
                return "ORDER BY r.rating ASC, m.title ASC ";
            case 6:
                return "ORDER BY r.rating ASC, m.title DESC ";
            case 7:
                return "ORDER BY r.rating DESC, m.title ASC ";
            case 8:
                return "ORDER BY r.rating DESC, m.title DESC ";
            default:
                return "";
        }
    }

    private void handleSearchRequest(HttpServletRequest request, PrintWriter out, Connection conn, int page, int pageLimit, int sortMethod) throws SQLException {
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");

        String searchQuery = getSearchQuery(title, year, director, starName);
        searchQuery += getSortMethod(sortMethod);
        searchQuery += "LIMIT ? OFFSET ?";

        //System.out.println("Search query: " + searchQuery);
        PreparedStatement preStmtSearch = conn.prepareStatement(searchQuery);
        int parameterIndex = 1;

        if (title != null) {
            preStmtSearch.setString(parameterIndex++, "%" + title + "%");
        }
        if (year != null) {
            preStmtSearch.setInt(parameterIndex++, Integer.parseInt(year));
        }
        if (director != null) {
            preStmtSearch.setString(parameterIndex++, "%" + director + "%");
        }
        if (starName != null) {
            preStmtSearch.setString(parameterIndex++, "%" + starName + "%");
        }

        int offset = (page - 1) * pageLimit;
        preStmtSearch.setInt(parameterIndex++, pageLimit);
        preStmtSearch.setInt(parameterIndex, offset);
    
        JsonArray output = new JsonArray();
        try (ResultSet rs = preStmtSearch.executeQuery()) {
            while (rs.next()) {
                output.add(getMovieObject(rs, conn));
            }
        }
        preStmtSearch.close();
        out.write(output.toString());
    }
    
    private static String getSearchQuery(String title, String year, String director, String starName) {
        String query = "SELECT m.*, r.rating FROM moviedb.movies m " +
                "LEFT JOIN moviedb.ratings r ON m.id = r.movieId ";
    
        boolean whereAdded = false;
    
        if (title != null) {
            query += "WHERE m.title LIKE ? ";
            whereAdded = true;
        }
        if (year != null) {
            // Assuming 'year' is exact match
            query += (whereAdded ? " AND " : "WHERE ") + "m.year = ? ";
            whereAdded = true;
        }
        if (director != null) {
            query += (whereAdded ? " AND " : "WHERE ") + "m.director LIKE ? ";
            whereAdded = true;
        }
        if (starName != null) {
            query += (whereAdded ? " AND " : "WHERE ");
            query += "m.id IN (SELECT sim.movieId FROM moviedb.stars_in_movies sim " +
            " INNER JOIN moviedb.stars s ON sim.starId = s.id " +
            " WHERE LOWER(s.name) LIKE LOWER(?))";
        }

        return query;
    }
    
    private void handleBrowseRequest(HttpServletRequest request, PrintWriter out, Connection conn, int page, int pageLimit, int sortMethod) throws SQLException {
        String genre = request.getParameter("genre");
        String firstChar = request.getParameter("firstChar");

        String browseQuery = getBrowseQuery(genre, firstChar);
        browseQuery += getSortMethod(sortMethod);
        browseQuery += "LIMIT ? OFFSET ?";
        PreparedStatement preStmtBrowse = conn.prepareStatement(browseQuery);

        int parameterIndex = 1;
        if (genre != null) {
            preStmtBrowse.setString(parameterIndex++, genre);
        } else if (firstChar != null && !firstChar.equals("*")) {
            preStmtBrowse.setString(parameterIndex++, firstChar + "%");
        }

        int offset = (page - 1) * pageLimit;
        preStmtBrowse.setInt(parameterIndex++, pageLimit);
        preStmtBrowse.setInt(parameterIndex, offset);

        JsonArray output = new JsonArray();
        //System.out.println("Prepared SQL Statement: " + preStmtBrowse.toString());

        try (ResultSet rs = preStmtBrowse.executeQuery()) {
            while (rs.next()) {
                output.add(getMovieObject(rs, conn));
            }
        }
        preStmtBrowse.close();
        out.write(output.toString());
    }

    private static String getBrowseQuery(String genre, String firstChar) {
        String query;
        if (genre != null) {
            query = "SELECT m.*, r.rating FROM moviedb.movies m " +
                    "JOIN moviedb.genres_in_movies gim ON m.id = gim.movieId " +
                    "JOIN moviedb.genres g ON gim.genreId = g.id " +
                    "LEFT JOIN moviedb.ratings r ON m.id = r.movieId " +
                    "WHERE g.name = ? ";
        } else if (firstChar != null && !firstChar.equals("*")) {
            query = "SELECT m.*, r.rating FROM moviedb.movies m " +
                    "LEFT JOIN moviedb.ratings r ON m.id = r.movieId " +
                    "WHERE m.title LIKE ? ";
        } else if (firstChar != null) {
            query = "SELECT m.*, r.rating FROM moviedb.movies m " +
                    "LEFT JOIN moviedb.ratings r ON m.id = r.movieId " +
                    "WHERE m.title REGEXP '^[^a-zA-Z0-9]' ";
        } else {
            query = "SELECT m.*, r.rating FROM moviedb.movies m " +
                    "LEFT JOIN moviedb.ratings r ON m.id = r.movieId ";
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
        movieObject.addProperty("movie_rating", getMovieRating(movie_id, conn));
        return movieObject;
    }

    private double getMovieRating(String movie_id, Connection conn) throws SQLException {
        // Get movie rating
        double movie_rating = 0;
        String movieRatingQuery = "SELECT r.rating FROM ratings r WHERE r.movieId = ?";
        PreparedStatement pstmtMovieRating = conn.prepareStatement(movieRatingQuery);
        pstmtMovieRating.setString(1, movie_id);
        ResultSet rsRating = pstmtMovieRating.executeQuery();

        if (rsRating.next()) {
            movie_rating = rsRating.getDouble("rating");
        }
        rsRating.close();
        pstmtMovieRating.close();
        return movie_rating;
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
        String query = "SELECT s.id AS star_id, s.name AS star_name, COUNT(sim_all.starId) AS movie_count " +
               "FROM stars_in_movies AS sim_movie " +
               "JOIN stars AS s ON sim_movie.starId = s.id " +
               "JOIN stars_in_movies AS sim_all ON sim_movie.starId = sim_all.starId " +
               "WHERE sim_movie.movieId = ? " +
               "GROUP BY sim_movie.starId, s.name " +
               "ORDER BY movie_count DESC, s.name LIMIT 3";

        JsonArray topStarsArray = new JsonArray();
    
        try (PreparedStatement preStmtStars = conn.prepareStatement(query)) {
            preStmtStars.setString(1, movieId);
    
            try (ResultSet rs = preStmtStars.executeQuery()) {
                while (rs.next()) {
                    String starId = rs.getString("star_id");
                    String starName = rs.getString("star_name");
    
                    JsonObject starObject = new JsonObject();
                    starObject.addProperty("star_name", starName);
                    starObject.addProperty("star_id", starId);
                    topStarsArray.add(starObject);
                }
            }
        }
    
        return topStarsArray;
    }
}