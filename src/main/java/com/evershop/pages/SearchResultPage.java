package com.evershop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.ArrayList;
import java.util.List;

public class SearchResultPage extends BasePage {

    private By productItems = By.cssSelector(".listing-tem");
    private By noResultMessage = By.xpath("//*[contains(text(), 'There is no product to display')]");

    public SearchResultPage(WebDriver driver) {
        super(driver);
        waitForUrl("search");
        // Wait for products to load
        sleep(1000);
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public List<WebElement> getProductItems() {
        return driver.findElements(productItems);
    }

    public int getProductCount() {
        return getProductItems().size();
    }

    public boolean isNoResultMessageDisplayed() {
        try {
            return waitForElement(noResultMessage).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getNoResultMessage() {
        return driver.findElement(noResultMessage).getText();
    }

    public boolean allProductsContainKeyword(String keyword) {
        List<String> productNames = getProductNames();
        for (String productName : productNames) {
            if (!productName.toLowerCase().contains(keyword.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    public boolean allProductsContainAllKeywords(String[] keywords) {
        List<String> productNames = getProductNames();
        for (String productName : productNames) {
            String lowerName = productName.toLowerCase();
            for (String keyword : keywords) {
                if (!lowerName.contains(keyword.toLowerCase())) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<String> getProductNames() {
        List<WebElement> products = getProductItems();
        List<String> productNames = new ArrayList<>();
        for (WebElement product : products) {
            String productName = product.findElement(By.cssSelector(".product-name.product-list-name a span")).getText();
            productNames.add(productName);
        }
        return productNames;
    }
}
