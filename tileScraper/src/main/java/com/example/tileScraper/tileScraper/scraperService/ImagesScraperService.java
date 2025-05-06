package com.example.tileScraper.tileScraper.scraperService;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ImagesScraperService {

    // Setter methods for URL and search term
    @Setter
    private String url;
    @Setter
    private String searchTerm;
    private ChromeDriver driver;

    // Use constructor with direct string parameters instead of property injection
    public ImagesScraperService() {
        // Empty constructor
    }



    public void scrape() {
        try {
            System.out.println("Starting scraping with URL: " + url + ", Search term: " + searchTerm);

            // Initialize the browser
            initializeDriver();

            // Do the scraping
            List<String> productUrls = searchAndGetProductUrls();

            // Process results
            System.out.println("Found " + productUrls.size() + " product URLs");

            // Close the browser when done
            if (driver != null) {
                driver.quit();
            }

        } catch (Exception e) {
            System.err.println("Error in scraping process: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // The rest of your methods remain the same
    public List<String> searchAndGetProductUrls() {
        List<String> productUrls = new ArrayList<>();
        try {
            System.out.println("Navigating to " + url);
            driver.get(url);

            // Wait longer for page to fully load
            Thread.sleep(3000);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // Try various selectors for the search box with better waiting
            WebElement searchBox = null;
            try {
                // Try to find the search box using various selectors
                String[] selectors = {
                        "input[type='text']",
                        "input[name='query']",
                        "input[name='search']",
                        ".search-input",
                        "#search",
                        ".search-field",
                        "[placeholder*='search']",
                        "[placeholder*='Search']",
                        "form input[type='text']"
                };

                for (String selector : selectors) {
                    try {
                        // First check if element is visible
                        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector(selector)));

                        // Then check if it's clickable
                        element = wait.until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector(selector)));

                        searchBox = element;
                        System.out.println("Found search box with selector: " + selector);
                        break;
                    } catch (Exception e) {
                        // Continue to next selector
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not find search box with any selector: " + e.getMessage());
                throw e;
            }

            if (searchBox == null) {
                throw new RuntimeException("Could not find search box element with any selector");
            }

            // Try to click on the search box first to ensure it's focused
            searchBox.click();
            Thread.sleep(500);

            // Try alternative clear approach instead of clear()
            searchBox.sendKeys(Keys.CONTROL + "a");
            searchBox.sendKeys(Keys.DELETE);

            // Enter search term
            searchBox.sendKeys(searchTerm);
            searchBox.sendKeys(Keys.ENTER);

            // Wait for search results to load with a more general selector
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".product, .product-card, article, .results, .search-results")));
            } catch (Exception e) {
                System.out.println("Could not detect specific result elements, continuing anyway");
            }

            // Allow more time for results to load
            Thread.sleep(3000);

            // Scroll down to load all results
            scrollToLoadAllResults();

            // Find all product links with a more comprehensive selector
            List<WebElement> productElements = driver.findElements(
                    By.cssSelector("a[href*='product'], a[href*='p-'], a[href*='item'], .product a, .product-card a, article a"));

            // Extract and store URLs
            for (WebElement element : productElements) {
                String href = element.getAttribute("href");
                if (href != null && !href.isEmpty() && !productUrls.contains(href)) {
                    productUrls.add(href);
                }
            }

            System.out.println("Scraped " + productUrls.size() + " product URLs");

        } catch (Exception e) {
            System.err.println("Error during search and scrape: " + e.getMessage());
            e.printStackTrace();
        }

        return productUrls;
    }

    private void initializeDriver() {
        ChromeOptions options = new ChromeOptions();

        // To avoid detection as a bot
        options.addArguments(
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);
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