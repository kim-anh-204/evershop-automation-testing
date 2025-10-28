package com.evershop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    // Locators
    private By emailInput = By.name("email");
    private By passwordInput = By.name("password");
    private By loginButton = By.cssSelector("button[type='submit']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToLoginPage() {
        driver.get("https://demo.evershop.io/account/login");
    }

    public void login(String email, String password) {
        navigateToLoginPage();
        waitForElement(emailInput).sendKeys(email);
        waitForElement(passwordInput).sendKeys(password);
        waitForClickable(loginButton).click();
        sleep(2000); // Wait for redirect
    }
}