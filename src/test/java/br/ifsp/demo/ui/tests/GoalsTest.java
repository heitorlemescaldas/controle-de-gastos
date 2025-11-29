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
    private final By toastLocator = By.xpath("//li[@role='status']");


    private void waitForSuccess() {
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
    @DisplayName("Test create goal successfully")
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

    @Test
    @DisplayName("Test Create Goal Invalid Vaalue Shows Error")
    @Tag("UiTest")
    void testCreateGoalInvalidValueShowsError() {
        String categoryName = faker.commerce().productName();
        String month = "2025-10";
        String invalidLimit = "-100";

        homePage.createRootCategory(categoryName);
        waitForSuccess();

        homePage.createGoal(categoryName, month, invalidLimit);
        waitForSuccess();

        String toastText = driver.findElement(toastLocator).getText().toLowerCase();
        assertThat(toastText).contains("preencha categoria, mês e limite > 0");
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Test Update Goal Successfully")
    void testUpdateGoalSuccessfully() {
        String categoryName = faker.commerce().productName();
        String month = "2025-12";

        homePage.createRootCategory(categoryName);
        waitForSuccess();

        homePage.createGoal(categoryName, month, "200.00");
        waitForSuccess();

        homePage.createGoal(categoryName, month, "900.00");
        waitForSuccess();

        homePage.getDisplayedGoalLimit();

        String limitText = homePage.getDisplayedGoalLimit();

        assertThat(limitText).contains("R$ 900,00");
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Test analyzing goal status according to expenses")
    void testAnalyzingGoalStatusAccordingToExpenses() {
        String categoryName = faker.commerce().productName();
        String expense1 = faker.commerce().productName();
        String expense2 = faker.commerce().productName();
        String expense3 = faker.commerce().productName();
        String month = "2025-12";
        String dateExpense = "10-12-2025";

        homePage.createRootCategory(categoryName);
        waitForSuccess();

        homePage.createGoal(categoryName, month, "100.00");
        waitForSuccess();

        homePage.createExpense(categoryName, expense1, 99.00, dateExpense);
        waitForSuccess();

        String status1 = homePage.getGoalStatus();
        assertThat(status1).contains("Dentro da meta");

        homePage.createExpense(categoryName, expense2, 1.00, dateExpense);
        waitForSuccess();

        String status2 = homePage.getGoalStatus();
        assertThat(status2).contains("Dentro da meta");

        homePage.createExpense(categoryName, expense3, 1.00, dateExpense);
        waitForSuccess();

        String status3 = homePage.getGoalStatus();
        assertThat(status3).contains("Meta EXCEDIDA");
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Test goal status when switching months with different limits and expenses")
    void testGoalStatusSwitchingMonths() {

        String categoryName = faker.commerce().productName();
        String expenseName = faker.commerce().productName();

        String monthAug = "2025-08";
        String monthSep = "2025-09";
        String dateExpense = "09-09-2025";

        homePage.createRootCategory(categoryName);
        waitForSuccess();

        homePage.createGoal(categoryName, monthAug, "100.00");
        waitForSuccess();

        homePage.createGoal(categoryName, monthSep, "200.00");
        waitForSuccess();

        homePage.createExpense(categoryName, expenseName, 250.00, dateExpense);
        waitForSuccess();

        homePage.selectGoalMonth(monthSep);
        waitForSuccess();

        String statusSep = homePage.getGoalStatus();
        assertThat(statusSep).contains("Meta EXCEDIDA");

        homePage.selectGoalMonth(monthAug);
        waitForSuccess();

        String status = homePage.getGoalStatus();
        assertThat(status).contains("Dentro da meta");
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Test multiple goals in the same month with status changes")
    void testMultipleGoalsSameMonthStatus() {
        String category1 = faker.commerce().productName();
        String category2 = faker.commerce().productName();

        String expense1Cat1 = faker.commerce().productName();
        String expense2Cat2 = faker.commerce().productName();

        String month = "2025-12";
        String dateExpense = "15-12-2025";

        homePage.createRootCategory(category1);
        waitForSuccess();
        homePage.createRootCategory(category2);
        waitForSuccess();

        homePage.createGoal(category1, month, "100.00");
        waitForSuccess();
        homePage.createGoal(category2, month, "200.00");
        waitForSuccess();

        homePage.createExpense(category1, expense1Cat1, 50.00, dateExpense);
        waitForSuccess();
        homePage.createExpense(category2, expense2Cat2, 250.00, dateExpense);
        waitForSuccess();

        homePage.selectGoalCategory(category1);
        waitForSuccess();

        String statusCat1 = homePage.getGoalStatus();
        assertThat(statusCat1).contains("Dentro da meta");

        homePage.selectGoalCategory(category2);
        waitForSuccess();

        String statusCat2 = homePage.getGoalStatus();
        assertThat(statusCat2).contains("Meta EXCEDIDA");
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Should show error when creating goal without category")
    void testCreateGoalWithoutCategory() {
        String month = "2025-10";
        String limit = "100";

        homePage.createGoalWithoutCategory(month,limit);

        waitForSuccess();

        String msg = driver.findElement(toastLocator).getText().toLowerCase();

        assertThat(msg).contains("preencha categoria, mês e limite > 0");
    }
}
