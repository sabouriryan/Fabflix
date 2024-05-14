import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jakarta.servlet.ServletConfig;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class XMLParser extends DefaultHandler {
    private StringBuilder data;
    private String currentElement;
    private String dirName;
    private String movieId;
    private String movieTitle;
    private int movieYear;
    private List<String> genres;
    public int movies_visited = 0;
    private final List<IncompleteTag> incompleteTags;


    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb";
    private static final String DB_USER = "mytestuser";
    private static final String DB_PASSWORD = "My6$Password";

    public XMLParser() {
        incompleteTags = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        /*
        if (movies_visited > 3) {
            System.exit(0);
        }

         */

        currentElement = qName;
        if (qName.equals("director")) {
            // flush director details
            dirName = "";
        } else if (qName.equals("film")) {
            movies_visited++;
            // flush movie details
            movieId = "";
            movieTitle = "";
            movieYear = 0;
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
                try {
                    movieYear = Integer.parseInt(data.toString());
                } catch (NumberFormatException e) {
                    incompleteTags.add(new IncompleteTag(currentElement, data.toString()));
                    movieYear = 0; // Set to NULL if not a valid integer
                }
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
        String content = new String(ch, start, length).trim();
        data.append(content);

        // Check for incomplete or malformed tags
        if (currentElement != null && !currentElement.isEmpty() && !content.isEmpty()) {
            incompleteTags.add(new IncompleteTag(currentElement, content));
        }
    }


    private void insertMovieIntoDatabase(String movieId, String movieTitle, int movieYear, String dirId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
            System.out.println(query + " (" + movieId + ", " + movieTitle + ", " + movieYear + ", " + dirId + ")");
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, movieId);
            statement.setString(2, movieTitle);
            statement.setInt(3, movieYear);
            statement.setString(4, dirId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertGenresIntoDatabase(String movieId, List<String> genres) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            for (String genre : genres) {
                try {
                    // Check if category exists in the database, if not, insert it
                    String insertGenreQuery = "INSERT IGNORE INTO genres (name) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = ?)";
                    PreparedStatement statement = connection.prepareStatement(insertGenreQuery);
                    statement.setString(1, genre);
                    statement.setString(2, genre);
                    statement.executeUpdate();

                    // Associate movie with category in genres_in_movies table
                    String movieGenreQuery = "INSERT INTO genres_in_movies (genreId, movieId) VALUES ((SELECT id FROM genres WHERE name = ?), ?)";
                    statement = connection.prepareStatement(movieGenreQuery);
                    statement.setString(1, genre);
                    statement.setString(2, movieId);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printIncompleteTags() {
        for (IncompleteTag incompleteTag : incompleteTags) {
            System.out.println("Incomplete tag: " + incompleteTag.getTagName() + " Value: " + incompleteTag.getTagValue());
        }
    }

    public static void main(String[] args) {
        try {
            System.setProperty("file.encoding", "ISO-8859-1");
            // Create a SAXParser instance
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            // Parse the XML file
            XMLParser xmlParser = new XMLParser();
            saxParser.parse("./resources/mains243.xml", xmlParser);
            xmlParser.printIncompleteTags();
            System.out.println("Total movies:" + xmlParser.movies_visited);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
