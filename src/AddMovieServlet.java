import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.CallableStatement;
import java.sql.SQLException;

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
@WebServlet("/AddMovieServlet")


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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve movie information from the request
        String title = request.getParameter("title");
        int year = Integer.parseInt(request.getParameter("year"));
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
        String genreName = request.getParameter("genreName");

        // Call the stored procedure to add the movie
        try (Connection conn = dataSource.getConnection()) {
            // Prepare the SQL call to the stored procedure
            String sql = "{CALL add_movie(?, ?, ?, ?, ?)}";
            CallableStatement stmt = conn.prepareCall(sql);
            // Set the parameters for the stored procedure
            stmt.setString(1, title);
            stmt.setInt(2, year);
            stmt.setString(3, director);
            stmt.setString(4, starName);
            stmt.setString(5, genreName);
            // Execute the stored procedure
            stmt.execute();
            // Close resources
            stmt.close();
            // Inform the user that the movie was added successfully
            response.getWriter().println("Movie added successfully!");
        } catch (SQLException e) {
            // Handle any database errors
            throw new ServletException("Error adding movie to database", e);
        }
    }

    // Generate a unique movie ID (e.g., using UUID)
    private String generateMovieId() {
        // Implement your own logic to generate a unique ID
        return java.util.UUID.randomUUID().toString();
    }
}

