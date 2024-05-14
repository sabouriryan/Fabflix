import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jakarta.servlet.ServletConfig;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;



public class XMLParser extends DefaultHandler {
    private StringBuilder data;
    private String currentElement;
    private String dirName;
    private String movieId;
    private String movieTitle;
    private String movieYear;
    private List<String> genres;
    private int movies_visited = 0;


    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb";
    private static final String DB_USER = "mytestuser";
    private static final String DB_PASSWORD = "My6$Password";



    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (movies_visited >= 3) {
            System.exit(0);
        }


        currentElement = qName;
        if (qName.equals("director")) {
            // flush director details
            dirName = "";
        } else if (qName.equals("film")) {
            movies_visited++;
            // flush movie details
            movieId = "";
            movieTitle = "";
            movieYear = "";
            genres = new ArrayList<>();
        }
        data = new StringBuilder();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "dirname":
                dirName = data.toString();
                break;
            case "fid":
                movieId = data.toString();
                break;
            case "t":
                movieTitle = data.toString();
                break;
            case "year":
                movieYear = data.toString();
                break;
            case "cat":
                genres.add(data.toString());
                break;
            case "film":
                // Insert director and movie data into the database
                insertMovieIntoDatabase(movieId, movieTitle, movieYear, dirName);
                insertGenresIntoDatabase(movieId, genres);
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        data.append(new String(ch, start, length).trim());
    }

    private void insertMovieIntoDatabase(String movieId, String movieTitle, String movieYear, String dirId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO movie (fid, title, year, directorid) VALUES (?, ?, ?, ?)";
            System.out.println(query);
            /*PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, movieId);
            statement.setString(2, movieTitle);
            statement.setString(3, movieYear);
            statement.setString(4, dirId);
            statement.executeUpdate();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertGenresIntoDatabase(String movieId, List<String> genres) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            for (String genre : genres) {
                // Check if category exists in the database, if not, insert it
                String query = "INSERT INTO genres (name) VALUES (?) ON DUPLICATE KEY UPDATE name = name";
                System.out.println(query);
                /*PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, genre);
                statement.executeUpdate();*/

                // Associate movie with category in genres_in_movies table
                query = "INSERT INTO genres_in_movies (genreId, movieId) VALUES ((SELECT id FROM genres WHERE name = ?), ?)";
                System.out.println(query);
                /*statement = connection.prepareStatement(query);
                statement.setString(1, genre);
                statement.setString(2, movieId);
                statement.executeUpdate();*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Create a SAXParser instance
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            // Parse the XML file
            XMLParser xmlParser = new XMLParser();
            saxParser.parse("/Users/wilberthgonzalez/Downloads/stanford-movies/mains243.xml", xmlParser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
