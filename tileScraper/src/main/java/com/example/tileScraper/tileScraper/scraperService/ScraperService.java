package com.example.tileScraper.tileScraper.scraperService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScraperService implements CommandLineRunner {
    private final ApplicationArguments arguments;
    private final ChromeDriver driver =  new ChromeDriver();

    @Value("${scraper.baseUrl:https://www.google.com/}")
    private String URL = null;


    public ScraperService(ApplicationArguments arguments) {
        this.arguments = arguments;
    }

    @Override
    public void run(String... args) throws Exception {
        if (arguments.containsOption("arg1")) {
            System.out.println("arg1 value" + arguments.getOptionValues("arg1").get(0));
            URL = arguments.getOptionValues("arg1").get(0);
        }
//     ChromeDriver driver = new ChromeDriver();
        try{
            String searchTerm = "fishsticks";
            if(arguments.containsOption("search")) {
                URL = arguments.getOptionValues("search").get(0);
            }
            scrape(searchTerm);
        } finally {
            driver.quit();
        }

    }


    public void scrape(final String value) {
        try {
            System.out.println("Scraping with URL: " + URL + " and search term: " + value);
            driver.get(URL + value);
            final WebElement words = driver.findElement(By.className("words"));
            final List<WebElement> wordList = words.findElements((By.tagName("a")));
            wordList.forEach((word -> System.out.println(word.getText())));
        } catch (Exception e) {
            System.err.println("Error during scraping: " + e.getMessage());
        }
    }
}
