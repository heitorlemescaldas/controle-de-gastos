package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    protected void type(By locator, String text) {
        driver.findElement(locator).clear();
        driver.findElement(locator).sendKeys(text);
    }

    protected void click(By locator) {
        driver.findElement(locator).click();
    }

    protected void waitForUrlToBe(String expectedUrl) {
        wait.until(ExpectedConditions.urlToBe(expectedUrl));
    }

    protected void waitForVisibility(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected By toastLocator() {
        return By.xpath("//li[@role='status']");
    }


    protected String getToastText() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(toastLocator()));
        return driver.findElement(toastLocator()).getText();
    }


    protected String waitForToastContaining(String expectedParte, long timeoutSeconds) {
        WebDriverWait localWait = new WebDriverWait(driver, java.time.Duration.ofSeconds(timeoutSeconds));
        localWait.until(ExpectedConditions.textToBePresentInElementLocated(toastLocator(), expectedParte));
        return driver.findElement(toastLocator()).getText();
    }

    protected void scrollToElement(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);", element);
    }
}