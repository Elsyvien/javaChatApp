package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading and writing files.
 * It can be expanded as needed for the application.
 */
public class FileOperations {

    /**
     * Reads all lines of a file as UTF-8. If the file does not exist,
     * an empty list is returned.
     */
    public List<String> readAllLines(String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    /**
     * Appends a single line to the given file using UTF-8 encoding.
     * The file and its parent directories are created if they do not already exist.
     */
    public void appendLine(String filePath, String line) throws IOException {
        Path path = Path.of(filePath);
        // Create parent directories if they don't exist
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        Files.write(path,
                (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    /**
     * Ensures that the parent directory of the given file path exists.
     * Creates the directory structure if it doesn't exist.
     */
    public void ensureDirectoryExists(String filePath) throws IOException {
        Path path = Path.of(filePath);
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Checks if a file exists at the given path.
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Path.of(filePath));
    }
}