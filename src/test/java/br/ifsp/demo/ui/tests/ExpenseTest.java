package br.ifsp.demo.ui.tests;

import br.ifsp.demo.ui.pages.HomePage;
import br.ifsp.demo.ui.pages.LoginPage;
import br.ifsp.demo.ui.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import com.github.javafaker.Faker;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExpenseTest")
public class ExpenseTest extends BaseTest {

    private final String VALID_EMAIL = "teste@gmail.com";
    private final String VALID_PASSWORD = "senhaTeste";
    private final Faker faker = new Faker();
    private HomePage homePage;
    private final String BACKEND_BASE = "http://localhost:8080";
    private final By toastLocator = By.xpath("//li[@role='status']");


    private void waitForSuccess() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(toastLocator));
            Thread.sleep(1500);
        } catch (Exception ignored) {}
    }

    @BeforeEach
    void setupTestEnvironment() {
        try {
            URL url = new URL(BACKEND_BASE + "/api/v1/register");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);
            String payload = String.format(
                    "{\"name\":\"Admin\",\"lastname\":\"Admin\",\"email\":\"%s\",\"password\":\"%s\"}",
                    VALID_EMAIL, VALID_PASSWORD
            );
            try (OutputStream os = con.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }
            con.getResponseCode();
        } catch (Exception ignored) {}

        if (!driver.getCurrentUrl().contains("/login")) {
            driver.get(BASE_URL + "/login");
        }

        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.performLogin(VALID_EMAIL, VALID_PASSWORD);

        waitForUrlContains("/");
        wait.until(ExpectedConditions.not(ExpectedConditions.titleIs("Login")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Controle de Gastos')]")
        ));

        homePage = new HomePage(driver, wait);
    }

    @Test
    @DisplayName("Test create expense successfully")
    @Tag("UiTest")
    void testCreateExpenseSuccessfully() {
        String categoryName = faker.commerce().productName();
        String description = faker.lorem().sentence(3);
        String amount = "150.50";
        String date = "30-11-2025";

        homePage.createRootCategory(categoryName);

        waitForSuccess();

        homePage.addTransaction(description, amount, "Despesa (DEBIT)", date, categoryName);
        waitForSuccess();

        String toastText = homePage.getToastText();
        assertThat(toastText).containsIgnoringCase("transação criada com sucesso");
    }

    @Test
    @DisplayName("Test create income successfully")
    @Tag("UiTest")
    void testCreateIncomeSuccessfully() {
        String categoryName = faker.commerce().productName();
        String description = faker.lorem().sentence(3);
        String amount = "2500.00";
        String date = "01-11-2025";

        homePage.createRootCategory(categoryName);
        waitForSuccess();

        homePage.addTransaction(description, amount, "Receita (CREDIT)", date, categoryName);
        waitForSuccess();

        String toastText = homePage.getToastText();
        assertThat(toastText).containsIgnoringCase("transação criada com sucesso");
    }

    @Test
    @DisplayName("Test transaction with invalid amount shows error")
    @Tag("UiTest")
    void testTransactionInvalidAmountShowsError() {
        String categoryName = faker.commerce().productName();
        String description = faker.lorem().sentence(3);
        String invalidAmount = "-10";
        String date = "30-11-2025";

        homePage.createRootCategory(categoryName);
        waitForSuccess();

        homePage.addTransaction(description, invalidAmount, "Despesa (DEBIT)", date, categoryName);
        waitForSuccess();

        String toastText = driver.findElement(toastLocator).getText().toLowerCase();
        assertThat(toastText).contains("valor deve ser maior que zero");
    }

    @Test
    @DisplayName("Test transaction without category shows error")
    @Tag("UiTest")
    void testTransactionWithoutCategoryShowsError() {
        String description = faker.lorem().sentence(3);
        String amount = "50.00";
        String date = "30-11-2025";

        homePage.addTransaction(description, amount, "Despesa (DEBIT)", date);
        waitForSuccess();

        String toastText = driver.findElement(toastLocator).getText().toLowerCase();
        assertThat(toastText).contains("selecione uma categoria");
    }

    @Test
    @DisplayName("Test transaction with invalid date shows error")
    @Tag("UiTest")
    void testTransactionInvalidDateShowsError() {
        String categoryName = faker.commerce().productName();
        homePage.createRootCategory(categoryName);
        waitForSuccess();

        String description = faker.lorem().sentence(3);
        String amount = "99.99";
        String badDate = "99-99-9999";

        homePage.addTransaction(description, amount, "Despesa (DEBIT)", badDate, categoryName);
        waitForSuccess();

        String toastText = driver.findElement(toastLocator).getText().toLowerCase();
        assertThat(toastText).contains("data inválida");
    }

    @Test
    @DisplayName("Test transaction with empty description shows error")
    @Tag("UiTest")
    void testTransactionEmptyDescriptionShowsError() {
        String categoryName = faker.commerce().productName();
        homePage.createRootCategory(categoryName);
        waitForSuccess();

        String amount = "80.00";
        String date = "20-11-2025";

        homePage.addTransaction("", amount, "Receita (CREDIT)", date, categoryName);
        waitForSuccess();

        String toastText = driver.findElement(toastLocator).getText().toLowerCase();
        assertThat(toastText).contains("descrição é obrigatória");
    }

    @Test
    @DisplayName("Test transaction with extremely large amount do not shows error")
    @Tag("UiTest")
    void testTransactionAmountOverflowShowsError() {
        String categoryName = faker.commerce().productName();
        homePage.createRootCategory(categoryName);
        waitForSuccess();

        String description = faker.lorem().sentence();
        String amount = "9999999999999999.99";
        String date = "10-11-2025";

        homePage.addTransaction(description, amount, "Despesa (DEBIT)", date, categoryName);
        waitForSuccess();

        String toastText = driver.findElement(toastLocator).getText().toLowerCase();
        assertThat(toastText).contains("transação criada com sucesso");
    }

    @Test
    @DisplayName("Test multiple transactions sequentially")
    @Tag("UiTest")
    void testMultipleTransactionsSequentially() {
        String categoryName = faker.commerce().productName();
        homePage.createRootCategory(categoryName);
        waitForSuccess();

        for (int i = 0; i < 3; i++) {
            String description = "Transação " + i;
            String amount = String.valueOf(10 * (i + 1));
            String date = "05-11-2025";
            homePage.addTransaction(description, amount, "Despesa (DEBIT)", date, categoryName);
            waitForSuccess();
        }

        String toastText = homePage.getToastText().toLowerCase();
        assertThat(toastText).contains("transação criada com sucesso");
    }

}