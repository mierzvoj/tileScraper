package com.example.tileScraper.tileScraper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.tileScraper.tileScraper.scraperService.ImagesScraperService;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.tileScraper"})
public class TileScraperApplication implements CommandLineRunner {

    private final ImagesScraperService imagesScraperService;

    @Autowired
    public TileScraperApplication(ImagesScraperService imagesScraperService) {
        this.imagesScraperService = imagesScraperService;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TileScraperApplication.class);
        app.run(args);
    }

    @Override
    public void run(String... args) {
        // Parse command-line arguments manually
        String url = System.getProperty("url", "https://www.opoczno.eu");
        String search = System.getProperty("search", "terrazzo");

        // Set the values in the service
        imagesScraperService.setUrl(url);
        imagesScraperService.setSearchTerm(search);

        // Start the scraping process
        imagesScraperService.scrape();
    }
}