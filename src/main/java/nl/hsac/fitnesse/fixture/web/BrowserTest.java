package nl.hsac.fitnesse.fixture.web;

import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BrowserTest extends SlimFixture {
    static final int MAX_WAIT_SECONDS = 30;
    private static final Pattern PATTERN = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>", Pattern.CASE_INSENSITIVE);

    private SeleniumHelper seleniumHelper = getEnvironment().getSeleniumHelper();

    protected SeleniumHelper getSeleniumHelper() {
        return seleniumHelper;
    }

    public boolean open(String htmlLink) {
        String url = urlFromLink(htmlLink);
        seleniumHelper.navigate().to(url);
        return true;
    }

    public String pageTitle() {
        return seleniumHelper.getPageTitle();
    }

    public boolean enterAs(String value, String place) {
        return enterFor(value, place);
    }

    public boolean enterFor(String value, String place) {
        boolean result = false;
        WebElement element = getElement(place);
        if (element != null) {
            String keys = cleanupValue(value);
            element.sendKeys(keys);
            result = true;
        }
        return result;
    }

    public boolean selectAs(String value, String place) {
        return selectFor(value, place);
    }

    public boolean selectFor(String value, String place) {
        // choose option for select, if possible
        boolean result = clickSelectOption(place, value);
        if (!result) {
            // try to click the first element with right value
            result = click(value);
        }
        return result;
    }

    public boolean enterForHidden(String value, String idOrName) {
        return seleniumHelper.setHiddenInputValue(idOrName, value);
    }

    private boolean clickSelectOption(String selectPlace, String optionValue) {
        boolean result = false;
        WebElement element = getElement(selectPlace);
        if (element != null) {
            if (isSelect(element)) {
                String id = element.getAttribute("id");
                result = clickOption(id, "//select[@id='%s']//option[text()='%s']", optionValue);
                if (!result) {
                    result = clickOption(id, "//select[@id='%s']//option[contains(text(), '%s')]", optionValue);
                }
            }
        }
        return result;
    }

    private boolean clickOption(String selectId, String optionXPath, String optionValue) {
        boolean result = false;
        By optionWithText = seleniumHelper.byXpath(optionXPath, selectId, optionValue);
        WebElement option = seleniumHelper.findElement(true, optionWithText);
        if (option != null) {
            option.click();
            result = true;
        }
        return result;
    }

    public boolean click(String place) {
        boolean result = false;
        WebElement element = getElement(place);
        if (element != null) {
            element.click();
            result = true;
        }
        return result;
    }

    public String valueOf(String place) {
        return valueFor(place);
    }

    public String valueFor(String place) {
        String result = null;
        WebElement element = getElement(place);
        if (element != null) {
            if (isSelect(element)) {
                String id = element.getAttribute("id");
                By selectedOption = seleniumHelper.byXpath("//select[@id='%s']//option[@selected]", id);
                WebElement option = seleniumHelper.findElement(true, selectedOption);
                if (option != null) {
                    result = option.getText();
                }
            } else {
                result = element.getAttribute("value");
            }
        }
        return result;
    }

    private boolean isSelect(WebElement element) {
        return "select".equalsIgnoreCase(element.getTagName());
    }

    public boolean clear(String place) {
        boolean result = false;
        WebElement element = getElement(place);
        if (element != null) {
            element.clear();
            result = true;
        }
        return result;
    }

    protected WebElement getElement(String place) {
        return seleniumHelper.getElement(place);
    }

    protected Boolean waitFor(ExpectedCondition<Boolean> condition) {
        return waitDriver().until(condition).booleanValue();
    }

    private WebDriverWait waitDriver() {
        return new WebDriverWait(getSeleniumHelper().driver(), MAX_WAIT_SECONDS);
    }

    /**
     * Removes result of wiki formatting (for e.g. email addresses) if needed.
     * @param rawValue value as received from Fitnesse.
     * @return rawValue if it was just text, cleaned version if it was not.
     */
    protected String cleanupValue(String rawValue) {
        String result = null;
        Matcher matcher = PATTERN.matcher(rawValue);
        if (matcher.matches()) {
            result = matcher.group(2);
        } else {
            result = rawValue;
        }
        return result;
    }

    private String urlFromLink(String htmlLink) {
        String result = null;
        Matcher matcher = PATTERN.matcher(htmlLink);
        if (matcher.matches()) {
            result = matcher.group(1);
        }
        return result;
    }
}
