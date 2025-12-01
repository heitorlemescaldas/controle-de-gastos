package br.ifsp.demo.controller;

import br.ifsp.demo.controller.ExpenseController.CreateExpenseRequest;
import br.ifsp.demo.domain.model.CategoryNode;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.security.auth.AuthRequest;
import br.ifsp.demo.security.auth.RegisterUserRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class ReportControllerTest {

    @LocalServerPort
    private int port;

    private Faker faker;
    private ObjectMapper mapper;
    private String baseUrl;
    private String token;
    private RegisterUserRequest registered;
    private CategoryNode category;

    @BeforeAll
    void setupAll() throws Exception {

        faker = new Faker(new Locale("pt", "BR"));

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        baseUrl = "http://localhost:" + port + "/api/v1";

        registered = new RegisterUserRequest(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                "12345678"
        );

        given()
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(registered))
                .when()
                .post(baseUrl + "/register")
                .then()
                .statusCode(201);

        AuthRequest login = new AuthRequest(registered.email(), "12345678");

        token = given()
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(login))
                .when()
                .post(baseUrl + "/authenticate")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

        CategoryController.CreateCategoryRequest categoryRequest =
                new CategoryController.CreateCategoryRequest(faker.book().title());
        String categoryJson = mapper.writeValueAsString(categoryRequest);

        category = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .header("X-User", registered.email())
                .body(categoryJson)
                .when()
                .post(baseUrl + "/categories")
                .then()
                .statusCode(201)
                .extract().body().as(CategoryNode.class);
    }

    @BeforeEach
    void setupEach() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/reports";
    }

    private void createExpense(BigDecimal amount, ExpenseType type, Instant timestamp) throws Exception {

        var req = new CreateExpenseRequest(
                amount,
                type,
                "Teste",
                timestamp,
                category.id()
        );

        given()
                .header("Authorization", "Bearer " + token)
                .header("X-User", registered.email())
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(req))
                .when()
                .post(baseUrl + "/expenses")
                .then()
                .statusCode(201);
    }


    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Should return 200 and calculate totals correctly")
    void shouldReturn200AndCalculateTotalsCorrectly() throws Exception {
        Instant referenceDate = Instant.now().plusSeconds(100000);

        createExpense(BigDecimal.valueOf(100), ExpenseType.DEBIT, referenceDate);
        createExpense(BigDecimal.valueOf(50), ExpenseType.CREDIT, referenceDate);

        given()
                .header("Authorization", "Bearer " + token)
                .header("X-User", registered.email())
                .queryParam("start", referenceDate.minusSeconds(3600).toString())
                .queryParam("end", referenceDate.plusSeconds(3600).toString())
                .when()
                .get("/period")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("userId", is(registered.email()))
                .body("totalDebit", is(100))
                .body("totalCredit", is(50))
                .body("balance", is(-50));
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Report by period without JWT should return 401")
    void reportByPeriodWithoutJwtShouldReturn401() {
        given()
                .header("X-User", registered.email())
                .queryParam("start", Instant.now().toString())
                .queryParam("end", Instant.now().toString())
                .when()
                .get("/period")
                .then()
                .statusCode(401);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Report by period without X-User header should return 400")
    void reportByPeriodWithoutXUserHeaderShouldReturn400() {
        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("start", Instant.now().toString())
                .queryParam("end", Instant.now().toString())
                .when()
                .get("/period")
                .then()
                .statusCode(400);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Report by period with invalid dates should return 400")
    void reportByPeriodWithInvalidDatesShouldReturn400() {
        given()
                .header("Authorization", "Bearer " + token)
                .header("X-User", registered.email())
                .queryParam("start", "invalid")
                .queryParam("end", "2025-01-01T00:00:00Z")
                .when()
                .get("/period")
                .then()
                .statusCode(400);
    }


    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Category tree report should return 200")
    void categoryTreeReportShouldReturn200() throws Exception {
        Instant now = Instant.now();

        createExpense(BigDecimal.valueOf(40), ExpenseType.DEBIT, now);
        createExpense(BigDecimal.valueOf(10), ExpenseType.CREDIT, now);

        given()
                .header("Authorization", "Bearer " + token)
                .header("X-User", registered.email())
                .queryParam("start", now.minusSeconds(3600).toString())
                .queryParam("end", now.plusSeconds(3600).toString())
                .queryParam("rootCategoryId", category.id())
                .when()
                .get("/category-tree")
                .then()
                .statusCode(200)
                .body("totalDebit", is(40))
                .body("totalCredit", is(10));
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Category tree report without JWT should return 401")
    void categoryTreeReportWithoutJwtShouldReturn401() {
        given()
                .header("X-User", registered.email())
                .queryParam("start", Instant.now().toString())
                .queryParam("end", Instant.now().toString())
                .queryParam("rootCategoryId", category.id())
                .when()
                .get("/category-tree")
                .then()
                .statusCode(401);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Category tree report without X-User header should return 400")
    void categoryTreeReportWithoutXUserHeaderShouldReturn400() {
        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("start", Instant.now().toString())
                .queryParam("end", Instant.now().toString())
                .queryParam("rootCategoryId", category.id())
                .when()
                .get("/category-tree")
                .then()
                .statusCode(400);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Category tree report with invalid dates should return 400")
    void categoryTreeReportWithInvalidDatesShouldReturn400() {
        given()
                .header("Authorization", "Bearer " + token)
                .header("X-User", registered.email())
                .queryParam("start", "invalid")
                .queryParam("end", "invalid")
                .queryParam("rootCategoryId", category.id())
                .when()
                .get("/category-tree")
                .then()
                .statusCode(400);
    }
}