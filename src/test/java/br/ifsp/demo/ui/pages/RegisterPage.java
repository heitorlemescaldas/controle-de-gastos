package br.ifsp.demo.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RegisterPage extends BasePage {

    private final By nameInput = By.xpath("//label[text()='Nome']/following::input[1]");
    private final By lastnameInput = By.xpath("//label[text()='Sobrenome']/following::input[1]");
    private final By emailInput = By.xpath("//label[text()='Email']/following::input[1]");
    private final By passwordInput = By.xpath("//label[text()='Senha']/following::input[1]");
    private final By registerButton = By.xpath("//button[text()='Registrar']");

    public RegisterPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void open() {
        driver.get(driver.getCurrentUrl().replaceAll("/.*$", "") + "/register");
    }

    public void performRegister(String name, String lastname, String email, String password) {
        type(nameInput, name);
        type(lastnameInput, lastname);
        type(emailInput, email);
        type(passwordInput, password);
        click(registerButton);
    }

    public String getSuccessToastMessagePart(String expectedPart) {
        return waitForToastContaining(expectedPart, 5);
    }
}