import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnect {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb";
    private static final String DB_USER = "mytestuser";
    private static final String DB_PASSWORD = "My6$Password";

    public static void main(String[] args) {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connection successful!");
            // Close the connection after successful authentication
            connection.close();
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found");
        } catch (SQLException e) {
            System.out.println("Authentication failed: " + e.getMessage());
        }
    }
}
/* 
-- Check if the movie title already exists
--IF EXISTS (SELECT 1 FROM movies WHERE title = movie_title AND director = movie_director AND year = movie_year) THEN
    -- Movie title is a duplicate, end the procedure
-- Check if the movie title already exists*/