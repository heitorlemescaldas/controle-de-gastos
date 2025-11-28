package br.ifsp.demo.ui.tests;

import br.ifsp.demo.ui.pages.LoginPage;
import br.ifsp.demo.ui.pages.RegisterPage;
import br.ifsp.demo.ui.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import static org.assertj.core.api.Assertions.assertThat;

public class RegisterTest extends BaseTest {

    private final String VALID_NAME = "Test";
    private final String VALID_LASTNAME = "User";
    private final String VALID_PASSWORD = "senha123";

    private final String BACKEND_BASE = "http://localhost:8080";

    private String testEmail;

    @BeforeEach
    void prepareUniqueEmail() {
        testEmail = "register.test+" + System.currentTimeMillis() + "@example.com";
    }

    private String getToastText() {
        By toast = By.xpath("//li[@role='status']");
        waitForVisibility(toast);
        return driver.findElement(toast).getText();
    }

    @Test
    void testSuccessfulRegister() {
        RegisterPage registerPage = new RegisterPage(driver, wait);
        driver.get(BASE_URL + "/register");

        registerPage.performRegister(VALID_NAME, VALID_LASTNAME, testEmail, VALID_PASSWORD);

        waitForUrlContains("/login");

        String toast = registerPage.getSuccessToastMessagePart("Registro realizado");
        assertThat(toast).isNotEmpty();
    }

    @Test
    void testRegisterWithEmptyFieldsShowsError() {
        RegisterPage registerPage = new RegisterPage(driver, wait);
        driver.get(BASE_URL + "/register");

        registerPage.performRegister("", "", "", "");

        String toast = getToastText();

        System.out.println("Toast Message: " + toast);

        assertThat(toast).as("Deve exibir toast de falha ao registrar com campos vazios")
                .containsIgnoringCase("falha")
                .containsIgnoringCase("registro");
        assertThat(driver.getCurrentUrl()).contains("/register");
    }

    @Test
    void testDuplicateRegisterShowsError() {
        String uniqueEmail = "duplicate.test+" + System.currentTimeMillis() + "@example.com";

        RegisterPage registerPage = new RegisterPage(driver, wait);
        driver.get(BASE_URL + "/register");

        registerPage.performRegister(VALID_NAME, VALID_LASTNAME, uniqueEmail, VALID_PASSWORD);

        waitForUrlContains("/login");

        driver.get(BASE_URL + "/register");
        registerPage.performRegister(VALID_NAME, VALID_LASTNAME, uniqueEmail, VALID_PASSWORD);

        String toast = getToastText();

        assertThat(toast).as("Segunda tentativa com mesmo email deve falhar")
                .containsIgnoringCase("falha")
                .containsIgnoringCase("registro");
        assertThat(driver.getCurrentUrl()).contains("/register");
    }

    @Test
    void testRegisterThenLogin() {
        RegisterPage registerPage = new RegisterPage(driver, wait);
        driver.get(BASE_URL + "/register");

        registerPage.performRegister(VALID_NAME, VALID_LASTNAME, testEmail, VALID_PASSWORD);
        waitForUrlContains("/login");

        String token = (String) ((JavascriptExecutor) driver).executeScript("return window.localStorage.getItem('auth_token');");
        assertThat(token).isNull();

        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.performLogin(testEmail, VALID_PASSWORD);

        loginPage.waitForSuccessfulLogin(BASE_URL + "/");
        assertThat(driver.getCurrentUrl()).endsWith("/");

        String tokenAfter = (String) ((JavascriptExecutor) driver).executeScript("return window.localStorage.getItem('auth_token');");
        assertThat(tokenAfter).as("auth_token deve existir no localStorage após login").isNotNull().isNotEmpty();

        String successToast = loginPage.getSuccessToastMessagePart("Login");
        assertThat(successToast).isNotEmpty();
    }
}