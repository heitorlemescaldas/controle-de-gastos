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

public class CategoryTest extends BaseTest {

    private final String VALID_EMAIL = "teste@gmail.com";
    private final String VALID_PASSWORD = "senhaTeste";
    private final Faker faker = new Faker();
    private HomePage homePage;
    private final String BACKEND_BASE = "http://localhost:8080";

    private void waitForSuccess() {
        By toastLocator = By.xpath("//li[@role='status']");

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(toastLocator));
            Thread.sleep(1500);
        } catch (Exception e) {}
    }

    @BeforeEach
    void setupTestEnvironment() {
        try {
            URL url = new URL(BACKEND_BASE + "/api/v1/register");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);
            String payload = String.format("{\"name\":\"Admin\",\"lastname\":\"Admin\",\"email\":\"%s\",\"password\":\"%s\"}", VALID_EMAIL, VALID_PASSWORD);
            try (OutputStream os = con.getOutputStream()) { os.write(payload.getBytes(StandardCharsets.UTF_8)); }
            con.getResponseCode();
        } catch (Exception e) {}

        if (!driver.getCurrentUrl().contains("/login")) {
            driver.get(BASE_URL + "/login");
        }

        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.performLogin(VALID_EMAIL, VALID_PASSWORD);

        waitForUrlContains("/");
        wait.until(ExpectedConditions.not(ExpectedConditions.titleIs("Login")));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Controle de Gastos')]")));

        homePage = new HomePage(driver, wait);
    }

    @Tag("UiTest")
    @DisplayName("Test create root category successfully")
    @Test
    void testCreateRootCategorySuccessfully() {
        String categoryName = faker.commerce().productName() + " " + System.currentTimeMillis();

        homePage.createRootCategory(categoryName);
        waitForSuccess();

        homePage.selectCategoryForAction(categoryName);
    }

    @Tag("UiTest")
    @DisplayName("Test create child category successfully")
    @Test
    void testCreateChildCategorySuccessfully() {
        String rootName = "Root " + System.currentTimeMillis();
        String childName = "Child " + System.currentTimeMillis();

        homePage.createRootCategory(rootName);
        waitForSuccess();

        homePage.createChildCategory(rootName, childName);
        waitForSuccess();

        homePage.selectCategoryForAction(rootName + "/" + childName);
    }

    @Tag("UiTest")
    @DisplayName("Test rename category successfully")
    @Test
    void testRenameCategorySuccessfully() {
        String originalName = "RenameMe " + System.currentTimeMillis();
        String newName = "Renamed " + System.currentTimeMillis();

        homePage.createRootCategory(originalName);
        waitForSuccess();

        homePage.selectCategoryForAction(originalName);
        homePage.renameSelectedCategory(newName);
        waitForSuccess();

        homePage.selectCategoryForAction(newName);
    }

    @Tag("UiTest")
    @DisplayName("Test delete category successfully")
    @Test
    void testDeleteCategorySuccessfully() {
        String categoryToDelete = "DeleteMe " + System.currentTimeMillis();

        homePage.createRootCategory(categoryToDelete);
        waitForSuccess();

        homePage.selectCategoryForAction(categoryToDelete);
        homePage.deleteSelectedCategory();
        waitForSuccess();

        try {
            homePage.selectCategoryForAction(categoryToDelete);
            assertThat(false).as("Categoria não deveria ser encontrada").isTrue();
        } catch (Exception e) {}
    }

    @Tag("UiTest")
    @DisplayName("Test move category successfully")
    @Test
    void testMoveCategorySuccessfully() {
        String categoryToMove = "ToMove " + System.currentTimeMillis();
        String newParent = "NewParent " + System.currentTimeMillis();

        homePage.createRootCategory(categoryToMove);
        waitForSuccess();

        homePage.createRootCategory(newParent);
        waitForSuccess();

        homePage.selectCategoryForAction(categoryToMove);
        homePage.moveSelectedCategory(newParent);
        waitForSuccess();

        homePage.selectCategoryForAction(newParent + "/" + categoryToMove);
    }
}