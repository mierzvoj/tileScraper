package com.example.tileScraper.tileScraper;

import com.example.tileScraper.tileScraper.appConfig.AppConfig;
import com.example.tileScraper.tileScraper.scraperService.ImagesScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TileScraperApplication implements CommandLineRunner {

    private final ImagesScraperService scraperService;

    // Constructor injection for your service
    @Autowired
    public TileScraperApplication(ImagesScraperService scraperService) {
        this.scraperService = scraperService;
    }

    public static void main(String[] args) {
        SpringApplication.run(TileScraperApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String url = null;
        String search = null;

        // Parse command line arguments manually
        for (String arg : args) {
            if (arg.startsWith("--url=")) {
                url = arg.substring(6);
            } else if (arg.startsWith("--search=")) {
                search = arg.substring(9);
            }
        }

        System.out.println("URL: " + url);
        System.out.println("Search: " + search);

        // Call your service with the parsed arguments
        if (url != null && search != null) {
            scraperService.searchAndGetProductUrls(url, search);
        } else {
            System.err.println("Missing required arguments: url and/or search");
        }
    }
}