import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called ReturnServlet, which maps to url "/api/return"
@WebServlet(name = "ReturnServlet", urlPatterns = "/public/api/return")
public class ReturnServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String queryString = (String) session.getAttribute("queryString");
        System.out.println("Query String received from session: " + queryString);
        if (queryString != null) {
            response.sendRedirect(request.getContextPath() + "/public/movie-list.html?" + queryString);
        } else {
            // Handle case where query string is not found
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
