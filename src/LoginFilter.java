import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private final ArrayList<String> dashboardURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        Employee employee = (Employee) httpRequest.getSession().getAttribute("employee");
        // Redirect to dashboard login page if the "employee" attribute doesn't exist in session
        if(this.isDashboardURL(httpRequest.getRequestURI())) {
            if (employee == null) {
                System.out.println("[LoginFilter] redirecting from " + httpRequest.getRequestURI());
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/public/_dashboard.html");
                return;
            } else {
                chain.doFilter(request, response);
            }
        }

        User user = (User) httpRequest.getSession().getAttribute("user");
        // Redirect to login page if the "user" attribute doesn't exist in session
        if (user == null) {
            System.out.println("[LoginFilter] redirecting from " + httpRequest.getRequestURI());
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/public/login.html");
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isDashboardURL(String requestURI) {
        return dashboardURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        //return true;
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        // Paths relative to index.html
        allowedURIs.add("/public/login.html");
        allowedURIs.add("/styles/login.css");
        allowedURIs.add("/styles/style.css");
        allowedURIs.add("/scripts/login.js");

        // Paths relative to other html files
        allowedURIs.add("login.html");
        allowedURIs.add("../styles/login.css");
        allowedURIs.add("../scripts/login.js");
        allowedURIs.add("../styles/style.css");

        // Paths relative to _dashboard.html
        allowedURIs.add("/public/_dashboard.html");
        allowedURIs.add("../scripts/dashboard-login.js");
        allowedURIs.add("_dashboard.html");

        dashboardURIs.add("/public/dashboard.html");
        dashboardURIs.add("/public/dashboard.css");
        dashboardURIs.add("/public/dashboard.js");
        dashboardURIs.add("/public/api/_dashboard");

        // Api endpoints for login and dashboard login
        allowedURIs.add("/public/api/login");
        allowedURIs.add("/public/api/_dashboard");

    }

    public void destroy() {
        // ignored.
    }

}
