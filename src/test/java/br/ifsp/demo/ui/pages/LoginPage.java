package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;

public class LoginPage extends BasePage {

    private final By emailInput = By.xpath("//input[@placeholder='seu@email.com']");
    private final By passwordInput = By.xpath("//input[@placeholder='••••••••']");
    private final By loginButton = By.xpath("//button[text()='Entrar']");
    private final By toastError = By.xpath("//li[@role='status']");


    public LoginPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
        driver.get(Objects.requireNonNull(driver.getCurrentUrl()));
    }

    public void waitForSuccessfulLogin(String url) {
        waitForUrlToBe(url);
    }

    public void performLogin(String email, String password) {
        type(emailInput, email);
        type(passwordInput, password);
        click(loginButton);
    }

    public String getErrorToastMessage() {
        WebDriverWait waitLocal = new WebDriverWait(driver, Duration.ofSeconds(5));
        waitLocal.until(ExpectedConditions.presenceOfElementLocated(toastError));
        return driver.findElement(toastError).getText();
    }

    public String getSuccessToastMessagePart(String expectedPart) {
        return waitForToastContaining(expectedPart, 5);
    }
}