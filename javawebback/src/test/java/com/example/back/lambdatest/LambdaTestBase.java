package com.example.back.lambdatest;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;

public class LambdaTestBase {

    protected RemoteWebDriver driver;
    protected WebDriverWait wait;
    protected boolean isMobile;

    // LambdaTest Credentials
    private static final String USERNAME = "vantu301204";
    private static final String ACCESS_KEY = "LT_eY9yd50ruiqTGKVGwL5wFNpXvMY5UrRHjWeNggNDUpeH9uI";
    private static final String GRID_URL = "@hub.lambdatest.com/wd/hub";

    @org.testng.annotations.Parameters({"browser", "version", "platform", "deviceName"})
    @BeforeMethod
    public void setup(String browser, String version, String platform, @org.testng.annotations.Optional("") String deviceName) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        // Mobile Capability Logic
        if (deviceName != null && !deviceName.isEmpty()) {
            this.isMobile = true;
            capabilities.setCapability("browserName", browser);
            // For mobile, 'version' param refers to OS Version (e.g. 11, 14)
            // Start with empty/null options map to put inside
        } else {
             // Desktop
            this.isMobile = false;
            capabilities.setCapability("browserName", browser);
            capabilities.setCapability("browserVersion", version);
        }

        capabilities.setCapability("platformName", platform); // Standard W3C capability checking
        
        HashMap<String, Object> ltOptions = new HashMap<>();
        ltOptions.put("username", USERNAME);
        ltOptions.put("accessKey", ACCESS_KEY);
        // platformName is already set in capabilities root
        
        if (deviceName != null && !deviceName.isEmpty()) {
            ltOptions.put("deviceName", deviceName);
            ltOptions.put("platformVersion", version); // Use version param as OS version
            ltOptions.put("isRealMobile", false); 
        }
        
        ltOptions.put("project", "FoxyStore Automation");
        ltOptions.put("build", "Cross-Browser & Mobile Admin Test Build");
        
        String testName = "Admin Create - " + browser + " on " + platform;
        if(deviceName != null && !deviceName.isEmpty()) {
            testName += " (" + deviceName + ")";
        }
        ltOptions.put("name", testName);
        
        ltOptions.put("tunnel", true); // Enable Tunnel
        ltOptions.put("w3c", true);
        
        capabilities.setCapability("LT:Options", ltOptions);

        try {
            driver = new RemoteWebDriver(new URL("https://" + USERNAME + ":" + ACCESS_KEY + GRID_URL), capabilities);
            
            // CRITICAL: Enable LocalFileDetector
            driver.setFileDetector(new LocalFileDetector());
            
            // Maximize only for Desktop
            if (deviceName == null || deviceName.isEmpty()) {
                driver.manage().window().maximize();
            }
            
            // Explicit wait configuration
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid Grid URL");
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Helper Methods
    protected void login(String email, String password) {
        // Assuming login URL is localhost:3000/login or similar based on typical React apps
        // Since we are running on cloud, we need the public URL or use ngrok if it's local.
        // HOWEVER, user asked to run on "File frontend". 
        // Based on "d:\FoxyStore-main\JavaWeb", it seems to be a local React app.
        // If the user hasn't deployed it, LambdaTest 'Local' tunnel might be needed.
        // Assuming the user wants to test the deployed version or has tunnel set up.
        // For now, I'll assume localhost:3000 maps to the app via LambdaTest Tunnel if used,
        // OR the user expects me to use `http://localhost:3000` assuming they enabled tunnel.
        // Given the prompt didn't specify Tunnel, I will use a placeholder or localhost.
        // Let's assume localhost but add a comment.
        
        driver.get("http://localhost:5173/login"); 
        
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Email'] | //input[@name='email'] | //input[@type='email']")));
        emailInput.clear();
        emailInput.sendKeys(email);
        
        WebElement passInput = driver.findElement(By.xpath("//input[@placeholder='Mật khẩu'] | //input[@type='password']"));
        passInput.clear();
        passInput.sendKeys(password);
        
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(),'ĐĂNG NHẬP')]"));
        loginBtn.click();

        // Handle "Đăng nhập thành công" Alert
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            // Ignore if no alert appears (just in case)
            System.out.println("No login alert appeared");
        }
        
        // Wait for login to complete (wait for URL to change from /login)
        // Note: App redirects to "/" if role includes "USER", so we can't strictly wait for "admin" here.
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }

    protected void waitForElementAndClick(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
    }

    protected void waitForElementAndSendKeys(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }
}
