package utils;

/*
 * Small utility class for file operations.
 * This class is intended to handle file-related operations such as reading and writing files.
 * It can be extended in the future to include more complex file handling functionalities.
 * @author Max Staneker, Mia Schienagel
 * @version 0.1
 */
import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


public class FileOperations {
    private final String filePath;
    private final File file;
    
    public FileOperations(String filePath) {
        this.filePath = filePath;
        this.file = new File(filePath);
    }
    
    public void writeToFile(String content) throws IOException {
        // Writes the given content to the file at the specified file path
        Files.write(Path.of(filePath), 
                    content.getBytes(), 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
    
    public String readFromFile() throws IOException {
        // Reads the content of the file at the specified file path
        return Files.readString(Path.of(filePath));
    }
    
    public void deleteFile() throws IOException {
        // Deletes the file at the specified file path
        if (!file.delete()) {
            throw new IOException("Failed to delete the file: " + filePath);
        }
    }
    
    public boolean fileExists() {
        return file.exists(); // Checks if the file exists
    }
    public String getFilePath() {
        return filePath; // Returns the file path
    }
    public File getFile() {
        return file; // Returns the File object
    }

}
