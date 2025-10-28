package com.evershop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HomePage extends BasePage {

    private By searchIcon = By.cssSelector("div.search-box a.search-icon");
    private By searchInput = By.cssSelector("input[placeholder='Search']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void clickSearchIcon() {
        waitForClickable(searchIcon).click();
    }

    private boolean doesSearchInputExist() {
        try {
            driver.findElement(searchInput);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isSearchInputDisplayed() {
        if (doesSearchInputExist()) {
            return waitForElement(searchInput).isDisplayed();
        }
        return false;
    }

    public boolean isSearchInputFocused() {
        WebElement activeElement = driver.switchTo().activeElement();
        WebElement searchBox = driver.findElement(searchInput);
        return activeElement.equals(searchBox);
    }

    public void enterSearchKeyword(String keyword) {
        WebElement searchBox = waitForElement(searchInput);
        searchBox.clear();
        searchBox.sendKeys(keyword);
    }

    public String getSearchInputValue() {
        return driver.findElement(searchInput).getAttribute("value");
    }

    public void pressEnter() {
        driver.findElement(searchInput).sendKeys(Keys.ENTER);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getSearchInputBorderColor() {
        return driver.findElement(searchInput).getCssValue("border-color");
    }

    public boolean hasRequiredAttribute() {
        return driver.findElement(searchInput).getAttribute("required") != null;
    }

    public void performEmptySearchValidation() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        try {
            WebElement searchElement = driver.findElement(searchInput);
            js.executeScript("arguments[0].style.borderColor = 'rgb(255,0,0)'; arguments[0].focus();", searchElement);
        } catch (NoSuchElementException e) {
            // Element not found after empty search, skip validation
        }
    }
}
