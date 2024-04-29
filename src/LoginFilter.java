import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        User user = (User) httpRequest.getSession().getAttribute("user");
        // Redirect to login page if the "user" attribute doesn't exist in session
        if (user == null) {
            System.out.println("Not allowed, redirecting...");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/public/login.html");
        } else {
            System.out.println("URI "+ httpRequest.getRequestURI() + " allowed");
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        // Paths relative to index.html
        allowedURIs.add("/public/login.html");
        allowedURIs.add("/styles/style.css");
        allowedURIs.add("/scripts/login.js");

        // Paths relative to other html files
        allowedURIs.add("login.html");
        allowedURIs.add("../scripts/login.js");
        allowedURIs.add("../styles/style.css");

        // Api endpoint relative to login.html
        allowedURIs.add("/public/api/login");
    }

    public void destroy() {
        // ignored.
    }

}
