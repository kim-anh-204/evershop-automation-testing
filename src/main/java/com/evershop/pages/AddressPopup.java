package com.evershop.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AddressPopup extends BasePage {

    // Locators
    private By fullNameInput = By.name("address[full_name]");
    private By telephoneInput = By.name("address[telephone]");
    private By addressInput = By.name("address[address_1]");
    private By cityInput = By.name("address[city]");
    private By countryDropdown = By.name("address[country]");
    private By provinceDropdown = By.name("address[province]");
    private By postcodeInput = By.name("address[postcode]");
    private By saveButton = By.cssSelector("button[type='submit'].button.primary");
    private By closeButton = By.linkText("Close");
    private By popupTitle = By.cssSelector("h2");
    private By popupContainer = By.cssSelector("div.bg-white.p-8");
    private By formElement = By.id("customerAddressForm");
    private By errorMessage = By.cssSelector(".error-message, .field-error, .text-danger, .pl025.text-critical");

    public AddressPopup(WebDriver driver) {
        super(driver);
    }

    public void fillFullName(String fullName) {
        waitForElement(fullNameInput).clear();
        waitForElement(fullNameInput).sendKeys(fullName);
    }

    public void fillTelephone(String telephone) {
        waitForElement(telephoneInput).clear();
        waitForElement(telephoneInput).sendKeys(telephone);
    }

    public void fillAddress(String address) {
        waitForElement(addressInput).clear();
        waitForElement(addressInput).sendKeys(address);
    }

    public void fillCity(String city) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement cityField = wait.until(ExpectedConditions.visibilityOfElementLocated(cityInput));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", cityField);
        wait.until(ExpectedConditions.elementToBeClickable(cityField));
        cityField.clear();
        cityField.sendKeys(city);
    }


    public void selectCountry(String country) {
        Select countrySelect = new Select(waitForElement(countryDropdown));
        countrySelect.selectByVisibleText(country);
        sleep(1000); // Wait for province to load
    }

    public void selectProvince(String province) {
        Select provinceSelect = new Select(waitForElement(provinceDropdown));
        provinceSelect.selectByVisibleText(province);
    }

    public void fillPostcode(String postcode) {
        waitForElement(postcodeInput).clear();
        waitForElement(postcodeInput).sendKeys(postcode);
    }

    public void fillAddressForm(String fullName, String telephone, String address,
                                String city, String country, String province, String postcode) {
        fillFullName(fullName);
        fillTelephone(telephone);
        fillAddress(address);
        fillCity(city);
        selectCountry(country);
        selectProvince(province);
        fillPostcode(postcode);
    }


    /**
     * Setup toast capture mechanism BEFORE clicking save
     * This injects JavaScript to monitor DOM changes
     */
    /**
     * Setup toast capture mechanism BEFORE clicking save
     * Use Java text block to avoid string concat/escape issues
     */
// AddressPopup.java — thêm/ thay thế các phương thức liên quan đến toast

    /**
     * Setup toast observer: always disconnect previous observer and reset flags first.
     */
    public void setupToastCapture() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script =
                "try {" +
                        "  if (window.toastObserver) { try { window.toastObserver.disconnect(); } catch(e){} window.toastObserver = null; }" +
                        "  window.capturedToast = null;" +
                        "  window.toastCaptured = false;" +
                        "  var selectors = ['.Toastify__toast', '.Toastify__toast-body', '[role=\"alert\"]', '.toast', '.notification', '.alert-success', '[aria-live=\"polite\"]', '[aria-live=\"assertive\"]'];" +
                        "  var observer = new MutationObserver(function(mutations) {" +
                        "    if (window.toastCaptured) return;" +
                        "    mutations.forEach(function(mutation) {" +
                        "      mutation.addedNodes.forEach(function(node) {" +
                        "        if (node.nodeType !== 1) return;" +
                        "        for (var i=0;i<selectors.length;i++) {" +
                        "          var sel = selectors[i];" +
                        "          try {" +
                        "            var found = node.matches && node.matches(sel) ? node : (node.querySelector ? node.querySelector(sel) : null);" +
                        "            if (found) { window.capturedToast = (found.textContent||found.innerText||'').trim(); window.toastCaptured = true; console.log('Toast captured:', window.capturedToast); break; }" +
                        "          } catch(e) { }" +
                        "        }" +
                        "      });" +
                        "    });" +
                        "  });" +
                        "  window.toastObserver = observer; observer.observe(document.body, { childList: true, subtree: true });" +
                        "  return true;" +
                        "} catch(e) { return 'ERROR:' + (e && e.message ? e.message : e); }";
        Object res = js.executeScript(script);
        System.out.println("setupToastCapture executeScript returned: " + res);
    }

    /**
     * Wait for the captured toast to appear (returns message or null)
     */
    public String waitForCapturedToast(int timeoutSeconds) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        try {
            Boolean ready = wait.until(d -> {
                Object res = js.executeScript("return (!!window.toastCaptured) || (!!window.capturedToast);");
                return Boolean.TRUE.equals(res);
            });
            if (Boolean.TRUE.equals(ready)) {
                Object cap = js.executeScript(
                        "var t = window.capturedToast || null;" +
                                "try { if (window.toastObserver) { window.toastObserver.disconnect(); } } catch(e) {}" +
                                "window.toastObserver = null; window.capturedToast = null; window.toastCaptured = false;" +
                                "return t;");
                return cap != null ? cap.toString().trim() : null;
            }
        } catch (Exception e) {
            // timeout or js error
        }
        return null;
    }

    /**
     * Click Save and wait for toast. Returns toast text or null if timeout.
     */
    public String clickSaveAndWaitToast(int timeoutSeconds) {
        setupToastCapture();
        waitForClickable(saveButton).click();
        return waitForCapturedToast(timeoutSeconds);
    }

    /**
     * Enhanced clickSave with toast capture support
     */
    public void clickSave() {
        try {
            setupToastCapture();
        } catch (Exception e) {
            // nếu setup toast capture lỗi, ghi log nhưng vẫn cố click để test không dừng ngay
            System.err.println("setupToastCapture failed: " + e.getMessage());
        }
        waitForClickable(saveButton).click();
        // đợi toast được set hoặc chờ popup thay đổi: tăng robust
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            // ưu tiên chờ toast (window.toastCaptured true) bằng polling JS:
            wait.until(d -> {
                try {
                    Object val = ((JavascriptExecutor) d).executeScript("return window.toastCaptured === true || !!window.capturedToast;");
                    return Boolean.TRUE.equals(val);
                } catch (Exception ex) {
                    return false;
                }
            });
        } catch (Exception ignored) {
        }

        // thêm sleep ngắn nếu cần (tùy app)
        sleep(500);
    }


    // --- thêm 2 locator hỗ trợ (nếu muốn cụ thể) ---
    private By allFieldErrors = By.cssSelector(".pl025.text-critical, .field-error, .error-message, .text-danger, .text-critical");

    public List<String> getAllErrorMessages() {
        List<String> texts = new ArrayList<>();
        try {
            WebElement popup = getPopupElement();
            List<WebElement> elems;
            if (popup != null) {
                elems = popup.findElements(allFieldErrors);
            } else {
                elems = driver.findElements(allFieldErrors);
            }
            for (WebElement e : elems) {
                try {
                    String t = e.getText();
                    if (t != null) {
                        t = t.trim();
                        if (!t.isEmpty()) texts.add(t);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            // ignore
        }
        return texts;
    }

    /**
     * Wait until at least expectedCount error elements are visible (or timeout)
     */
    public boolean waitForAtLeastErrorCount(int expectedCount, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return wait.until(d -> {
                List<String> msgs = getAllErrorMessages();
                return msgs.size() >= expectedCount;
            });
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wait until any field error appears (useful fallback)
     */
    public boolean waitForAnyFieldError(int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return wait.until(d -> {
                List<String> msgs = getAllErrorMessages();
                return msgs.size() > 0;
            });
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Get the captured toast message
     * @return Toast message or null if not found
     */
    public String getCapturedToast() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object captured = js.executeScript("return window.capturedToast;");

            // Cleanup observer
            try {
                js.executeScript("if (window.toastObserver) { window.toastObserver.disconnect(); }");
            } catch (Exception ignored) {}

            return captured != null ? captured.toString().trim() : null;
        } catch (Exception e) {
            System.out.println("Error getting captured toast: " + e.getMessage());
            return null;
        }
    }

    public void clickClose() {
        waitForClickable(closeButton).click();
        sleep(1000);
    }

    public String getPopupTitle() {
        return waitForElement(popupTitle).getText();
    }

    public String getErrorMessage() {
        try {
            return waitForElement(errorMessage).getText();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isPopupDisplayed() {
        try {
            waitForElement(popupTitle);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wait for popup to close/disappear
     * @param timeoutSeconds Maximum time to wait
     * @return true if popup closed, false if still visible
     */
    public boolean waitForPopupToClose(int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(popupContainer));
            return true;
        } catch (Exception e) {
            System.out.println("Popup did not close within " + timeoutSeconds + " seconds");
            return false;
        }
    }

    /**
     * Get the popup container element (useful for checking visibility)
     */
    public WebElement getPopupElement() {
        try {
            return driver.findElement(popupContainer);
        } catch (Exception e) {
            return null;
        }
    }
}