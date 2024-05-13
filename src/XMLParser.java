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
    private String dirId;
    private String dirName;
    private String filmId;
    private String filmTitle;
    private String filmYear;
    private List<String> categories;


    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb";
    private static final String DB_USER = "mytestuser";
    private static final String DB_PASSWORD = "My6$Password";



    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = qName;
        if (qName.equals("director")) {
            // Initialize director details
            dirId = "";
            dirName = "";
        } else if (qName.equals("films")) {
            // Initialize film details
            filmId = "";
            filmTitle = "";
            filmYear = "";
            categories = new ArrayList<>();
        }
        data = new StringBuilder();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("dirid")) {
            dirId = data.toString();
        } else if (qName.equals("dirname")) {
            dirName = data.toString();
        } else if (qName.equals("fid")) {
            filmId = data.toString();
        } else if (qName.equals("t")) {
            filmTitle = data.toString();
        } else if (qName.equals("year")) {
            filmYear = data.toString();
        } else if (qName.equals("cat")) {
            categories.add(data.toString());
        } else if (qName.equals("directorfilms")) {
            // Insert director and film data into the database
            insertDirectorIntoDatabase(dirId, dirName);
            insertFilmIntoDatabase(filmId, filmTitle, filmYear, dirId);
            insertCategoriesIntoDatabase(filmId, categories);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        data.append(new String(ch, start, length).trim());
    }

    private void insertDirectorIntoDatabase(String dirId, String dirName) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO director (dirid, dirname) VALUES (?, ?)";
            System.out.println(query);
            /*PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, dirId);
            statement.setString(2, dirName);
            statement.executeUpdate();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertFilmIntoDatabase(String filmId, String filmTitle, String filmYear, String dirId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO movie (fid, title, year, directorid) VALUES (?, ?, ?, ?)";
            System.out.println(query);
            /*PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, filmId);
            statement.setString(2, filmTitle);
            statement.setString(3, filmYear);
            statement.setString(4, dirId);
            statement.executeUpdate();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertCategoriesIntoDatabase(String filmId, List<String> categories) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            for (String category : categories) {
                // Check if category exists in the database, if not, insert it
                String query = "INSERT INTO genres (name) VALUES (?) ON DUPLICATE KEY UPDATE name = name";
                System.out.println(query);
                /*PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, category);
                statement.executeUpdate();*/

                // Associate film with category in genres_in_movies table
                query = "INSERT INTO genres_in_movies (genreid, movieid) VALUES ((SELECT id FROM genres WHERE name = ?), ?)";
                System.out.println(query);
                /*statement = connection.prepareStatement(query);
                statement.setString(1, category);
                statement.setString(2, filmId);
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
            saxParser.parse("../resources/mains243.xml", xmlParser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
