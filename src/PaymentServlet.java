import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "PaymentServlet", urlPatterns = "/public/api/payment")
public class PaymentServlet extends HttpServlet {
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
        String first_name = request.getParameter("first-name");
        String last_name = request.getParameter("last-name");
        String credit_card_number = request.getParameter("credit-card-number");
        String expiration_date = request.getParameter("expiration-date");

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            String paymentQuery = "SELECT cc.id, cc.firstName, cc.lastName, cc.expiration FROM creditcards cc " +
                                "WHERE cc.id = ? AND cc.firstName = ? AND cc.lastName = ? AND cc.expiration = ?";

            PreparedStatement pstmtPayment = conn.prepareStatement(paymentQuery);
            pstmtPayment.setString(1, credit_card_number);
            pstmtPayment.setString(2, first_name);
            pstmtPayment.setString(3, last_name);
            pstmtPayment.setString(4, expiration_date);
            ResultSet rs = pstmtPayment.executeQuery();

            JsonObject output = new JsonObject();
            if (rs.next()) {
                System.out.println("Successful payment info");
                output.addProperty("status", "success");
            } else {
                System.out.println("Unsuccessful payment info");
                output.addProperty("status", "fail");
                output.addProperty("message", "One or more fields is incorrect. Please try again.");
            }

            rs.close();
            pstmtPayment.close();

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
