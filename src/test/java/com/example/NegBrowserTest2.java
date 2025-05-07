package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

public class NegBrowserTest2 {

    private static final Logger logger = LogManager.getLogger(NegBrowserTest.class);
    private WebDriver driver;
    private WebDriverWait wait;
    private List<String> urls;
    private static final String EXPECTED_URL = "https://www.victoriassecret.com";

    @BeforeMethod
    @Parameters("browser")
    public void setUp(String browser) {
        driver = initializeWebDriver(browser);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        logger.info("WebDriver setup complete.");

        urls = readUrlsFromPropertiesFile("urls.properties");
    }

    /**
     * Initializes WebDriver based on the specified browser type.
     * 
     * @param browser Browser type (Chrome, Edge)
     * @return WebDriver instance
     */
    private WebDriver initializeWebDriver(String browser) {
        switch (browser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                logger.info("Launching Chrome.");
                return new ChromeDriver();
            case "edge":
                WebDriverManager.edgedriver().setup();
                logger.info("Launching Edge.");
                return new EdgeDriver();
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    /**
     * Reads URLs from the specified properties file located in classpath.
     * 
     * @param fileName The name of the properties file
     * @return List of URLs
     */
    private List<String> readUrlsFromPropertiesFile(String fileName) {
        List<String> urls = new ArrayList<>();
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                logger.error("Properties file not found: {}", fileName);
                return urls;
            }
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                urls.add(properties.getProperty(key));
            }
            logger.info("Successfully loaded URLs from properties file.");
        } catch (IOException e) {
            logger.error("Failed to read URLs from properties file.", e);
        }
        return urls;
    }

    @Test
    public void testOpenMultipleTabs() {
        for (String url : urls) {
            openNewTabAndCheckUrl(url);
        }
    }

    /**
     * Opens a new tab for the given URL and checks if it matches the expected URL.
     * 
     * @param url The URL to navigate
     */
    private void openNewTabAndCheckUrl(String url) {
        try {
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", url);
            driver.switchTo().window(driver.getWindowHandles().toArray()[driver.getWindowHandles().size() - 1].toString());

            logger.info("Navigated to {}", url);
            String currentUrl = driver.getCurrentUrl();

            if (!currentUrl.equals(url)) {
                logger.error("URL mismatch. Expected: {}, Actual: {}", EXPECTED_URL, currentUrl);

                // Check for error messages in page source
                if (driver.getPageSource().contains("This site can’t be reached")) {
                    logger.error("Detected site access failure message.");
                }

                // Capture browser console logs for debugging
                LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
                for (LogEntry entry : logs) {
                    logger.warn("Browser Log: {}", entry.getMessage());
                }
            }
        } catch (WebDriverException e) {
            logger.error("Navigation error: {}", e.getMessage());
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            logger.info("Browser session ended.");
        }
    }

    public static void main(String[] args) {
    	// Set Log4j configuration explicitly
        System.setProperty("log4j.configurationFile", "classpath:log4j.xml");

        String[] browsers = { "chrome", "edge" };
        for (String browser : browsers) {
            NegBrowserTest2 test = new NegBrowserTest2();
            test.setUp(browser);
            test.testOpenMultipleTabs();
            test.tearDown();
            logger.info("All browser tests completed.");
        }
    }
}
