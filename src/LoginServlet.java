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

@WebServlet(name = "LoginServlet", urlPatterns = "/public/api/login")
public class LoginServlet extends HttpServlet {
    private DataSource dataSource;
    private static final String SECRET_KEY = "6LcuU9UpAAAAAGUbPlxd5EoEPytuJGNQRTOnA3MT";
    private static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";


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
        String recaptchaResponse = request.getParameter("g-recaptcha-response"); // Get reCAPTCHA response

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            verifyRecaptcha(recaptchaResponse);

            String loginQuery = "SELECT * FROM customers WHERE email = ?";

            PreparedStatement pstmtLogin = conn.prepareStatement(loginQuery);
            pstmtLogin.setString(1, username);
            ResultSet rs = pstmtLogin.executeQuery();

            JsonObject output = new JsonObject();
            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);

                if (success) {
                    System.out.println("Successful user login");
                    request.getSession().setAttribute("user", new User(username));
                    output.addProperty("status", "success");
                } else {
                    output.addProperty("status", "fail");
                    output.addProperty("message", "Invalid email or password");
                }
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

            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    public static void verifyRecaptcha(String gRecaptchaResponse) throws Exception {
        URL verifyUrl = new URL(SITE_VERIFY_URL);

        // Open Connection to URL
        HttpsURLConnection conn = (HttpsURLConnection) verifyUrl.openConnection();

        // Add Request Header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Data will be sent to the server.
        String postParams = "secret=" + SECRET_KEY + "&response=" + gRecaptchaResponse;

        // Send Request
        conn.setDoOutput(true);

        // Get the output stream of Connection
        // Write data in this stream, which means to send data to Server.
        OutputStream outStream = conn.getOutputStream();
        outStream.write(postParams.getBytes());

        outStream.flush();
        outStream.close();

        // Get the InputStream from Connection to read data sent from the server.
        InputStream inputStream = conn.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        JsonObject jsonObject = new Gson().fromJson(inputStreamReader, JsonObject.class);

        inputStreamReader.close();

        if (jsonObject.get("success").getAsBoolean()) {
            // verification succeed
            return;
        }

        throw new Exception("recaptcha verification failed: response is " + jsonObject);
    }
}

