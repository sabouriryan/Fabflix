import com.google.gson.Gson;
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

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "DashboardLoginServlet", urlPatterns = "/_dashboard/api/login")
public class DashboardLoginServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            InitialContext initContext = new InitialContext();
            dataSource = (DataSource) initContext.lookup("java:comp/env/jdbc/moviedbRead");
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

        try (out; Connection conn = dataSource.getConnection()) {
            // employees table for the dashboard instead of customers
            String loginQuery = "SELECT * FROM employees WHERE email = ?";

            PreparedStatement pstmtLogin = conn.prepareStatement(loginQuery);
            pstmtLogin.setString(1, username);
            ResultSet rs = pstmtLogin.executeQuery();

            JsonObject output = new JsonObject();
            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);

                if (success) {
                    System.out.println("Successful Employee login");
                    request.getSession().setAttribute("employee", new Employee(username));
                    output.addProperty("status", "success");
                } else {
                    System.out.println("Unsuccessful Employee login");
                    output.addProperty("status", "fail");
                    output.addProperty("message", "Invalid email or password");
                }
            } else {
                System.out.println("Employee email not found");
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

            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}

