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

@DisplayName("GoalsTest")
public class GoalsTest extends BaseTest {

    private final String VALID_EMAIL = "teste@gmail.com";
    private final String VALID_PASSWORD = "senhaTeste";
    private final Faker faker = new Faker();
    private HomePage homePage;
    private final String BACKEND_BASE = "http://localhost:8080";

    private void waitForSuccess() {
        By toastLocator = By.xpath("//li[@role='status']");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(toastLocator));
            Thread.sleep(1200);
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
    @DisplayName("Teste create goal successfully")
    @Tag("UiTest")
    void testCreateGoalSuccessfully() {
        String categoryName = faker.commerce().productName();
        String month = "2025-11";
        String limit = "500,00";

        homePage.createRootCategory(categoryName);
        waitForSuccess();

        homePage.createGoal(categoryName, month, limit);
        waitForSuccess();

        String limitText = homePage.getDisplayedGoalLimit();
        assertThat(limitText).contains("R$ 500,00");
    }

}
