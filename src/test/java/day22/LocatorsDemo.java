package day22;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class LocatorsDemo {

	public static void main(String[] args) {
		// Set the path to the ChromeDriver executable
        System.setProperty("webdriver.chrome.driver", "/driver/chromedriver.exe");

        WebDriver driver = new ChromeDriver();

      
            // Navigate to the test page
            driver.get("https://seleniumbase.io/demo_page/");
            driver.manage().window().maximize();
            driver.quit();

	}

}
