package br.ifsp.demo.controller;

import br.ifsp.demo.controller.ExpenseController.CreateExpenseRequest;
import br.ifsp.demo.domain.model.CategoryNode;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.security.auth.AuthRequest;
import br.ifsp.demo.security.auth.RegisterUserRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
class ExpenseControllerTest {

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
                new CategoryController.CreateCategoryRequest(faker.beer().name());
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
        RestAssured.basePath = "/api/v1/expenses";
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Create expense with JWT should return 201")
    void createExpenseWithJwtShouldReturn201() throws Exception {
        var req = new CreateExpenseRequest(
                BigDecimal.valueOf(150.00),
                ExpenseType.DEBIT,
                "Mercado",
                Instant.now(),
                category.id()
        );

        given()
                .header("Authorization", "Bearer " + token)
                .header("X-User", registered.email())
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(req))
                .when()
                .post()
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("amount", is(150.00f))
                .body("description", is("Mercado"))
                .body("categoryId", is(category.id()))
                .body("userId", is(registered.email()));
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Create expense without category ID should return 201")
    void createExpenseWithoutCategoryIdShouldReturn201() throws Exception {
        var req = new CreateExpenseRequest(
                BigDecimal.valueOf(50.00),
                ExpenseType.CREDIT,
                "Extra Income",
                Instant.now(),
                null
        );

        given()
                .header("Authorization", "Bearer " + token)
                .header("X-User", registered.email())
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(req))
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("categoryId", nullValue());
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Create expense without JWT should return 401")
    void createExpenseWithoutJwtShouldReturn401() throws Exception {
        var req = new CreateExpenseRequest(
                BigDecimal.valueOf(100),
                ExpenseType.DEBIT,
                "Unauthorized",
                Instant.now(),
                category.id()
        );

        given()
                .header("X-User", registered.email())
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(req))
                .when()
                .post()
                .then()
                .statusCode(401);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Create expense missing X-User header should return 400")
    void createExpenseMissingXUserHeaderShouldReturn400() throws Exception {
        var req = new CreateExpenseRequest(
                BigDecimal.valueOf(100),
                ExpenseType.DEBIT,
                "No Header",
                Instant.now(),
                category.id()
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(req))
                .when()
                .post()
                .then()
                .statusCode(400);
    }
}