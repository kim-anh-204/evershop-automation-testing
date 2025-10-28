package com.evershop.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

/**
 * AccountPage - helpers for Address Book page
 *
 * Provides:
 *  - utilities to read visible address cards
 *  - robust name matching (normalize, diacritics removed)
 *  - detection of default card (by green border class OR absence of "Make default" link)
 *  - ability to set a card default (by clicking "Make default" inside card)
 *  - edit/delete helpers scoped inside card element
 *  - toast detection (MutationObserver JS + fallback)
 *  - debug dump helpers
 *
 * Notes:
 *  - selectors are conservative / multi-pattern to tolerate small HTML changes.
 *  - methods swallow transient exceptions (StaleElementReference) and retry where reasonable.
 */
public class AccountPage extends BasePage {
    // Locators (few multi-patterns)
    private By userIcon = By.cssSelector("a[href='/account'], a[href*='/account']");
    private By addNewAddressLink = By.linkText("Add new address");
    private By editAddressLink = By.linkText("Edit");
    private By deleteAddressLink = By.linkText("Delete");
    private By makeDefaultLink = By.linkText("Make default");
    private By addressCard = By.cssSelector(".address-card, .address-item, .border.rounded");
    private By addressSummary = By.cssSelector(".address__summary");
    private By fullNameSel = By.cssSelector(".address__summary .full-name, .full-name");
    private By telephoneSel = By.cssSelector(".address__summary .telephone, .telephone");

    public AccountPage(WebDriver driver) {
        super(driver);
    }

    /* -----------------------------
       Normalization & matching
       ----------------------------- */

    private String normalizeForCompare(String s) {
        if (s == null) return "";
        String t = s.trim().replaceAll("\\s+", " ");
        String noDiacritics = Normalizer.normalize(t, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noDiacritics.toLowerCase();
    }

    /* -----------------------------
       Basic page actions
       ----------------------------- */

    public void navigateToAccount() {
        driver.get("https://demo.evershop.io/account");
    }

    public void clickUserIcon() {
        waitForClickable(userIcon).click();
        sleep(800);
    }

    public void clickAddNewAddress() {
        waitForClickable(addNewAddressLink).click();
        sleep(700);
    }

    /* -----------------------------
       Read / list helpers
       ----------------------------- */

    public int getAddressCardCount() {
        try {
            return driver.findElements(addressSummary).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getAddressCount() {
        try {
            return driver.findElements(addressCard).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public List<String> getAllVisibleFullNames() {
        List<String> names = new ArrayList<>();
        try {
            List<WebElement> els = driver.findElements(fullNameSel);
            for (WebElement e : els) {
                try {
                    if (!e.isDisplayed()) continue;
                    String t = e.getText();
                    if (t != null && !t.trim().isEmpty()) names.add(t.trim());
                } catch (StaleElementReferenceException ignored) {}
            }
        } catch (Exception ignored) {}
        return names;
    }

    /**
     * Robust "is in list" with short wait
     */
    public boolean isAddressInList(String expectedFullName, int timeoutSeconds) {
        final String expectedNorm = normalizeForCompare(expectedFullName);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Math.max(1, timeoutSeconds)));
        try {
            return wait.until(d -> {
                List<String> names = getAllVisibleFullNames();
                for (String n : names) {
                    String norm = normalizeForCompare(n);
                    if (norm.equals(expectedNorm) || norm.contains(expectedNorm) || expectedNorm.contains(norm)) {
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * More tolerant matching (token intersection etc.) used by tests when list updates are flaky.
     */
    public boolean isAddressInListRobust(String expectedFullName, int timeoutSeconds) {
        final String expectedNorm = normalizeForCompare(expectedFullName);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Math.max(1, timeoutSeconds)));
        try {
            return wait.until(d -> {
                List<WebElement> cards = d.findElements(addressSummary);
                List<String> visible = new ArrayList<>();
                for (WebElement card : cards) {
                    try {
                        WebElement nameEl = card.findElement(By.cssSelector(".full-name"));
                        if (!nameEl.isDisplayed()) continue;
                        String name = nameEl.getText().trim();
                        visible.add(name);
                        String nameNorm = normalizeForCompare(name);

                        if (nameNorm.equals(expectedNorm)) return true;
                        if (nameNorm.contains(expectedNorm) || expectedNorm.contains(nameNorm)) return true;

                        Set<String> expectedTokens = new HashSet<>(Arrays.asList(expectedNorm.split(" ")));
                        Set<String> nameTokens = new HashSet<>(Arrays.asList(nameNorm.split(" ")));
                        if (!expectedTokens.isEmpty() && nameTokens.containsAll(expectedTokens)) return true;

                        int matchCount = 0;
                        for (String tkn : expectedTokens) if (nameTokens.contains(tkn)) matchCount++;
                        if (matchCount >= Math.min(2, expectedTokens.size())) return true;
                    } catch (StaleElementReferenceException | NoSuchElementException ignored) {}
                }
                // small debug
                System.out.println("DEBUG visible names: " + visible);
                return false;
            });
        } catch (Exception e) {
            return false;
        }
    }

    /* -----------------------------
       Default card detection / manipulation
       ----------------------------- */

    /**
     * Find the default address card by:
     *  - presence of 'border-green-700' or 'default' in class on container
     *  - fallback: card that DOES NOT contain a 'Make default' link (common pattern when default hides action)
     *
     * Returns the card container WebElement or null.
     */
    public WebElement findDefaultAddressCard() {
        try {
            List<WebElement> containers = driver.findElements(addressCard);
            // 1. check class flag
            for (WebElement c : containers) {
                try {
                    String cls = c.getAttribute("class");
                    if (cls != null && (cls.contains("border-green-700") || cls.contains("default") || cls.contains("data-default"))) {
                        // ensure it's an address card (has address__summary)
                        if (!c.findElements(addressSummary).isEmpty()) return c;
                    }
                } catch (StaleElementReferenceException ignored) {}
            }
            // 2. fallback: card that has address summary but no "Make default" link inside -> likely default
            for (WebElement c : containers) {
                try {
                    if (c.findElements(addressSummary).isEmpty()) continue;
                    List<WebElement> makeLinks = c.findElements(By.xpath(".//a[contains(normalize-space(.),'Make default')]"));
                    if (makeLinks.isEmpty()) return c;
                } catch (StaleElementReferenceException ignored) {}
            }
        } catch (Exception e) {
            System.out.println("findDefaultAddressCard error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Click the Make default link inside the card at index (0-based).
     * Returns true if clicked, false otherwise.
     */
    public boolean makeCardDefaultByIndex(int index) {
        try {
            List<WebElement> containers = driver.findElements(addressCard);
            if (index < 0 || index >= containers.size()) return false;
            WebElement card = containers.get(index);
            List<WebElement> makeLinks = card.findElements(By.xpath(".//a[contains(normalize-space(.),'Make default')]"));
            if (makeLinks.isEmpty()) return false;
            makeLinks.get(0).click();
            sleep(600);
            return true;
        } catch (Exception e) {
            System.out.println("makeCardDefaultByIndex error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Click "Make default" on the first card whose name contains expectedFullName (substring, case-insensitive).
     * Returns true if action performed.
     */
    public boolean makeCardDefaultByName(String expectedFullName) {
        final String expectedNorm = normalizeForCompare(expectedFullName);
        try {
            List<WebElement> containers = driver.findElements(addressCard);
            for (WebElement c : containers) {
                try {
                    WebElement nameEl = null;
                    List<WebElement> nameEls = c.findElements(By.cssSelector(".full-name"));
                    if (!nameEls.isEmpty()) nameEl = nameEls.get(0);
                    if (nameEl == null) continue;
                    String name = nameEl.getText();
                    if (name == null) continue;
                    String norm = normalizeForCompare(name);
                    if (norm.contains(expectedNorm) || expectedNorm.contains(norm)) {
                        List<WebElement> makeLinks = c.findElements(By.xpath(".//a[contains(normalize-space(.),'Make default')]"));
                        if (!makeLinks.isEmpty()) {
                            makeLinks.get(0).click();
                            sleep(600);
                            return true;
                        }
                    }
                } catch (StaleElementReferenceException ignored) {}
            }
        } catch (Exception e) {
            System.out.println("makeCardDefaultByName error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Ensure a default exists. If none exists, try to set the first available 'Make default' and wait.
     * Returns the default card element or null if couldn't ensure.
     */
    /**
     * Ensure a default address exists. If already exist -> return it.
     * Otherwise, try to click "Make default" on the first card that has such a link,
     * then wait up to timeoutSeconds for a default to appear (class change or absence of link).
     */
    public WebElement ensureDefaultAddressExists(int timeoutSeconds) {
        // 1) already exists?
        WebElement def = findDefaultAddressCard();
        if (def != null) return def;

        // 2) find first card that has Make default link and click it
        try {
            List<WebElement> containers = driver.findElements(By.cssSelector(".border.rounded, .address-card, .address-item"));
            for (int i = 0; i < containers.size(); i++) {
                WebElement c = containers.get(i);
                try {
                    List<WebElement> makeLinks = c.findElements(By.xpath(".//a[contains(normalize-space(.),'Make default')]"));
                    if (!makeLinks.isEmpty()) {
                        // copy loop index into an effectively-final variable for use inside lambda
                        final int idx = i;
                        makeLinks.get(0).click();

                        // wait until findDefaultAddressCard returns non-null or the clicked card lost the "Make default" link
                        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Math.max(1, timeoutSeconds)));
                        boolean ok = wait.until(d -> {
                            try {
                                // re-evaluate default presence
                                WebElement found = findDefaultAddressCard();
                                if (found != null) return true;
                                // fallback: re-find the same card by index and check it no longer has the Make default link
                                List<WebElement> updatedContainers = d.findElements(By.cssSelector(".border.rounded, .address-card, .address-item"));
                                if (idx < updatedContainers.size()) {
                                    WebElement same = updatedContainers.get(idx);
                                    List<WebElement> nowMakeLinks = same.findElements(By.xpath(".//a[contains(normalize-space(.),'Make default')]"));
                                    if (nowMakeLinks.isEmpty()) return true; // link removed -> became default
                                }
                                return false;
                            } catch (Exception ex) {
                                return false;
                            }
                        });
                        if (ok) return findDefaultAddressCard();
                        else {
                            // if not ok, continue to next container (rare)
                            continue;
                        }
                    }
                } catch (StaleElementReferenceException ignored) {}
            }
        } catch (Exception e) {
            System.out.println("ensureDefaultAddressExists error: " + e.getMessage());
        }
        return null;
    }

    public boolean isCardDefault(WebElement card) {
        if (card == null) return false;
        try {
            String cls = card.getAttribute("class");
            if (cls != null && (cls.contains("border-green-700") || cls.contains("default") || cls.contains("data-default"))) return true;
            List<WebElement> makeLinks = card.findElements(By.xpath(".//a[contains(normalize-space(.),'Make default')]"));
            return makeLinks.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /* -----------------------------
       Edit / delete inside card
       ----------------------------- */

    /**
     * Click Edit link inside given card element (robust).
     */
    public void clickEditOnCard(WebElement card) {
        if (card == null) throw new IllegalArgumentException("card is null");
        try {
            List<WebElement> edits = card.findElements(By.xpath(".//a[normalize-space(.)='Edit' or contains(normalize-space(.),'Edit')]"));
            if (!edits.isEmpty()) {
                edits.get(0).click();
                sleep(600);
                return;
            }
            // fallback: global first Edit
            List<WebElement> global = driver.findElements(editAddressLink);
            if (!global.isEmpty()) {
                global.get(0).click();
                sleep(600);
                return;
            }
            throw new RuntimeException("Edit link not found");
        } catch (Exception e) {
            throw new RuntimeException("Failed to click Edit on card: " + e.getMessage(), e);
        }
    }

    public void clickEditAddress(int index) {
        List<WebElement> edits = driver.findElements(editAddressLink);
        if (!edits.isEmpty() && index >= 0 && index < edits.size()) {
            edits.get(index).click();
            sleep(700);
        }
    }

    public void clickDeleteAddress(int index) {
        List<WebElement> deletes = driver.findElements(deleteAddressLink);
        if (!deletes.isEmpty() && index >= 0 && index < deletes.size()) {
            deletes.get(index).click();
            sleep(700);
        }
    }

    public void clickMakeDefault(int index) {
        List<WebElement> makes = driver.findElements(makeDefaultLink);
        if (!makes.isEmpty() && index >= 0 && index < makes.size()) {
            makes.get(index).click();
            sleep(700);
        }
    }

    /* -----------------------------
       Card field waiters
       ----------------------------- */

    public boolean waitForCardTelephoneToMatch(WebElement card, String telephone, int timeoutSeconds) {
        if (card == null) return false;
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Math.max(1, timeoutSeconds)));
            return wait.until(d -> {
                try {
                    WebElement telEl = card.findElement(By.cssSelector(".telephone"));
                    String t = telEl.getText().trim();
                    return t.equals(telephone);
                } catch (StaleElementReferenceException | NoSuchElementException ex) {
                    return false;
                }
            });
        } catch (Exception e) {
            System.out.println("waitForCardTelephoneToMatch error: " + e.getMessage());
            return false;
        }
    }

    /* -----------------------------
       Toast detection (observer + fallbacks)
       ----------------------------- */

    public String waitForToastUsingObserver(long timeoutMillis) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String script =
                    "var timeout = arguments[0] || 5000; " +
                            "var callback = arguments[arguments.length-1]; " +
                            "var selector = '.Toastify__toast .Toastify__toast-body, .Toastify__toast--success .Toastify__toast-body, [role=\"alert\"]';" +
                            "try{ " +
                            "  var found = document.querySelector(selector); " +
                            "  if(found){ callback(found.innerText || found.textContent); return; }" +
                            "  var observer = new MutationObserver(function(muts){ " +
                            "    for(var i=0;i<muts.length;i++){ " +
                            "      var added = muts[i].addedNodes; " +
                            "      for(var j=0;j<added.length;j++){ " +
                            "        var node = added[j]; " +
                            "        if(node.nodeType===1){ " +
                            "          try{ " +
                            "            if(node.matches && node.matches(selector)) { observer.disconnect(); callback((node.innerText||node.textContent)); return; } " +
                            "            var el = node.querySelector && node.querySelector(selector); " +
                            "            if(el){ observer.disconnect(); callback((el.innerText||el.textContent)); return; } " +
                            "          } catch(e){} " +
                            "        } " +
                            "      } " +
                            "    } " +
                            "  }); " +
                            "  observer.observe(document.body, { childList: true, subtree: true }); " +
                            "  setTimeout(function(){ try{ observer.disconnect(); }catch(e){}; callback(null); }, timeout); " +
                            "} catch(e){ callback(null); }";
            Object result = js.executeAsyncScript(script, timeoutMillis);
            if (result != null) return result.toString();
        } catch (Exception e) {
            System.out.println("waitForToastUsingObserver error: " + e.getMessage());
        }
        return null;
    }

    public String waitForToastifySuccessAndGetText() {
        int perLocatorTimeoutSeconds = 2;    // each locator short wait
        int bodyScanTimeoutSeconds = 4;      // bounded body scan

        By[] locators = new By[] {
                By.cssSelector(".Toastify__toast--success .Toastify__toast-body"),
                By.cssSelector(".Toastify__toast .Toastify__toast-body"),
                By.cssSelector("[role='alert']"),
                By.cssSelector("[aria-live='polite']"),
                By.cssSelector("[aria-live='assertive']"),
                By.cssSelector(".toast, .toast-message, .notification, .notification-message, .alert-success")
        };

        System.out.println("[ToastCheck] Start looking for toast (fast mode).");

        // 1) quick visibility checks
        for (By loc : locators) {
            try {
                System.out.println("[ToastCheck] Trying locator: " + loc);
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(perLocatorTimeoutSeconds));
                WebElement el = shortWait.until(ExpectedConditions.visibilityOfElementLocated(loc));
                String text = safeGetText(el);
                if (text != null && !text.trim().isEmpty()) {
                    System.out.println("[ToastCheck] Found toast with locator: " + loc + " -> " + text.trim());
                    return text.trim();
                } else {
                    System.out.println("[ToastCheck] Element found but text empty for locator: " + loc);
                }
            } catch (TimeoutException te) {
                System.out.println("[ToastCheck] Locator timed out (no visible element): " + loc);
            } catch (Exception e) {
                System.out.println("[ToastCheck] Exception for locator " + loc + ": " + e.getMessage());
            }
        }

        // 2) presence checks (in case toast present but not visible)
        for (By loc : locators) {
            try {
                System.out.println("[ToastCheck] Trying presence for locator: " + loc);
                WebDriverWait presenceWait = new WebDriverWait(driver, Duration.ofSeconds(1));
                WebElement el = presenceWait.until(ExpectedConditions.presenceOfElementLocated(loc));
                String text = safeGetText(el);
                if (text != null && !text.trim().isEmpty()) {
                    System.out.println("[ToastCheck] Found (presence) toast: " + text.trim());
                    return text.trim();
                } else {
                    System.out.println("[ToastCheck] Presence found but no text: " + loc);
                }
            } catch (TimeoutException te) {
                // ignore
            } catch (Exception e) {
                System.out.println("[ToastCheck] Exception during presence check " + loc + ": " + e.getMessage());
            }
        }

        // 3) bounded body scan
        System.out.println("[ToastCheck] Scanning body text for keywords (up to " + bodyScanTimeoutSeconds + "s)");
        String[] keywords = new String[] {"success", "successfully", "thành công", "đã thêm", "saved"};
        long end = System.currentTimeMillis() + (bodyScanTimeoutSeconds * 1000L);
        while (System.currentTimeMillis() < end) {
            try {
                String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
                for (String k : keywords) {
                    if (body.contains(k)) {
                        int idx = body.indexOf(k);
                        int start = Math.max(0, idx - 40);
                        int finish = Math.min(body.length(), idx + 80);
                        String snippet = driver.findElement(By.tagName("body")).getText().substring(start, finish).trim();
                        System.out.println("[ToastCheck] Found keyword in body: " + snippet);
                        return snippet;
                    }
                }
            } catch (Exception e) {
                System.out.println("[ToastCheck] Exception scanning body: " + e.getMessage());
            }
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        }

        System.out.println("[ToastCheck] No toast found (fast mode).");
        return null;
    }

    private String safeGetText(WebElement el) {
        try {
            String t = el.getText();
            if (t != null && !t.trim().isEmpty()) return t;
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object inner = js.executeScript("return arguments[0].innerText", el);
            return inner != null ? inner.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getToastMessage() {
        try {
            String obs = waitForToastUsingObserver(5000);
            if (obs != null && !obs.trim().isEmpty()) return obs.trim();
            return waitForToastifySuccessAndGetText();
        } catch (Exception e) {
            System.out.println("getToastMessage error: " + e.getMessage());
            return null;
        }
    }

    /* -----------------------------
       Debug helpers
       ----------------------------- */

    public void dumpVisibleAddressesToConsoleAndFile(String filename) {
        List<String> names = getAllVisibleFullNames();
        System.out.println("Visible addresses (" + names.size() + "):");
        for (int i = 0; i < names.size(); i++) {
            System.out.println(i + ": '" + names.get(i) + "'");
        }
        try {
            Files.write(Paths.get(filename), driver.getPageSource().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            System.out.println("Wrote page snapshot: " + filename);
        } catch (IOException e) {
            System.out.println("Failed to write snapshot: " + e.getMessage());
        }
    }

    /* -----------------------------
       Small util
       ----------------------------- */

    // sleep helper reusing BasePage.sleep if exists
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
