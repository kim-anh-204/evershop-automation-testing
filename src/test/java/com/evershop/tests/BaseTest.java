package com.evershop.tests;

import com.evershop.pages.*;
import com.evershop.testdata.SearchData;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.*;

public class BaseTest implements ITestListener {

    protected WebDriver driver;
    protected LoginPage loginPage;
    protected AccountPage accountPage;
    protected AddressPopup addressPopup;
    protected HomePage homePage;
    protected SearchResultPage searchResultPage;

    @BeforeClass
    public void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    // inside BaseTest
    public WebDriver getDriver() {
        return this.driver; // adjust if your webdriver field has different name
    }

    @BeforeMethod
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        // options.addArguments("--headless"); // Commented out to show browser for debugging
        driver = new ChromeDriver(options);

        // Initialize page objects
        loginPage = new LoginPage(driver);
        accountPage = new AccountPage(driver);
        addressPopup = new AddressPopup(driver);
        homePage = new HomePage(driver);

        // Navigate to the home page
        driver.get(SearchData.getBaseUrl());
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void loginToAccount() {
        // Sử dụng account test của demo.evershop.io
        loginPage.login("kimanh61224@gmail.com", "123456");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (driver instanceof TakesScreenshot) {
            takeScreenshot("Screenshot on Failure: " + result.getMethod().getMethodName());
        }
    }

    @Attachment(value = "{description}", type = "image/png")
    public byte[] takeScreenshot(String description) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    // Other ITestListener methods can be empty or default implementation
    @Override
    public void onTestStart(ITestResult result) {}
    @Override
    public void onTestSuccess(ITestResult result) {}
    @Override
    public void onTestSkipped(ITestResult result) {}
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override
    public void onStart(org.testng.ITestContext context) {}
    @Override
    public void onFinish(org.testng.ITestContext context) {}
}
