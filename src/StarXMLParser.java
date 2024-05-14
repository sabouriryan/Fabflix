import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StarXMLParser extends DefaultHandler {
    private StringBuilder data;
    private String currentElement;
    private String starName;
    private String starId;
    private int birthYear;
    private final List<IncompleteTag> incompleteTags;

    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb";
    private static final String DB_USER = "mytestuser";
    private static final String DB_PASSWORD = "My6$Password";

    public StarXMLParser() {
        incompleteTags = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = qName;
        if (qName.equals("actor")) {
            // flush director details
            starName = "";
            birthYear = 0;
            starId = "";
        }
        data = new StringBuilder();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "stagename":
                starName = data.toString();
                break;
            case "dob":
                try {
                    birthYear = Integer.parseInt(data.toString());
                } catch (NumberFormatException e) {
                    incompleteTags.add(new IncompleteTag(currentElement, data.toString()));
                    birthYear = 0; // Set to NULL if not a valid integer
                }
                break;
            case "actor":
                // Insert star data into the database
                insertStarIntoDatabase(starName, birthYear);
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

    private void insertStarIntoDatabase(String starName, int birthYear) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Generate starId
            String idNumQuery = "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1, 1) FROM stars";
            Statement idStatement = connection.createStatement();
            java.sql.ResultSet rs = idStatement.executeQuery(idNumQuery);
            rs.next();
            String starId = "nm" + rs.getInt(1);

            String query = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            System.out.println("INSERT INTO stars (" + starId + ", " + starName + ", " + birthYear + ")");

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, starId);
            statement.setString(2, starName);
            statement.setInt(3, birthYear);
            statement.executeUpdate();
        } catch (SQLException e) {
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
            // Create a SAXParser instance
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            // Parse the XML file
            StarXMLParser starXMLParser = new StarXMLParser();
            saxParser.parse("./resources/actors63.xml", starXMLParser);
            starXMLParser.printIncompleteTags();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
