package com.example.tileScraper.tileScraper.scraperService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.example.tileScraper.tileScraper.utilities.FileUtils;
import lombok.Setter;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
public class ImagesScraperService {

    // Setter methods for URL and search term
    @Setter
    private String url;
    @Setter
    private String searchTerm;
    private ChromeDriver driver;
    private final FileUtils fileUtils;

    // Use constructor with direct string parameters instead of property injection
    public ImagesScraperService(FileUtils fileUtils) {
        // Empty constructor
        this.fileUtils = fileUtils;
    }



    public void scrape() {
        try {
            System.out.println("Starting scraping with URL: " + url + ", Search term: " + searchTerm);

            // Initialize the browser
            initializeDriver();

            // Do the scraping
            List<String> productUrls = searchAndGetProductUrls();

            // Save the URLs to a file
            if (!productUrls.isEmpty()) {
                String filename = fileUtils.createFilenameFromSearchTerm(searchTerm);
                fileUtils.saveUrlsToFile(productUrls, filename);
            } else {
                System.out.println("No URLs found to save");
            }

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

            // Wait longer for initial page load
            Thread.sleep(5000);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Try different search interaction approaches
            boolean searchSuccessful = false;

            // Approach 1: Use direct JavaScript to submit search
            try {
                System.out.println("Trying JavaScript search approach");
                JavascriptExecutor js = (JavascriptExecutor) driver;

                // This approach bypasses the need to find the search input
                String script = "var searchForms = document.querySelectorAll('form');" +
                        "for(var i=0; i < searchForms.length; i++) {" +
                        "  var inputs = searchForms[i].querySelectorAll('input[type=\"text\"], input[type=\"search\"]');" +
                        "  if(inputs.length > 0) {" +
                        "    inputs[0].value = '" + searchTerm + "';" +
                        "    searchForms[i].submit();" +
                        "    return true;" +
                        "  }" +
                        "}" +
                        "return false;";

                Boolean result = (Boolean) js.executeScript(script);
                if (result) {
                    System.out.println("JavaScript search successful");
                    searchSuccessful = true;
                    Thread.sleep(5000); // Wait for results to load
                }
            } catch (Exception e) {
                System.out.println("JavaScript search approach failed: " + e.getMessage());
            }

            // Approach 2: Try to find and interact with search icon first
            if (!searchSuccessful) {
                try {
                    System.out.println("Trying search icon approach");
                    // Find a search icon/button - common in many sites
                    WebElement searchIcon = wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("[class*='search-icon'], [class*='search-btn'], [id*='search-btn'], button[aria-label*='Search']")));

                    // Use JavaScript to click the search icon
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchIcon);
                    System.out.println("Clicked search icon");
                    Thread.sleep(3000); // Wait for search field to appear

                    // Now find the input that appeared
                    WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("input[type='text'], input[type='search'], input[placeholder*='search'], input[placeholder*='Search']")));

                    // Use JavaScript to set value and submit
                    ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", searchInput, searchTerm);
                    searchInput.sendKeys(Keys.ENTER);

                    System.out.println("Entered search term via search icon approach");
                    searchSuccessful = true;
                    Thread.sleep(5000); // Wait for results to load
                } catch (Exception e) {
                    System.out.println("Search icon approach failed: " + e.getMessage());
                }
            }

            // Approach 3: Navigate directly to search URL if possible
            if (!searchSuccessful) {
                try {
                    System.out.println("Trying direct search URL approach");
                    // Many sites have a search URL pattern - attempt with common formats
                    String searchUrl = url;
                    if (!searchUrl.endsWith("/")) {
                        searchUrl += "/";
                    }

                    // Try common search URL patterns
                    String encodedSearchTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8.toString());
                    String directSearchUrl = searchUrl + "search?q=" + encodedSearchTerm;

                    System.out.println("Navigating to direct search URL: " + directSearchUrl);
                    driver.get(directSearchUrl);

                    Thread.sleep(5000); // Wait for results to load
                    searchSuccessful = true;
                } catch (Exception e) {
                    System.out.println("Direct search URL approach failed: " + e.getMessage());
                }
            }

            // If none of the approaches worked, we can't proceed
            if (!searchSuccessful) {
                throw new RuntimeException("Could not perform search using any approach");
            }

            // Now look for search results
            System.out.println("Looking for search results");

            // Take a screenshot to help debug
            // File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            // Files.copy(screenshot.toPath(), new File("search_results.png").toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Get page source for debugging
            // String pageSource = driver.getPageSource();
            // Files.write(new File("page_source.html").toPath(), pageSource.getBytes(StandardCharsets.UTF_8));

            // Wait for results and scroll
            try {
                Thread.sleep(2000);
                scrollToLoadAllResults();
            } catch (Exception e) {
                System.out.println("Error during scrolling: " + e.getMessage());
            }

            // Try different selectors for finding product links
            List<WebElement> productElements = new ArrayList<>();

            String[] resultSelectors = {
                    ".result a",
                    ".search-result a",
                    ".title a",
                    "h3.title a",
                    ".product-item a",
                    "[class*='product'] a",
                    "[class*='result'] a",
                    "a[href*='product']",
                    "a[href*='item']"
            };

            for (String selector : resultSelectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                    if (!elements.isEmpty()) {
                        System.out.println("Found " + elements.size() + " elements with selector: " + selector);
                        productElements.addAll(elements);
                    }
                } catch (Exception e) {
                    System.out.println("Error finding elements with selector " + selector + ": " + e.getMessage());
                }
            }

            System.out.println("Found total of " + productElements.size() + " potential product links");

            // Extract URLs (with deduplication)
            Set<String> uniqueUrls = new HashSet<>();

            for (WebElement element : productElements) {
                try {
                    String href = element.getAttribute("href");
                    if (href != null && !href.isEmpty() && !uniqueUrls.contains(href)) {
                        uniqueUrls.add(href);
                    }
                } catch (Exception e) {
                    // Ignore elements that can't be processed
                }
            }

            productUrls.addAll(uniqueUrls);
            System.out.println("Added " + productUrls.size() + " unique product URLs");

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