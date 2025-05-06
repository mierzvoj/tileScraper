package com.example.tileScraper.tileScraper.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Utility class for file operations
 */
@Component
public class FileUtils {
    public FileUtils() {
    }

    /**
     * Saves a list of URLs to a text file
     *
     * @param urls List of URLs to save
     * @param filename Name of the file to save to
     * @return boolean indicating success or failure
     */
    public boolean saveUrlsToFile(List<String> urls, String filename) {
        try {
            // Create output directory if it doesn't exist
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Create the file in the output directory
            File file = new File(outputDir, filename);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            for (String url : urls) {
                writer.write(url);
                writer.newLine();  // Add a new line after each URL
            }

            writer.close();
            System.out.println("Successfully saved " + urls.size() + " URLs to " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("Error saving URLs to file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates a valid filename from a search term
     *
     * @param searchTerm The search term to create a filename from
     * @return A valid filename
     */
    public String createFilenameFromSearchTerm(String searchTerm) {
        // Replace invalid filename characters with underscores
        String sanitized = searchTerm.replaceAll("[^a-zA-Z0-9]", "_");
        return sanitized + "_urls.txt";
    }
}