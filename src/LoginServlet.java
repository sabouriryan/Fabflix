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

@WebServlet(name = "LoginServlet", urlPatterns = "/public/api/login")
public class LoginServlet extends HttpServlet {
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

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            String loginQuery = "SELECT * FROM customers WHERE email = ? AND password = ?";

            PreparedStatement pstmtLogin = conn.prepareStatement(loginQuery);
            pstmtLogin.setString(1, username);
            pstmtLogin.setString(2, password);
            ResultSet rs = pstmtLogin.executeQuery();

            /* This example only allows username/password to be test/test
            /  in the real project, you should talk to the database to verify username/password
            */
            JsonObject output = new JsonObject();
            if (rs.next()) {
                request.getSession().setAttribute("user", new User(username));
                output.addProperty("status", "success");
            } else {
                output.addProperty("status", "fail");
                output.addProperty("message", "Invalid email or password");
            }

            rs.close();
            pstmtLogin.close();
            out.print(output.toString());

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

