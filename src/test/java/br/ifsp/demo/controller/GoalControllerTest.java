package br.ifsp.demo.controller;

import br.ifsp.demo.controller.GoalController.SetGoalRequest;
import br.ifsp.demo.domain.model.CategoryNode;
import br.ifsp.demo.security.auth.AuthRequest;
import br.ifsp.demo.security.auth.RegisterUserRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Locale;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GoalControllerTest {

    @LocalServerPort
    private int port;

    private Faker faker;
    private ObjectMapper objectMapper;
    private String apiBase;

    private String jwtToken;
    private String registeredUserId;
    private RegisterUserRequest registeredUser;

    private CategoryNode category;

    @BeforeAll
    public void setupAll() throws JsonProcessingException {
        faker = new Faker(new Locale("pt", "BR"));
        objectMapper = new ObjectMapper();

        apiBase = "http://localhost:" + port + "/api/v1";

        // register user
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 12, true, true);
        registeredUser = new RegisterUserRequest(faker.name().firstName(), faker.name().lastName(), email, password);

        String registerJson = objectMapper.writeValueAsString(registeredUser);

        registeredUserId = given()
                .contentType(ContentType.JSON)
                .body(registerJson)
                .when()
                .post(apiBase + "/register")
                .then()
                .statusCode(201)
                .extract().path("id");

        // authenticate
        AuthRequest authRequest = new AuthRequest(email, password);
        String authJson = objectMapper.writeValueAsString(authRequest);

        jwtToken = given()
                .contentType(ContentType.JSON)
                .body(authJson)
                .when()
                .post(apiBase + "/authenticate")
                .then()
                .statusCode(200)
                .extract().path("token");

        // create root category
        CategoryController.CreateCategoryRequest categoryRequest =
                new CategoryController.CreateCategoryRequest(faker.book().title());
        String categoryJson = objectMapper.writeValueAsString(categoryRequest);

        category = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .body(categoryJson)
                .when()
                .post(apiBase + "/categories")
                .then()
                .statusCode(201)
                .extract().body().as(CategoryNode.class);
    }

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/goals";
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Create goal with JWT should return 201")
    public void createGoalWithJwtShouldReturn201() throws JsonProcessingException {
        String rootCategoryId = category.id();
        String month = YearMonth.now().plusMonths(1).toString();
        BigDecimal limit = BigDecimal.valueOf(500.00);

        SetGoalRequest requestBody = new SetGoalRequest(rootCategoryId, month, limit);
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("userId", is(registeredUser.email()))
                .body("categoryId", is(rootCategoryId))
                .body("month", is(month));
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Create goal without JWT but with X-User should return 401")
    public void createGoalWithoutJwtButWithXUserShouldReturn401() throws JsonProcessingException {
        String rootCategoryId = UUID.randomUUID().toString();
        String month = YearMonth.now().plusMonths(1).toString();
        BigDecimal limit = BigDecimal.valueOf(100.00);

        SetGoalRequest requestBody = new SetGoalRequest(rootCategoryId, month, limit);
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        given()
                .contentType(ContentType.JSON)
                .header("X-User", registeredUser.email())
                .body(jsonBody)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(401);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Create goal missing X-User header should return 400")
    public void createGoalMissingXUserHeaderShouldReturn400() throws JsonProcessingException {
        String rootCategoryId = UUID.randomUUID().toString();
        String month = YearMonth.now().plusMonths(1).toString();
        BigDecimal limit = BigDecimal.valueOf(150.00);

        SetGoalRequest requestBody = new SetGoalRequest(rootCategoryId, month, limit);
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .body(jsonBody)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Create goal with invalid month format should return 400")
    public void createGoalInvalidMonthFormatShouldReturn400() throws JsonProcessingException {
        String rootCategoryId = category.id();
        String month = "2025/13"; // invalid format
        BigDecimal limit = BigDecimal.valueOf(200.00);

        SetGoalRequest requestBody = new SetGoalRequest(rootCategoryId, month, limit);
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Evaluate with JWT and existing goal should return 200")
    public void evaluateWithJwtAndExistingGoalShouldReturn200() throws JsonProcessingException {
        String rootCategoryId = category.id();
        String month = YearMonth.now().minusMonths(1).toString();
        BigDecimal limit = BigDecimal.valueOf(200.00);

        SetGoalRequest createBody = new SetGoalRequest(rootCategoryId, month, limit);
        String createJson = objectMapper.writeValueAsString(createBody);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(createJson)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .queryParam("rootCategoryId", rootCategoryId)
                .queryParam("month", month)
                .when()
                .get("/evaluate")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("categoryId", is(rootCategoryId))
                .body("month", is(month));
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Evaluate without JWT should return 401")
    public void evaluateWithoutJwtShouldReturn401() {
        String rootCategoryId = UUID.randomUUID().toString();
        String month = YearMonth.now().toString();

        given()
                .queryParam("rootCategoryId", rootCategoryId)
                .queryParam("month", month)
                .when()
                .get("/evaluate")
                .then()
                .log().ifValidationFails()
                .statusCode(401);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Evaluate goal not found should return 400")
    public void evaluateGoalNotFoundShouldReturn400() {
        String rootCategoryId = UUID.randomUUID().toString();
        String month = YearMonth.now().toString();

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .queryParam("rootCategoryId", rootCategoryId)
                .queryParam("month", month)
                .when()
                .get("/evaluate")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Evaluate with invalid month format should return 400")
    public void evaluateInvalidMonthFormatShouldReturn400() {
        String rootCategoryId = category.id();
        String month = "invalid-month";

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .queryParam("rootCategoryId", rootCategoryId)
                .queryParam("month", month)
                .when()
                .get("/evaluate")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }
}