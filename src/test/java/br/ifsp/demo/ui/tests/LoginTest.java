package br.ifsp.demo.ui.tests;

import br.ifsp.demo.ui.pages.LoginPage;
import br.ifsp.demo.ui.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.github.javafaker.Faker; // 💡 Import do Faker

import static org.assertj.core.api.Assertions.assertThat;

public class LoginTest extends BaseTest {

    private final String VALID_EMAIL = "teste@gmail.com";
    private final String VALID_PASSWORD = "testando";
    private final String INVALID_PASSWORD = "wrongpassword";
    private final String BACKEND_BASE = "http://localhost:8080";

    private final Faker faker = new Faker();

    @BeforeEach
    void ensureAdminExists() {
        try {
            URL url = new URL(BACKEND_BASE + "/api/v1/register");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);

            String payload = String.format(
                    "{\"name\":\"%s\",\"lastname\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                    "Admin", "Admin", VALID_EMAIL, VALID_PASSWORD
            );

            try (OutputStream os = con.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int code = con.getResponseCode();
            // 201 Created (Novo usuário) ou 409 Conflict (Usuário já existe) são aceitáveis
            if (code != 201 && code != 409) {
                throw new RuntimeException("Falha ao criar admin de teste, HTTP " + code);
            }
        } catch (Exception e) {
            // Este try/catch é robusto e deve permanecer
            throw new RuntimeException("Erro ao garantir usuário admin para testes: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // TESTES DE SUCESSO E ESTADO
    // -------------------------------------------------------------------------

    @Tag("UiTest")
    @Test
    void testSuccessfulLogin() {
        LoginPage loginPage = new LoginPage(driver, wait);

        loginPage.performLogin(VALID_EMAIL, VALID_PASSWORD);

        loginPage.waitForSuccessfulLogin(BASE_URL + "/");

        assertThat(driver.getCurrentUrl())
                .as("Verificar se a navegação após login é para a URL esperada")
                .endsWith("/");

        String successToastMessage = loginPage.getSuccessToastMessagePart("Login");

        assertThat(successToastMessage).as("Deve exibir toast de sucesso após o login").isNotEmpty();
    }

    @Tag("UiTest")
    @Test
    void testTokenStoredAfterLogin() {
        LoginPage loginPage = new LoginPage(driver, wait);

        loginPage.performLogin(VALID_EMAIL, VALID_PASSWORD);
        loginPage.waitForSuccessfulLogin(BASE_URL + "/");

        String token = (String) ((JavascriptExecutor) driver).executeScript("return window.localStorage.getItem('auth_token');");

        assertThat(token).as("auth_token deve existir no localStorage após login").isNotNull().isNotEmpty();
    }

    @Tag("UiTest")
    @Test
    void testLogoutClearsTokenAndRedirects() {
        LoginPage loginPage = new LoginPage(driver, wait);

        loginPage.performLogin(VALID_EMAIL, VALID_PASSWORD);
        loginPage.waitForSuccessfulLogin(BASE_URL + "/");

        String tokenBefore = (String) ((JavascriptExecutor) driver).executeScript("return window.localStorage.getItem('auth_token');");
        assertThat(tokenBefore).as("Token deve existir antes do logout").isNotNull();

        driver.findElement(By.xpath("//button[text()='Sair']")).click();

        waitForUrlContains("/login"); // Espera o redirecionamento

        String tokenAfter = (String) ((JavascriptExecutor) driver).executeScript("return window.localStorage.getItem('auth_token');");
        assertThat(tokenAfter).as("auth_token deve ser removido após logout").isNull();
        assertThat(driver.getCurrentUrl()).contains("/login");
    }

    @Tag("UiTest")
    @Test
    void testTrimmedEmailLogin() {
        LoginPage loginPage = new LoginPage(driver, wait);

        loginPage.performLogin("  " + VALID_EMAIL + "  ", VALID_PASSWORD);
        loginPage.waitForSuccessfulLogin(BASE_URL + "/");

        String token = (String) ((JavascriptExecutor) driver).executeScript("return window.localStorage.getItem('auth_token');");
        assertThat(token).as("Login com email com espaços deve ser bem-sucedido (trimming)").isNotNull();
    }

    // -------------------------------------------------------------------------
    // TESTES DE FALHA (Com Dados Válidos, Inválidos e Inexistentes)
    // -------------------------------------------------------------------------

    @Tag("UiTest")
    @Test
    void testLoginWithInvalidPassword() {
        LoginPage loginPage = new LoginPage(driver, wait);

        loginPage.performLogin(VALID_EMAIL, INVALID_PASSWORD);

        String errorMessage = loginPage.getErrorToastMessage();

        assertThat(errorMessage)
                .as("Verificar a mensagem de erro no Toast para senha inválida")
                .containsIgnoringCase("Falha no login")
                .contains("HTTP 401");

        assertThat(driver.getCurrentUrl()).contains("/login");
    }

    @Tag("UiTest")
    @Test
    void testLoginWithEmptyCredentials() {
        LoginPage loginPage = new LoginPage(driver, wait);

        loginPage.performLogin("", "");

        String errorMessage = loginPage.getErrorToastMessage();

        assertThat(errorMessage)
                .as("Verificar a mensagem de erro no Toast para campos vazios")
                .containsIgnoringCase("Falha no login");

        assertThat(driver.getCurrentUrl()).contains("/login");
    }

    @Tag("UiTest")
    @Test
    void testLoginWithNonExistentUser() {
        LoginPage loginPage = new LoginPage(driver, wait);

        String nonExistentEmail = faker.internet().emailAddress();
        String fakePassword = faker.internet().password();

        loginPage.performLogin(nonExistentEmail, fakePassword);

        String errorMessage = loginPage.getErrorToastMessage();

        assertThat(errorMessage)
                .as("Deve falhar o login com usuário inexistente")
                .containsIgnoringCase("Falha no login");

        assertThat(driver.getCurrentUrl()).contains("/login");
    }
}