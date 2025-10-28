package com.evershop.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ExtentTestNGIReporter implements ITestListener, IReporter {

    private static ExtentReports extent;
    private static ExtentSparkReporter sparkReporter;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static Map<String, WebDriver> drivers = new HashMap<>();

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void setDriver(String testName, WebDriver driver) {
        drivers.put(testName, driver);
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        extent.flush();
    }

    @Override
    public void onStart(ITestContext context) {
        if (extent == null) {
            String reportPath = System.getProperty("user.dir") + "/ExtentReports/ExtentReport.html";
            File reportDir = new File("ExtentReports");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }

            sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setDocumentTitle("EverShop Automation Test Report");
            sparkReporter.config().setReportName("Test Report");
            sparkReporter.config().setTheme(Theme.DARK);

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("Environment", "Production");
            extent.setSystemInfo("User", "TestUser");
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = extent.createTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.PASS, "Test passed: " + result.getMethod().getMethodName());
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.FAIL, "Test failed: " + result.getMethod().getMethodName());
            extentTest.log(Status.FAIL, result.getThrowable());

            // Capture screenshot
            String testName = result.getMethod().getMethodName();
            WebDriver driver = drivers.get(testName);
            if (driver instanceof TakesScreenshot) {
                try {
                    String screenshotPath = captureScreenshot(driver, testName);
                    extentTest.addScreenCaptureFromPath(screenshotPath);
                } catch (IOException e) {
                    extentTest.log(Status.WARNING, "Failed to attach screenshot");
                }
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest extentTest = test.get();
        if (extentTest != null) {
            extentTest.log(Status.SKIP, "Test skipped: " + result.getMethod().getMethodName());
        }
    }

    private String captureScreenshot(WebDriver driver, String testName) throws IOException {
        String screenshotDir = "ExtentReports/screenshots/";
        File dir = new File(screenshotDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String screenshotName = testName + "_" + System.currentTimeMillis() + ".png";
        String screenshotPath = screenshotDir + screenshotName;

        byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Files.write(new File(screenshotPath).toPath(), screenshotBytes);
        return screenshotPath;
    }
}
