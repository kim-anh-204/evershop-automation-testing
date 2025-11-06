package com.evershop.tests;

import com.evershop.testdata.AddressData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddAddressTest extends BaseTest {

    private static final int DEFAULT_TOAST_WAIT = 2;
    private static final int DEFAULT_LIST_WAIT = 2;

    /* -------------------------
       Helper utilities inside test
       ------------------------- */

    private String saveAndWaitToast() {
        return addressPopup.clickSaveAndWaitToast(DEFAULT_TOAST_WAIT);
    }

    private void dumpPageSnapshot(String filename) {
        try {
            String html = driver.getPageSource();
            Files.write(Paths.get(filename), html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            System.out.println("Wrote page snapshot: " + filename);
        } catch (Exception e) {
            System.out.println("Failed to write snapshot: " + e.getMessage());
        }
    }

    private void assertAddressAdded(String expectedFullName, int beforeCount) {
        // Wait until either card count increased OR robust name match appears
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_LIST_WAIT));
        try {
            wait.until(d -> {
                int now = accountPage.getAddressCardCount();
                if (now > beforeCount) return true;
                return accountPage.isAddressInListRobust(expectedFullName, 1);
            });
        } catch (Exception ignored) {
            // timeout, will assert below with final robust check
        }

        boolean finalFound = accountPage.isAddressInListRobust(expectedFullName, 3);
        if (!finalFound) {
            accountPage.dumpVisibleAddressesToConsoleAndFile("tc_add_missing_address_list.txt");
            dumpPageSnapshot("tc_add_missing_address_page.html");
        }
        Assert.assertTrue(finalFound, "New address should appear in the list (expected: " + expectedFullName + ")");
    }

    private void assertNoNewAddressAddedAfterFailure(int beforeCount) {
        int after = accountPage.getAddressCardCount();
        if (after > beforeCount) {
            // dump for debug
            accountPage.dumpVisibleAddressesToConsoleAndFile("tc_negative_unexpected_added.txt");
            dumpPageSnapshot("tc_negative_unexpected_added_page.html");
        }
        Assert.assertEquals(after, beforeCount, "Address count must remain unchanged after invalid save");
    }

    private List<String> normalizeListLower(List<String> src) {
        return src.stream().map(s -> s == null ? "" : s.toLowerCase()).collect(Collectors.toList());
    }

    /* -------------------------
       Tests (with ACC_xxx in description & method names)
       ------------------------- */

    @Test(priority = 1, description = "ACC_44 - Add Address Successfully")
    public void acc_44_addAddressSuccess() {
        System.out.println("=== START: ACC_44 - Add Address Successfully ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();
        Assert.assertTrue(addressPopup.isPopupDisplayed(), "Add address popup should be displayed");

        AddressData validData = AddressData.getValidUSAddress();
        String expectedFullName = validData.getFullName();
        System.out.println("Fill valid data: " + validData);

        int beforeCount = accountPage.getAddressCardCount();
        System.out.println("Address count before add: " + beforeCount);

        addressPopup.fillAddressForm(
                expectedFullName,
                validData.getTelephone(),
                validData.getAddress(),
                validData.getCity(),
                validData.getCountry(),
                validData.getProvince(),
                validData.getPostcode()
        );

        System.out.println("Click Save...");
        String toastMsg = saveAndWaitToast();
        System.out.println("Toast message: " + toastMsg);

        Assert.assertNotNull(toastMsg, "Toast message should be displayed");
        Assert.assertTrue(toastMsg.toLowerCase().contains("successfully"),
                "Success message should be displayed (expected 'successfully').");

        // verify address actually added to list
        assertAddressAdded(expectedFullName, beforeCount);
        System.out.println("=== END: ACC_44 ===");
    }

    @Test(priority = 2, description = "ACC_45 - Add Address Unsuccessfully - Empty All Fields")
    public void acc_45_addAddressWithEmptyFields() {
        final int WAIT_TIMEOUT = 5;          // seconds
        final List<String> EXPECTED_FIELDS = Arrays.asList(
                "full name", "telephone", "address", "city", "country", "postcode"
        );

        System.out.println("=== START: ACC_45 - Empty all fields ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        System.out.println("Click Save with no data entered...");
        addressPopup.clickSave();

        // wait for field errors and capture
        boolean any = addressPopup.waitForAnyFieldError(WAIT_TIMEOUT);
        if (!any) {
            System.out.println("Warning: no field error detected within " + WAIT_TIMEOUT + "s (will still try to collect).");
        }

        List<String> errors = addressPopup.getAllErrorMessages();
        System.out.println("Error messages found (" + errors.size() + "): " + errors);

        // ensure no new address was added
        assertNoNewAddressAddedAfterFailure(beforeCount);

        // Assertions: có ít nhất 1 lỗi và các field cần thiết
        Assert.assertFalse(errors.isEmpty(), "Expected at least one error message to be displayed");

        List<String> normalized = normalizeListLower(errors);
        for (String expectedField : EXPECTED_FIELDS) {
            boolean found = normalized.stream().anyMatch(t -> t.contains(expectedField));
            Assert.assertTrue(found, "Missing expected field error containing: '" + expectedField + "'. Actual errors: " + errors);
        }

        System.out.println("=== END: ACC_45 ===");
    }

    @Test(priority = 3, description = "ACC_46 - Add Address Unsuccessfully - Missing Full Name")
    public void acc_46_addAddressWithoutFullName() {
        System.out.println("=== START: ACC_46 - Missing Full name ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        AddressData data = AddressData.getValidUSAddress();
        System.out.println("Fill all fields except Full name...");
        addressPopup.fillTelephone(data.getTelephone());
        addressPopup.fillAddress(data.getAddress());
        addressPopup.fillCity(data.getCity());
        addressPopup.selectCountry(data.getCountry());
        addressPopup.selectProvince(data.getProvince());
        addressPopup.fillPostcode(data.getPostcode());

        System.out.println("Click Save...");
        addressPopup.clickSave();

        String errorMsg = addressPopup.getErrorMessage();
        System.out.println("Received error message: " + errorMsg);

        // no new card should be added
        assertNoNewAddressAddedAfterFailure(beforeCount);

        Assert.assertNotNull(errorMsg, "Error message should be displayed");
        Assert.assertTrue(errorMsg.contains("Full name") || errorMsg.toLowerCase().contains("required"),
                "Full name is required");
        System.out.println("=== END: ACC_46 ===");
    }

    @Test(priority = 4, description = "ACC_47 - Add Address Unsuccessfully - Full Name > 50 Characters")
    public void acc_47_addAddressWithLongFullName() {
        System.out.println("=== START: ACC_47 - Full name > 50 chars ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        AddressData data = AddressData.getAddressWithLongFullName();
        System.out.println("Long full name: " + data.getFullName());
        addressPopup.fillAddressForm(
                data.getFullName(),
                data.getTelephone(),
                data.getAddress(),
                data.getCity(),
                data.getCountry(),
                data.getProvince(),
                data.getPostcode()
        );

        System.out.println("Click Save...");
        addressPopup.clickSave();

        String errorMsg = addressPopup.getErrorMessage();
        System.out.println("Received error message: " + errorMsg);

        // ensure not added
        assertNoNewAddressAddedAfterFailure(beforeCount);

        Assert.assertNotNull(errorMsg, "Error message should be displayed");
        System.out.println("=== END: ACC_47 ===");
    }

    @Test(priority = 5, description = "ACC_48 - Add Address Unsuccessfully - Telephone < 8 Digits")
    public void acc_48_addAddressWithShortTelephone() {
        System.out.println("=== START: ACC_48 - Telephone < 8 digits ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        AddressData data = AddressData.getAddressWithShortTelephone();
        System.out.println("Enter short telephone: " + data.getTelephone());
        addressPopup.fillAddressForm(
                data.getFullName(),
                data.getTelephone(),
                data.getAddress(),
                data.getCity(),
                data.getCountry(),
                data.getProvince(),
                data.getPostcode()
        );

        addressPopup.clickSave();

        String errorMsg = addressPopup.getErrorMessage();
        System.out.println("Received error message: " + errorMsg);

        // ensure not added
        assertNoNewAddressAddedAfterFailure(beforeCount);

        Assert.assertNotNull(errorMsg, "Error message should be displayed");
        Assert.assertTrue(errorMsg.contains("Telephone") || errorMsg.toLowerCase().contains("valid"),
                "Telephone validation error should be shown");
        System.out.println("=== END: ACC_48 ===");
    }

    @Test(priority = 6, description = "ACC_49 - Add Address Unsuccessfully - Telephone > 11 Digits")
    public void acc_49_addAddressWithLongTelephone() {
        System.out.println("=== START: ACC_49 - Telephone > 11 digits ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        AddressData data = AddressData.getAddressWithLongTelephone();
        System.out.println("Enter long telephone: " + data.getTelephone());
        addressPopup.fillAddressForm(
                data.getFullName(),
                data.getTelephone(),
                data.getAddress(),
                data.getCity(),
                data.getCountry(),
                data.getProvince(),
                data.getPostcode()
        );

        addressPopup.clickSave();

        String errorMsg = addressPopup.getErrorMessage();
        System.out.println("Received error message: " + errorMsg);

        // ensure not added
        assertNoNewAddressAddedAfterFailure(beforeCount);

        Assert.assertNotNull(errorMsg, "Error message should be displayed");
        System.out.println("=== END: ACC_49 ===");
    }

    @Test(priority = 7, description = "ACC_50 - Add Address Unsuccessfully - Missing Address")
    public void acc_50_addAddressWithoutAddressField() {
        System.out.println("=== START: ACC_50 - Missing Address ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        AddressData data = AddressData.getSomeAddress(); // use a helper that returns reasonable fields (you can customize)
        addressPopup.fillAddressForm(
                data.getFullName(),
                data.getTelephone(),
                "", // leave address blank
                data.getCity(),
                data.getCountry(),
                data.getProvince(),
                data.getPostcode()
        );

        addressPopup.clickSave();

        String errorMsg = addressPopup.getErrorMessage();
        System.out.println("Received error message: " + errorMsg);

        assertNoNewAddressAddedAfterFailure(beforeCount);

        Assert.assertNotNull(errorMsg, "Error message should be displayed");
        Assert.assertTrue(errorMsg.toLowerCase().contains("address") || errorMsg.toLowerCase().contains("required"),
                "Address validation error should be shown");
        System.out.println("=== END: ACC_50 ===");
    }

    @Test(priority = 8, description = "ACC_51 - Add Address Unsuccessfully - Missing City")
    public void acc_51_addAddressWithoutCityField() {
        System.out.println("=== START: ACC_51 - Missing City ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        AddressData data = AddressData.getSomeAddress();
        addressPopup.fillAddressForm(
                data.getFullName(),
                data.getTelephone(),
                data.getAddress(),
                "", // leave city blank
                data.getCountry(),
                data.getProvince(),
                data.getPostcode()
        );

        addressPopup.clickSave();

        String errorMsg = addressPopup.getErrorMessage();
        System.out.println("Received error message: " + errorMsg);

        assertNoNewAddressAddedAfterFailure(beforeCount);

        Assert.assertNotNull(errorMsg, "Error message should be displayed");
        Assert.assertTrue(errorMsg.toLowerCase().contains("city") || errorMsg.toLowerCase().contains("required"),
                "City validation error should be shown");
        System.out.println("=== END: ACC_51 ===");
    }

    @Test(priority = 9, description = "ACC_52 - Add Address Unsuccessfully - Missing Postcode")
    public void acc_52_addAddressWithoutPostcodeField() {
        System.out.println("=== START: ACC_52 - Missing Postcode ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        AddressData data = AddressData.getSomeAddress();
        addressPopup.fillAddressForm(
                data.getFullName(),
                data.getTelephone(),
                data.getAddress(),
                data.getCity(),
                data.getCountry(),
                data.getProvince(),
                "" // leave postcode blank
        );

        addressPopup.clickSave();

        String errorMsg = addressPopup.getErrorMessage();
        System.out.println("Received error message: " + errorMsg);

        assertNoNewAddressAddedAfterFailure(beforeCount);

        Assert.assertNotNull(errorMsg, "Error message should be displayed");
        Assert.assertTrue(errorMsg.toLowerCase().contains("postcode") || errorMsg.toLowerCase().contains("required"),
                "Postcode validation error should be shown");
        System.out.println("=== END: ACC_52 ===");
    }

    @Test(priority = 10, description = "ACC_53 - Add Address Unsuccessfully - Address > 100 Characters")
    public void acc_53_addAddressOverLong() {
        System.out.println("=== START: ACC_53 - Address > 100 chars ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        AddressData data = AddressData.getAddressWithVeryLongAddress(); // implement this in testdata
        addressPopup.fillAddressForm(
                data.getFullName(),
                data.getTelephone(),
                data.getAddress(),
                data.getCity(),
                data.getCountry(),
                data.getProvince(),
                data.getPostcode()
        );

        addressPopup.clickSave();

        String errorMsg = addressPopup.getErrorMessage();
        System.out.println("Received error message: " + errorMsg);

        assertNoNewAddressAddedAfterFailure(beforeCount);

        Assert.assertNotNull(errorMsg, "Error message should be displayed");
        System.out.println("=== END: ACC_53 ===");
    }

    @Test(priority = 11, description = "ACC_54 - Add Address Unsuccessfully - City > 50 Characters")
    public void acc_54_addCityOverLong() {
        System.out.println("=== START: ACC_54 - City > 50 chars ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        AddressData data = AddressData.getAddressWithLongCity(); // implement in testdata
        addressPopup.fillAddressForm(
                data.getFullName(),
                data.getTelephone(),
                data.getAddress(),
                data.getCity(),
                data.getCountry(),
                data.getProvince(),
                data.getPostcode()
        );

        addressPopup.clickSave();

        String errorMsg = addressPopup.getErrorMessage();
        System.out.println("Received error message: " + errorMsg);

        assertNoNewAddressAddedAfterFailure(beforeCount);

        Assert.assertNotNull(errorMsg, "Error message should be displayed");
        System.out.println("=== END: ACC_54 ===");
    }

    @Test(priority = 12, description = "ACC_55 - Add Address Unsuccessfully - Postcode > 20 Characters")
    public void acc_55_addPostcodeOverLong() {
        System.out.println("=== START: ACC_55 - Postcode > 20 chars ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        AddressData data = AddressData.getAddressWithLongPostcode(); // implement in testdata
        addressPopup.fillAddressForm(
                data.getFullName(),
                data.getTelephone(),
                data.getAddress(),
                data.getCity(),
                data.getCountry(),
                data.getProvince(),
                data.getPostcode()
        );

        addressPopup.clickSave();

        String errorMsg = addressPopup.getErrorMessage();
        System.out.println("Received error message: " + errorMsg);

        assertNoNewAddressAddedAfterFailure(beforeCount);

        Assert.assertNotNull(errorMsg, "Error message should be displayed");
        System.out.println("=== END: ACC_55 ===");
    }

    @Test(priority = 15, description = "ACC_58 - Allow Editing Default Address")
    public void acc_58_editDefaultAddress() {
        System.out.println("=== START: ACC_58 - Allow editing default address ===");
        loginToAccount();
        accountPage.navigateToAccount();

        // precondition: ensure at least one address exists and one is default
        int beforeCount = accountPage.getAddressCardCount();
        Assert.assertTrue(beforeCount > 0, "Precondition: at least one address must exist");

        // ensure there is a default card (will click Make default if needed)
        WebElement defaultCardBefore = accountPage.ensureDefaultAddressExists(5);
        Assert.assertNotNull(defaultCardBefore, "Precondition: a default address card must exist (or be created)");

        // For debug: capture the default card's name (so we can re-find it later)
        String defaultFullName = "";
        try {
            WebElement nameEl = defaultCardBefore.findElement(By.cssSelector(".full-name"));
            defaultFullName = nameEl.getText().trim();
        } catch (Exception ignored) {}

        // Click Edit inside that default card
        accountPage.clickEditOnCard(defaultCardBefore);

        // modify a field (telephone) and save
        String newTelephone = "0900000000";
        addressPopup.fillTelephone(newTelephone);

        // save and wait for toast (ensure you have saveAndWaitToast helper in test)
        String toast = saveAndWaitToast();
        System.out.println("Toast after edit: " + toast);
        Assert.assertNotNull(toast, "Expected a toast after save");
        Assert.assertTrue(toast.toLowerCase().contains("updated") || toast.toLowerCase().contains("successfully"),
                "Expected update success toast (got: " + toast + ")");

        // After save the DOM might change. Re-find the default card element (do not reuse old WebElement)
        WebElement defaultCardAfter = accountPage.findDefaultAddressCard();
        Assert.assertNotNull(defaultCardAfter, "Default card should still be present after edit");

        // Wait for updated telephone to appear inside that card
        boolean updated = accountPage.waitForCardTelephoneToMatch(defaultCardAfter, newTelephone, 5);
        if (!updated) {
            // fallback: try to find by full name (if we captured it) and check telephone there
            if (!defaultFullName.isEmpty()) {
                List<WebElement> cards = driver.findElements(By.cssSelector(".address__summary"));
                for (WebElement c : cards) {
                    try {
                        String name = c.findElement(By.cssSelector(".full-name")).getText().trim();
                        if (name.equalsIgnoreCase(defaultFullName)) {
                            // wait on this card
                            updated = accountPage.waitForCardTelephoneToMatch(c, newTelephone, 3);
                            if (updated) break;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        Assert.assertTrue(updated, "Edited telephone should be visible on card (expected: " + newTelephone + ")");

        // Verify the card we edited remains default
        boolean stillDefault = accountPage.isCardDefault(defaultCardAfter);
        Assert.assertTrue(stillDefault, "Edited card should remain default");

        System.out.println("=== END: ACC_58 ===");
    }
    @Test(priority = 14, description = "ACC_57 - Add Address Unsuccessfully - Enter Only Spaces")
    public void acc_57_addAddressWithSpacesOnly() {
        System.out.println("=== START: ACC_57 - Enter only spaces ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();

        int beforeCount = accountPage.getAddressCardCount();

        // Enter only spaces
        String space = "   ";
        addressPopup.fillAddressForm(
                space,   // Full name
                space,   // Telephone
                space,   // Address
                space,   // City
                "China", // Country
                "Hunan", // Province
                space    // Postcode
        );

        System.out.println("Click Save...");
        addressPopup.clickSave();

        // Wait and get all errors displayed
        boolean any = addressPopup.waitForAnyFieldError(5);
        if (!any) {
            System.out.println("Warning: No field error detected after Save click (check selector).");
        }

        List<String> errors = addressPopup.getAllErrorMessages();
        System.out.println("Error messages found (" + errors.size() + "): " + errors);

        // No new address added
        assertNoNewAddressAddedAfterFailure(beforeCount);

        // Check required errors
        List<String> normalized = normalizeListLower(errors);
        List<String> expectedFields = Arrays.asList("full name", "telephone", "address", "city", "postcode");
        for (String field : expectedFields) {
            boolean found = normalized.stream().anyMatch(t -> t.contains(field) || t.contains("required"));
            Assert.assertTrue(found, "Missing expected error for: " + field);
        }

        // Popup remains open
        Assert.assertTrue(addressPopup.isPopupDisplayed(), "Popup should remain open after validation errors");

        System.out.println("=== END: ACC_57 ===");
    }
    @Test(priority = 13, description = "ACC_56 - Add Address Unsuccessfully - Invalid Telephone")
    public void acc_56_addAddressWithInvalidTelephone() {
        System.out.println("=== START: ACC_56 - Invalid telephone ===");
        loginToAccount();
        accountPage.navigateToAccount();
        accountPage.clickAddNewAddress();
        Assert.assertTrue(addressPopup.isPopupDisplayed(), "Add address popup should be displayed");

        int beforeCount = accountPage.getAddressCardCount();

        // Test data
        AddressData data = AddressData.getSomeAddress();
        String invalidTelephone = "abc03422113"; // or "083218337{]"

        addressPopup.fillAddressForm(
                "Kim Hoa",
                invalidTelephone,
                "12 MG Road",
                "Bengaluru",
                "India",
                "Karnataka",
                "560001"
        );

        System.out.println("Click Save...");
        addressPopup.clickSave();

        // Get displayed error message
        String errorMsg = addressPopup.getErrorMessage();
        System.out.println("Received error message: " + errorMsg);

        // No new address added
        assertNoNewAddressAddedAfterFailure(beforeCount);

        // Check error message
        Assert.assertNotNull(errorMsg, "Expected an error message when telephone is invalid");
        Assert.assertTrue(
                errorMsg.toLowerCase().contains("telephone") && errorMsg.toLowerCase().contains("invalid"),
                "Expected 'Telephone number is invalid' message, got: " + errorMsg
        );

        System.out.println("=== END: ACC_56 ===");
    }



}