import com.google.gson.JsonArray;
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
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "DashboardServlet", urlPatterns = "/_dashboard/api/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet tablesResultSet = metadata.getTables("moviedb", null, "%", null);

            JsonArray tablesArray = new JsonArray();
            while (tablesResultSet.next()) {
                String tableName = tablesResultSet.getString("TABLE_NAME");
                JsonObject tableObject = new JsonObject();
                tableObject.addProperty("table_name", tableName);

                ResultSet columnsResultSet = metadata.getColumns("moviedb", null, tableName, null);
                JsonArray columnsArray = new JsonArray();
                while (columnsResultSet.next()) {
                    String columnName = columnsResultSet.getString("COLUMN_NAME");
                    String columnType = columnsResultSet.getString("TYPE_NAME");
                    JsonObject columnObject = new JsonObject();
                    columnObject.addProperty("column_name", columnName);
                    columnObject.addProperty("column_type", columnType);
                    columnsArray.add(columnObject);
                }
                tableObject.add("columns", columnsArray);
                tablesArray.add(tableObject);
            }
            out.write(tablesArray.toString());
            response.setStatus(200);

        } catch (SQLException e) {
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