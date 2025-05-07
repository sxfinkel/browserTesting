package com.example;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.*;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NegBrowserTest {
	
	//logger for logging info, warnings, and errors
	
	private static final Logger Logger = LogManager.getLogger(NegBrowserTest.class);
	private WebDriver driver;
	private WebDriverWait wait;
	private List<String> urls;
	private static final String EXPECTED_URL = "https://www.victoriassecret.com";
	
	/**
	 * Sets up the WebDriver based on teh browser parameter and initializes the WebDriver
	 * Reads URLs from the properties file.
	 * @param browser The browser to use for testing (chrome,edge)
	 */

	@BeforeMethod
	@Parameters("browser")
	public void setUp(String browser) {
		switch (browser.toLowerCase()) {
		case "chrome":
			WebDriverManager.chromedriver().setup();
			driver = new ChromeDriver();
			Logger.info("Starting session testing in Chrome.");
			break;
		case "edge":	
			WebDriverManager.edgedriver().setup();
			driver = new EdgeDriver();
			Logger.info("Starting session testing in Edge.");
			break;
		default:
			throw new IllegalArgumentException("Browser not supported: " + browser);
		}
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		Logger.info("Setup complete.");
		
		//read urls from properties file
		
		urls = readUrlsFromPropertiesFile("urls.properties");
	}
	/**
	 * Read URLs from the specified properties file
	 * @param filepath the path to the properties file containing URLs
	 * @return a list of URLs.
	 */
	
	private List<String> readUrlsFromPropertiesFile(String filePath) {
		List<String> urls = new ArrayList<>();
		Properties properties = new Properties();
		try (FileInputStream input = new FileInputStream(filePath)) {
			properties.load(input);
			for (String key : properties.stringPropertyNames()) {
				urls.add(properties.getProperty(key));
			}
		    } catch (IOException e) {
		    	Logger.error("Failed to read URLs from property file.", e);
	}
		return urls;
		
	}
	/**
	 * Opens multiple tabs and checks each URL
	 * If the URL is incorrect, captures the message from the page
	  */
	@Test
	public void testOpenMultipleTabs() {
		for (String url : urls) {
			openNewTabAndCheckUrl(url);
		}
	}
	/**
	 * Open a new tab for the given URL and checks if it matches the expected URL.
	 * If the URL is incorrect, captures the message from the page.
	 * @param url The URL to open and check.
	 */
	private void openNewTabAndCheckUrl(String url) {
	    try {
	        // Open a new tab and navigate to the specified URL
	        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", url);
	        
	        // Switch to the newly opened tab
	        for (String handle : driver.getWindowHandles()) {
	            driver.switchTo().window(handle);
	        }

	        Logger.info("Navigated to {}", url);

	        // Get the current URL after navigation
	        String currentUrl = driver.getCurrentUrl();

	        // Check if the navigation was successful
	        if (!currentUrl.equals(url)) {
	            Logger.error("URL does not match the expected URL. Actual URL: {}", currentUrl);

	            // Retrieve the page source and check if it contains a common error message
	            String pageSource = driver.getPageSource();
	            if (pageSource.contains("This site can’t be reached")) {
	                Logger.error("Detected site access failure message.");
	            }

	            // (Optional) Capture browser console logs for additional debugging
	            LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
	            for (LogEntry entry : logs) {
	                Logger.error("Browser Log: " + entry.getMessage());
	            }
	        }

	    } catch (WebDriverException e) {
	        // Handle WebDriver-related errors, such as navigation failures
	        Logger.error("Navigation failed due to a WebDriver issue: {}", e.getMessage());
	    }
	}

	@AfterMethod
	public void tearDown() {
		if (driver != null) {
			driver.quit();
			Logger.info("Browser session ended.");
		}
	}
	public static void main(String[] args) {
		String[] browsers = {"chrome", "edge"};
		for (String browser : browsers) {
			NegBrowserTest test = new NegBrowserTest();
			test.setUp(browser);
			test.testOpenMultipleTabs();
			test.tearDown();
		Logger.info("All browser testing is done");	
		}
		
		
		
		
		
		  
			
	
}
}
	
