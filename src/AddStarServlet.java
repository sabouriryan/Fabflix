import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.sql.ResultSet;
import java.sql.Statement;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

//import com.mysql.cj.xdevapi.Statement;


// change path later
@WebServlet("/addStar")
public class AddStarServlet extends HttpServlet {
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
        String starName = request.getParameter("starName");
        String birthYearStr = request.getParameter("birthYear");
        
        // Validate star name
        if (starName == null || starName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Star name is required.");
            return;
        }
        
        // Parse birth year
        Integer birthYear = null;
        if (birthYearStr != null && !birthYearStr.isEmpty()) {
            try {
                birthYear = Integer.parseInt(birthYearStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Invalid birth year format.");
                return;
            }
        }

        // Perform database insertion
        try (Connection conn = dataSource.getConnection()){
            String idQuery = "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1) FROM stars";

            // Execute the ID query
            int starId = 1; // Default value if no stars exist yet
            try (Statement idStatement = conn.createStatement();
                ResultSet resultSet = idStatement.executeQuery(idQuery)) {
                if (resultSet.next()) {
                   starId = resultSet.getInt(1);
                }
            
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Database error: " + e.getMessage());
                return;
            }
            

            // Prepare SQL statement
            String sql = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            //statement.setString(1, generateStarId());
            statement.setString(1, "nm" + starId);
            statement.setString(2, starName);
            statement.setObject(3, birthYear, java.sql.Types.INTEGER);

            // Execute the statement
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("Star added successfully.");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Failed to add star.");
            }

            // Close resources
            statement.close();
            conn.close();
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    // Generate a unique star ID (e.g., using UUID)
    private String generateStarId() {
        // Implement your own logic to generate a unique ID
        return java.util.UUID.randomUUID().toString();
    }
}
