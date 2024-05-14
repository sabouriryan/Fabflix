import java.io.File;

public class FileExistenceChecker {

    public static void main(String[] args) {
        // Provide the file path here
        String filePath = "./resources/actors63.xml";

        // Create a File object with the provided file path
        File file = new File(filePath);

        // Check if the file exists
        if (file.exists()) {
            System.out.println("The file exists.");
        } else {
            System.out.println("The file does not exist.");
        }
    }
}
