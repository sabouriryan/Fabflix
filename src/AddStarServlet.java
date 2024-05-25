import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.sql.ResultSet;
import java.sql.Statement;

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
@WebServlet(name = "AddStarServlet", urlPatterns = "/_dashboard/api/addStar")
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        JsonObject output = new JsonObject();

        String starName = request.getParameter("star-name");
        String birthYear = request.getParameter("birth-year");

        // Validate star name
        if (starName == null || starName.isEmpty()) {
            output.addProperty("status", "fail");
            output.addProperty("message", "Invalid star name.");
            out.write(output.toString());
            return;
        }

        // Perform database insertion
        try (out; Connection conn = dataSource.getConnection()) {
            String idNumQuery = "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1) FROM stars";

            Statement idStatement = conn.createStatement();
            ResultSet rs = idStatement.executeQuery(idNumQuery);
            rs.next();
            String starId = "nm" + rs.getInt(1);

            System.out.println("Star name is: " + starName + ", birth year is: " + birthYear + ", id is: " + starId);

            // Prepare SQL statement
            String sql = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            //statement.setString(1, generateStarId());
            statement.setString(1, starId);
            statement.setString(2, starName);
            if (birthYear != null) {
                statement.setObject(3, birthYear); // Set birthYear if not null
            } else {
                statement.setNull(3, java.sql.Types.INTEGER); // Set NULL if birthYear is null
            }

            // Execute the statement
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                output.addProperty("status", "success");
            } else {
                output.addProperty("status", "fail");
                output.addProperty("message", "Failed to add star.");
            }
            statement.close();
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
