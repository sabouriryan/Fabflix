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

@WebServlet(name = "SearchSuggestionServlet", urlPatterns = "/public/api/search-suggestion")
public class SearchSuggestionServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
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
        response.setContentType("application/json");
        String query = request.getParameter("query");

        PrintWriter out = response.getWriter();

        JsonArray output = new JsonArray();

        if (query == null) {
            out.write(output.toString());
            return;
        }

        try (out; Connection conn = dataSource.getConnection()) {
            String[] keywords = query.split("\\s+");
            StringBuilder booleanQuery = new StringBuilder();
            for (String keyword : keywords) {
                booleanQuery.append("+").append(keyword.toLowerCase()).append("* ");
            }
            String moviesQuery = "SELECT ft.* FROM moviedb.ft_movie_titles ft " +
                                "WHERE MATCH (ft.title) AGAINST (? IN BOOLEAN MODE) LIMIT 10";
            PreparedStatement pstmt = conn.prepareStatement(moviesQuery);
            pstmt.setString(1, booleanQuery.toString().trim());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                JsonObject movie = new JsonObject();
                JsonObject data = new JsonObject();
                movie.addProperty("value", rs.getString("title"));
                data.addProperty("movie_id", rs.getString("movieID"));
                movie.add("data", data);
                output.add(movie);
            }
            rs.close();
            pstmt.close();

            out.write(output.toString());
            response.setStatus(200);
        } catch (Exception e) {
            // Write error message JSON object to output
            e.printStackTrace();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }
    }
}
