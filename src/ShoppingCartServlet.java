import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Map;
import java.util.HashMap;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/public/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource; // Create a dataSource which registered in web.

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private void printRequestURL(HttpServletRequest request) {
        String initialUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString != null) {
            initialUrl += "?" + queryString;
        }
        System.out.println("Shopping URL: " + initialUrl);
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        printRequestURL(request);

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(true);
        String action = request.getParameter("action");
        String movieId = request.getParameter("movie-id");

        try (Connection conn = dataSource.getConnection()) {
            if ("increment".equals(action)) {
                incrementCartItem(session, movieId);
            } else if ("decrement".equals(action)) {
                decrementCartItem(session, movieId);
            } else if ("delete".equals(action)) {
                deleteCartItem(session, movieId);
            }



        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("errorMessage", e.getMessage());
            out.write(errorObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }

    private void incrementCartItem(HttpSession session, String movieId) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            session.setAttribute("user", new User("test"));
            user = (User) session.getAttribute("user");
        }

        Map<String, Integer> cartItems = user.getShoppingCartItems();
        if (cartItems.containsKey(movieId)) {
            int count = cartItems.get(movieId);
            cartItems.put(movieId, count + 1);
        } else {
            cartItems.put(movieId, 1);
        }
    }

    private void decrementCartItem(HttpSession session, String movieId) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            // User not logged in, handle accordingly
            return;
        }

        Map<String, Integer> cartItems = user.getShoppingCartItems();
        if (cartItems.containsKey(movieId)) {
            int count = cartItems.get(movieId);
            if (count > 1) {
                cartItems.put(movieId, count - 1);
            } else {
                cartItems.remove(movieId);
            }
        }
    }

    private void deleteCartItem(HttpSession session, String movieId) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            // User not logged in, handle accordingly
            return;
        }

        Map<String, Integer> cartItems = user.getShoppingCartItems();
        cartItems.remove(movieId);
    }

    private Map<String, Integer> getShoppingCart(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            // User not logged in, handle accordingly
            return new HashMap<>();
        }

        return user.getShoppingCartItems();
    }
}
