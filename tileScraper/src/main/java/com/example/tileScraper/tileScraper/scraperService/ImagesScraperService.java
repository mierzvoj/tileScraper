package com.example.tileScraper.tileScraper.scraperService;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ImagesScraperService implements CommandLineRunner {

    @Value("${scraper.baseUrl:https://www.google.com/}")
    private String url;

    @Value("${tiles:ceramic tiles}")
    private String searchTerm;

    private final ApplicationArguments arguments;
    private ChromeDriver driver;

    public ImagesScraperService(ApplicationArguments arguments) {
        this.arguments = arguments;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            if (arguments.containsOption("url")) {
                url = arguments.getOptionValues("url").get(0);
            }

            if (arguments.containsOption("search")) {
                searchTerm = arguments.getOptionValues("search").get(0);
            }


            initializeDriver();  // Initialize the driver here

            List<String> productUrls = searchAndGetProductUrls(url, searchTerm);

            System.out.println("Found " + productUrls.size() + " product URLs:");
            for (String productUrl : productUrls) {  // Renamed to avoid conflict with url field
                System.out.println(productUrl);
            }

        } catch (Exception e) {
            System.err.println("Error during scraping: " + e.getMessage());

        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public List<String> searchAndGetProductUrls(String url, String searchTerm) {
        List<String> productUrls = new ArrayList<>();
        try {
            System.out.println("Navigating to " + url);
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(
                            "input[type='text'], input[name='query'], input[name='search'], .search-input, #search")));
            // Clear any existing text and enter search term
            searchBox.clear();
            searchBox.sendKeys(searchTerm);
            searchBox.sendKeys(Keys.ENTER);

            // Wait for search results to load
            // Adjust selector based on actual website structure
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".title-module, .product-card, .product, article")));

            // Allow some extra time for any dynamic content to load
            Thread.sleep(2000);

            // Scroll down to load all results if needed (for pages with lazy loading)
            scrollToLoadAllResults();

            // Find all product links
            // Adjust selector based on actual website structure
            List<WebElement> productElements = driver.findElements(
                    By.cssSelector(".product-item a, .product-card a, .product a, article a"));

            // Extract and store URLs
            for (WebElement element : productElements) {
                String href = element.getAttribute("href");
                if (href != null && !href.isEmpty() && !productUrls.contains(href)) {
                    // Only add product detail pages (usually contains 'product' or 'p-' in URL)
                    // Adjust this filter based on the actual URL patterns of the site
                    if (href.contains("product") || href.contains("p-") || href.contains("item")) {
                        productUrls.add(href);
                    }
                }
            }

            System.out.println("Scraped " + productUrls.size() + " product URLs");

        } catch (Exception e) {
            System.err.println("Error during search and scrape: " + e.getMessage());
        }

        return productUrls;
    }

    private void initializeDriver() {  // Method names should start with lowercase
        ChromeOptions options = new ChromeOptions();

        // To avoid detection as a bot
        options.addArguments(
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));  // Fixed variable declaration

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    private void scrollToLoadAllResults() {
        try {
            // Define number of scrolls (adjust based on needs)
            int scrollCount = 5;

            for (int i = 0; i < scrollCount; i++) {
                // Scroll down
                driver.executeScript("window.scrollTo(0, document.body.scrollHeight)");

                // Wait for content to load
                Thread.sleep(1000);

                // Get current height
                Long currentHeight = (Long) driver.executeScript("return document.body.scrollHeight");

                // Scroll up slightly and back down to trigger any lazy loading
                driver.executeScript("window.scrollTo(0, " + (currentHeight - 200) + ")");
                Thread.sleep(500);
                driver.executeScript("window.scrollTo(0, " + currentHeight + ")");

                // Wait for any new content
                Thread.sleep(1500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}