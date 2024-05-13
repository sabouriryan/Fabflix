import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.CallableStatement;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


// change path later
@WebServlet(name = "AddMovieServlet", urlPatterns = "/_dashboard/api/addMovie")
public class AddMovieServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            InitialContext initContext = new InitialContext();
            dataSource = (DataSource) initContext.lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve movie information from the request
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String starName = request.getParameter("star-name");
        String genreName = request.getParameter("genre");

        PrintWriter out = response.getWriter();
        JsonObject output = new JsonObject();

        if (title == null || year == null || director == null || starName == null || genreName == null) {
            output.addProperty("status", "fail");
            output.addProperty("message", "Please fill in every field.");
            out.write(output.toString());
            return;
        }

        System.out.println("title: " + title + "year: " + year + "director: " + director + "star-mame: " + starName + "genre: " + genreName);

        // Call the stored procedure to add the movie
        try (Connection conn = dataSource.getConnection()) {

            String sql = "{CALL add_movie(?, ?, ?, ?, ?, ?)}";
            CallableStatement stmt = conn.prepareCall(sql);

            stmt.setString(1, title);
            stmt.setInt(2, Integer.parseInt(year));
            stmt.setString(3, director);
            stmt.setString(4, starName);
            stmt.setString(5, genreName);
            stmt.registerOutParameter(6, java.sql.Types.VARCHAR);

            // Execute the stored procedure
            stmt.execute();

            String status = stmt.getString(6);
            stmt.close();

            if (status.equals("fail")) {
                output.addProperty("status", "fail");
                output.addProperty("message", "Duplicate movie detected.");
            } else {
                output.addProperty("status", "success");
            }
            out.write(output.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            output.addProperty("status", "fail");
            output.addProperty("message", e.getMessage());
            out.write(output.toString());
        } finally {
             out.close();
        }
    }
}

