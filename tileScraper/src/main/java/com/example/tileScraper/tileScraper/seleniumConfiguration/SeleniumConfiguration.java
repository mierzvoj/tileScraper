package com.example.tileScraper.tileScraper.seleniumConfiguration;

import jakarta.annotation.PostConstruct;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumConfiguration {
    @PostConstruct
    void postConstruct() {
        String pathToChromeDriver = System.setProperty("webdriver.chrome.driver", "C:\\chromedriver\\chromedriver.exe");
    }

    @Bean
    public ChromeDriver ChromeDriver() {
        return new ChromeDriver();
    }
}
