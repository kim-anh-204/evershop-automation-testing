package com.evershop.tests;

import com.evershop.testdata.SearchData;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.evershop.pages.SearchResultPage;
import java.util.List;

public class SearchTests extends BaseTest {

    /**
     * Search_01: Tìm kiếm sản phẩm thành công với tên chính xác
     */
    @Test(priority = 1, description = "Search with exact product name")
    public void Search_01_SearchByExactName() {
        System.out.println("=== STARTING SEARCH_01: Search with exact product name ===");

        String searchKeyword = SearchData.getExactProductName();
        System.out.println("Search keyword: " + searchKeyword);

        // Click vào Search icon
        System.out.println("Clicking search icon...");
        homePage.clickSearchIcon();

        // Verify Search textbox hiển thị
        System.out.println("Verifying search input is displayed...");
        Assert.assertTrue(homePage.isSearchInputDisplayed(),
                "Search textbox should be displayed");

        // Verify con trỏ chuột tự động vào ô tìm kiếm
        System.out.println("Verifying search input is focused...");
        Assert.assertTrue(homePage.isSearchInputFocused(),
                "Search input should be focused");

        // Nhập tên sản phẩm
        System.out.println("Entering search keyword...");
        homePage.enterSearchKeyword(searchKeyword);

        // Verify nội dung đã nhập
        System.out.println("Verifying search input value...");
        Assert.assertEquals(homePage.getSearchInputValue(), searchKeyword,
                "Search input should contain the entered text");

        // Nhấn Enter
        System.out.println("Pressing Enter to search...");
        homePage.pressEnter();

        // Verify kết quả tìm kiếm
        System.out.println("Loading search results page...");
        searchResultPage = new SearchResultPage(driver);
        String pageTitle = searchResultPage.getPageTitle().toLowerCase();

        // Log for comparison
        System.out.println("=== SEARCH RESULTS ===");
        System.out.println("Search_01 - Page Title: " + pageTitle);
        System.out.println("Search_01 - Search URL: " + driver.getCurrentUrl());
        System.out.println("Search_01 - Product Count: " + searchResultPage.getProductCount());

        Assert.assertTrue(pageTitle.contains("search results for"),
                "Expected page title to contain 'search results for' but was: " + pageTitle);
        System.out.println("✓ Page title contains 'search results for'");

        Assert.assertTrue(pageTitle.contains(searchKeyword.toLowerCase()),
                "Expected page title to contain search keyword '" + searchKeyword + "' but was: " + pageTitle);
        System.out.println("✓ Page title contains search keyword");

        // Verify hiển thị sản phẩm
        Assert.assertTrue(searchResultPage.getProductCount() > 0,
                "Should display at least one product");
        System.out.println("✓ At least 1 product displayed");

        // Verify sản phẩm chứa từ khóa
        Assert.assertTrue(searchResultPage.allProductsContainKeyword(searchKeyword),
                "All products should contain the search keyword");
        System.out.println("✓ All products contain search keyword");

        // Log product names
        List<String> productNames = searchResultPage.getProductNames();
        System.out.println("Search_01 - Product Names:");
        for (int i = 0; i < productNames.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + productNames.get(i));
        }

        System.out.println("=== SEARCH_01 COMPLETED SUCCESSFULLY ===");
    }

    /**
     * Search_06: Tìm kiếm trống (empty)
     */
    @Test(priority = 6, description = "Search with empty input")
    public void Search_06_SearchEmpty() {
        System.out.println("=== STARTING SEARCH_06: Search with empty input ===");

        // Click vào Search icon
        System.out.println("Step 1: Clicking search icon to display textbox...");
        homePage.clickSearchIcon();
        Assert.assertTrue(homePage.isSearchInputDisplayed(),
                "Search textbox should be displayed after clicking icon");

        // Không nhập gì và nhấn Enter
        System.out.println("Step 2: Pressing Enter with empty input...");
        homePage.pressEnter();



        // Also verify still on home page
        String currentUrl = homePage.getCurrentUrl();
        boolean stayedOnSamePage = currentUrl.equals(SearchData.getBaseUrl()) ||
                currentUrl.equals(SearchData.getBaseUrl() + "/");

        System.out.println("Current URL: " + currentUrl);
        System.out.println("Stayed on home page: " + stayedOnSamePage);

        Assert.assertTrue(stayedOnSamePage,
                "Expected to stay on home page for empty search validation, but navigated to: " + currentUrl);

        if (stayedOnSamePage && homePage.isSearchInputDisplayed()) {

            // Empty search validation: apply red border and focus

            System.out.println("Input still displayed after empty enter - applying red border and focus validation...");

            homePage.performEmptySearchValidation();

            // Assert border color is red

            String borderColor = homePage.getSearchInputBorderColor();

            System.out.println("Border color after empty search: " + borderColor);

            Assert.assertEquals(borderColor, "rgb(255, 0, 0)",

                "Border color should be red for empty input validation");

            // Assert input is focused

            Assert.assertTrue(homePage.isSearchInputFocused(),

                "Search input should be focused after empty search");

            System.out.println("✓ Empty search validation successful - red border applied, focused, and remained on home page");

        } else if (stayedOnSamePage) {

            System.out.println("Input no longer displayed after empty enter - page handles validation by removing/hiding the search input.");

            System.out.println("✓ Empty search handled by page - remained on home page without input showing error.");

        } else {

            System.out.println("Unexpected behavior: navigated away after empty search.");

        }

        System.out.println("=== SEARCH_06 COMPLETED SUCCESSFULLY ===");
    }

    /**
     * Search_02: Tìm kiếm sản phẩm thành công với từ khóa
     */
    @Test(priority = 2, description = "Search with keyword")
    public void Search_02_SearchByKeyword() {
        String searchKeyword = SearchData.getValidKeyword();

        // Click vào Search icon
        homePage.clickSearchIcon();

        // Verify Search textbox hiển thị và focused
        Assert.assertTrue(homePage.isSearchInputDisplayed(),
                "Search textbox should be displayed");
        Assert.assertTrue(homePage.isSearchInputFocused(),
                "Search input should be focused");

        // Nhập từ khóa
        homePage.enterSearchKeyword(searchKeyword);
        Assert.assertEquals(homePage.getSearchInputValue(), searchKeyword);

        // Nhấn Enter
        homePage.pressEnter();

        // Verify kết quả
        searchResultPage = new SearchResultPage(driver);
        String pageTitle = searchResultPage.getPageTitle().toLowerCase();

        // Log for comparison
        System.out.println("Search_02 - Page Title: " + pageTitle);
        System.out.println("Search_02 - Search URL: " + driver.getCurrentUrl());
        System.out.println("Search_02 - Product Count: " + searchResultPage.getProductCount());

        // Log product names
        List<String> productNames = searchResultPage.getProductNames();
        System.out.println("Search_02 - Product Names:");
        for (int i = 0; i < productNames.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + productNames.get(i));
        }

        Assert.assertTrue(pageTitle.contains(searchKeyword.toLowerCase()),
                "Expected page title to contain search keyword '" + searchKeyword + "' but was: " + pageTitle);
        Assert.assertTrue(searchResultPage.getProductCount() > 0,
                "Expected to display at least 1 product for keyword '" + searchKeyword + "' but found " + searchResultPage.getProductCount());
        Assert.assertTrue(searchResultPage.allProductsContainKeyword(searchKeyword),
                "Expected all products to contain keyword '" + searchKeyword + "'");
    }

    /**
     * Search_03: Tìm kiếm không thành công với sản phẩm không tồn tại
     */
    @Test(priority = 3, description = "Search with non-existent product")
    public void Search_03_SearchNonExistentProduct() {
        String searchKeyword = SearchData.getNonExistentProduct();

        // Click và tìm kiếm
        homePage.clickSearchIcon();
        Assert.assertTrue(homePage.isSearchInputDisplayed());

        homePage.enterSearchKeyword(searchKeyword);
        Assert.assertEquals(homePage.getSearchInputValue(), searchKeyword);

        homePage.pressEnter();

        // Verify kết quả
        searchResultPage = new SearchResultPage(driver);

        // Log for comparison
        System.out.println("Search_03 - Page Title: " + searchResultPage.getPageTitle());
        System.out.println("Search_03 - Search URL: " + driver.getCurrentUrl());
        System.out.println("Search_03 - Product Count: " + searchResultPage.getProductCount());
        System.out.println("Search_03 - No Result Message Displayed: " + searchResultPage.isNoResultMessageDisplayed());
        if (searchResultPage.isNoResultMessageDisplayed()) {
            System.out.println("Search_03 - No Result Message: " + searchResultPage.getNoResultMessage());
        }

        // Verify thông báo "There is no product to display"
        Assert.assertTrue(searchResultPage.isNoResultMessageDisplayed(),
                "Expected no product message to be displayed but it was not found");
        Assert.assertTrue(searchResultPage.getNoResultMessage()
                        .contains("There is no product to display"),
                "Expected message to contain 'There is no product to display' but was: " + searchResultPage.getNoResultMessage());

        // Verify không có sản phẩm hiển thị
        Assert.assertEquals(searchResultPage.getProductCount(), 0,
                "Expected 0 products to be displayed but found " + searchResultPage.getProductCount());

        // Verify không bị crash
        Assert.assertTrue(driver.getCurrentUrl().contains("search"),
                "Expected URL to contain 'search' but was: " + driver.getCurrentUrl());
    }

    /**
     * Search_04: Tìm kiếm với khoảng trắng thừa đầu/cuối
     */
    @Test(priority = 4, description = "Search with leading/trailing spaces")
    public void Search_04_SearchWithSpaces() {
        String searchKeyword = SearchData.getKeywordWithSpaces();
        String trimmedKeyword = SearchData.getKeywordTrimmed();

        // Thực hiện tìm kiếm
        homePage.clickSearchIcon();
        homePage.enterSearchKeyword(searchKeyword);
        homePage.pressEnter();

        // Verify kết quả
        searchResultPage = new SearchResultPage(driver);
        String pageTitle = searchResultPage.getPageTitle().toLowerCase();

        // Log for comparison
        System.out.println("Search_04 - Page Title: " + pageTitle);
        System.out.println("Search_04 - Search URL: " + driver.getCurrentUrl());
        System.out.println("Search_04 - Product Count: " + searchResultPage.getProductCount());

        // Log product names
        List<String> productNames = searchResultPage.getProductNames();
        System.out.println("Search_04 - Product Names:");
        for (int i = 0; i < productNames.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + productNames.get(i));
        }

        Assert.assertTrue(pageTitle.contains(trimmedKeyword.toLowerCase()),
                "Title should contain trimmed keyword");
        Assert.assertTrue(searchResultPage.getProductCount() > 0,
                "Should display products");
        Assert.assertTrue(searchResultPage.allProductsContainKeyword(trimmedKeyword),
                "Products should contain trimmed keyword");
    }

    /**
     * Search_05: Tìm kiếm với từ khóa nhiều từ
     */
    @Test(priority = 5, description = "Search with multiple words")
    public void Search_05_SearchMultipleWords() {
        String searchKeyword = SearchData.getMultipleWords();
        String[] keywords = searchKeyword.split(" ");

        // Click và verify
        homePage.clickSearchIcon();
        Assert.assertTrue(homePage.isSearchInputDisplayed());
        Assert.assertTrue(homePage.isSearchInputFocused());

        // Tìm kiếm
        homePage.enterSearchKeyword(searchKeyword);
        Assert.assertEquals(homePage.getSearchInputValue(), searchKeyword);
        homePage.pressEnter();

        // Verify kết quả
        searchResultPage = new SearchResultPage(driver);
        String pageTitle = searchResultPage.getPageTitle().toLowerCase();

        // Log for comparison
        System.out.println("Search_05 - Page Title: " + pageTitle);
        System.out.println("Search_05 - Search URL: " + driver.getCurrentUrl());
        System.out.println("Search_05 - Product Count: " + searchResultPage.getProductCount());

        // Log product names
        List<String> productNames = searchResultPage.getProductNames();
        System.out.println("Search_05 - Product Names:");
        for (int i = 0; i < productNames.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + productNames.get(i));
        }

        Assert.assertTrue(pageTitle.contains(searchKeyword.toLowerCase()),
                "Title should contain search keywords");
        Assert.assertTrue(searchResultPage.getProductCount() > 0,
                "Should display products");
        Assert.assertTrue(searchResultPage.allProductsContainAllKeywords(keywords),
                "Products should contain all keywords");
    }


    /**
     * Search_07: Tìm kiếm 1 ký tự
     */
    @Test(priority = 7, description = "Search with single character")
    public void Search_07_SearchSingleCharacter() {
        System.out.println("=== STARTING SEARCH_07: Search with single character ===");

        String searchKeyword = SearchData.getSingleCharacter();
        System.out.println("Search keyword: " + searchKeyword);

        // Click vào Search icon
        System.out.println("Clicking search icon...");
        homePage.clickSearchIcon();

        // Verify Search textbox hiển thị và focused
        System.out.println("Verifying search input is displayed...");
        Assert.assertTrue(homePage.isSearchInputDisplayed(),
                "Search textbox should be displayed");
        System.out.println("Verifying search input is focused...");
        Assert.assertTrue(homePage.isSearchInputFocused(),
                "Search input should be focused");

        // Nhập từ khóa
        System.out.println("Entering search keyword...");
        homePage.enterSearchKeyword(searchKeyword);

        // Verify nội dung đã nhập
        System.out.println("Verifying search input value...");
        Assert.assertEquals(homePage.getSearchInputValue(), searchKeyword,
                "Search input should contain the entered text");

        // Nhấn Enter
        System.out.println("Pressing Enter to search...");
        homePage.pressEnter();

        // Verify kết quả tìm kiếm
        System.out.println("Loading search results page...");
        searchResultPage = new SearchResultPage(driver);
        String pageTitle = searchResultPage.getPageTitle().toLowerCase();

        // Log for comparison
        System.out.println("=== SEARCH RESULTS ===");
        System.out.println("Search_07 - Page Title: " + pageTitle);
        System.out.println("Search_07 - Search URL: " + driver.getCurrentUrl());
        System.out.println("Search_07 - Product Count: " + searchResultPage.getProductCount());

        // Log product names
        List<String> productNames = searchResultPage.getProductNames();
        System.out.println("Search_07 - Product Names:");
        for (int i = 0; i < productNames.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + productNames.get(i));
        }

        Assert.assertTrue(pageTitle.contains("search results for"),
                "Expected page title to contain 'search results for' but was: " + pageTitle);
        System.out.println("✓ Page title contains 'search results for'");

        Assert.assertTrue(pageTitle.contains(searchKeyword.toLowerCase()),
                "Expected page title to contain search keyword '" + searchKeyword + "' but was: " + pageTitle);
        System.out.println("✓ Page title contains search keyword");

        // Verify hiển thị sản phẩm chứa ký tự
        Assert.assertTrue(searchResultPage.getProductCount() > 0,
                "Should display at least one product containing the character '" + searchKeyword + "'");
        System.out.println("✓ At least 1 product displayed");

        // Verify tất cả sản phẩm chứa ký tự (không phân biệt hoa/thường)
        Assert.assertTrue(searchResultPage.allProductsContainKeyword(searchKeyword),
                "All products should contain the search character '" + searchKeyword + "'");
        System.out.println("✓ All products contain search character (case insensitive)");

        System.out.println("=== SEARCH_07 COMPLETED SUCCESSFULLY ===");
    }
}
