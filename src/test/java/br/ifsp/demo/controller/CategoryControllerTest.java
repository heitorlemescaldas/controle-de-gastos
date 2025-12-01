package br.ifsp.demo.controller;

import br.ifsp.demo.controller.CategoryController.CreateCategoryRequest;
import br.ifsp.demo.controller.CategoryController.MoveRequest;
import br.ifsp.demo.controller.CategoryController.RenameRequest;
import br.ifsp.demo.domain.model.CategoryNode;
import br.ifsp.demo.security.auth.AuthRequest;
import br.ifsp.demo.security.auth.RegisterUserRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CategoryControllerTest {

    private static final int MAX_NAME_LENGTH = 50;

    @LocalServerPort
    private int port;

    private Faker faker;
    private ObjectMapper objectMapper;
    private String apiBase;

    private String jwtToken;
    private RegisterUserRequest registeredUser;

    private String truncate(String input) {
        if (input == null) {
            return null;
        }
        return input.substring(0, Math.min(input.length(), MAX_NAME_LENGTH));
    }

    @BeforeAll
    public void setupAll() throws JsonProcessingException {
        faker = new Faker(new Locale("pt", "BR"));
        objectMapper = new ObjectMapper();

        apiBase = "http://localhost:" + port + "/api/v1";

        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 12, true, true);

        String firstName = truncate(faker.name().firstName());
        String lastName = truncate(faker.name().lastName());

        registeredUser = new RegisterUserRequest(firstName, lastName, email, password);

        String registerJson = objectMapper.writeValueAsString(registeredUser);

        given()
                .contentType(ContentType.JSON)
                .body(registerJson)
                .when()
                .post(apiBase + "/register")
                .then()
                .statusCode(201);

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
    }

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/categories";
    }

    private String uniqueName() {
        return "category-" + UUID.randomUUID().toString();
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Create root category with JWT should return 201")
    public void createRootCategoryWithJwtShouldReturn201() throws JsonProcessingException {
        String name = uniqueName();
        CreateCategoryRequest req = new CreateCategoryRequest(name);
        String json = objectMapper.writeValueAsString(req);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("name", is(name));
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Create root category missing X-User header should return 400")
    public void createRootCategoryMissingXUserHeaderShouldReturn400() throws JsonProcessingException {
        String name = uniqueName();
        CreateCategoryRequest req = new CreateCategoryRequest(name);
        String json = objectMapper.writeValueAsString(req);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Create root category without JWT but with X-User should return 401")
    public void createRootCategoryWithoutJwtButWithXUserShouldReturn401() throws JsonProcessingException {
        String name = uniqueName();
        CreateCategoryRequest req = new CreateCategoryRequest(name);
        String json = objectMapper.writeValueAsString(req);

        given()
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(401);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Create child category with JWT should return 201")
    public void createChildCategoryWithJwtShouldReturn201() throws JsonProcessingException {
        // create parent root
        String parentName = uniqueName();
        CreateCategoryRequest parentReq = new CreateCategoryRequest(parentName);
        String parentJson = objectMapper.writeValueAsString(parentReq);

        CategoryNode parent = given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(parentJson)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract().body().as(CategoryNode.class);

        Assertions.assertNotNull(parent, "parent deve existir");
        Assertions.assertNotNull(parent.id(), "parent.id() não deve ser nulo - verifique extração");

        String childName = uniqueName();
        CreateCategoryRequest childReq = new CreateCategoryRequest(childName);
        String childJson = objectMapper.writeValueAsString(childReq);

        Response resp = given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(childJson)
                .when()
                .post("/" + parent.id() + "/children")
                .andReturn();

        System.out.println("POST /api/v1/categories/" + parent.id() + "/children -> HTTP " + resp.getStatusCode());
        System.out.println(resp.asString());

        Assertions.assertEquals(201, resp.getStatusCode(), "esperava 201; ver output acima para o motivo");

        CategoryNode childNode = resp.getBody().as(CategoryNode.class);
        Assertions.assertNotNull(childNode.id(), "child.id não deve ser nulo");
        Assertions.assertEquals(childName, childNode.name());
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Create child category with invalid parent should return 400")
    public void createChildCategoryWithInvalidParentShouldReturn400() throws JsonProcessingException {
        String childName = uniqueName();
        CreateCategoryRequest childReq = new CreateCategoryRequest(childName);
        String childJson = objectMapper.writeValueAsString(childReq);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(childJson)
                .when()
                .post("/" + UUID.randomUUID() + "/children")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Delete category with JWT should return 204")
    public void deleteCategoryWithJwtShouldReturn204() throws JsonProcessingException {
        String name = uniqueName();
        CreateCategoryRequest req = new CreateCategoryRequest(name);
        String json = objectMapper.writeValueAsString(req);

        CategoryNode node = given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract().body().as(CategoryNode.class);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .when()
                .delete("/" + node.id())
                .then()
                .log().ifValidationFails()
                .statusCode(204);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Delete category missing X-User header should return 400")
    public void deleteCategoryMissingXUserHeaderShouldReturn400() throws JsonProcessingException {
        String name = uniqueName();
        CreateCategoryRequest req = new CreateCategoryRequest(name);
        String json = objectMapper.writeValueAsString(req);

        CategoryNode node = given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract().body().as(CategoryNode.class);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete("/" + node.id())
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Rename category with JWT should return 204")
    public void renameCategoryWithJwtShouldReturn204() throws JsonProcessingException {
        String name = uniqueName();
        CreateCategoryRequest req = new CreateCategoryRequest(name);
        String json = objectMapper.writeValueAsString(req);

        CategoryNode node = given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract().body().as(CategoryNode.class);

        RenameRequest rename = new RenameRequest(name + "X");
        String renameJson = objectMapper.writeValueAsString(rename);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(renameJson)
                .when()
                .patch("/" + node.id() + "/rename")
                .then()
                .log().ifValidationFails()
                .statusCode(204);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Move category with JWT should return 204")
    public void moveCategoryWithJwtShouldReturn204() throws JsonProcessingException {
        CreateCategoryRequest reqA = new CreateCategoryRequest(uniqueName());
        CreateCategoryRequest reqB = new CreateCategoryRequest(uniqueName());
        String jsonA = objectMapper.writeValueAsString(reqA);
        String jsonB = objectMapper.writeValueAsString(reqB);

        CategoryNode rootA = given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(jsonA)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract().body().as(CategoryNode.class);

        CategoryNode rootB = given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(jsonB)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract().body().as(CategoryNode.class);

        CreateCategoryRequest childReq = new CreateCategoryRequest(uniqueName());
        String childJson = objectMapper.writeValueAsString(childReq);

        CategoryNode child = given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(childJson)
                .when()
                .post("/" + rootA.id() + "/children")
                .then()
                .statusCode(201)
                .extract().body().as(CategoryNode.class);

        MoveRequest move = new MoveRequest(rootB.id());
        String moveJson = objectMapper.writeValueAsString(move);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(moveJson)
                .when()
                .patch("/" + child.id() + "/move")
                .then()
                .log().ifValidationFails()
                .statusCode(204);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("Move category to non existing parent should return 400")
    public void moveCategoryToNonExistingParentShouldReturn400() throws JsonProcessingException {
        CreateCategoryRequest req = new CreateCategoryRequest(uniqueName());
        String json = objectMapper.writeValueAsString(req);

        CategoryNode node = given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract().body().as(CategoryNode.class);

        MoveRequest move = new MoveRequest(UUID.randomUUID().toString());
        String moveJson = objectMapper.writeValueAsString(move);

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .contentType(ContentType.JSON)
                .body(moveJson)
                .when()
                .patch("/" + node.id() + "/move")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("List categories with JWT should return 200")
    public void listCategoriesWithJwtShouldReturn200() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .header("X-User", registeredUser.email())
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", is(notNullValue()));
    }

    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @Test
    @DisplayName("List categories missing X-User header should return 400")
    public void listCategoriesMissingXUserHeaderShouldReturn400() {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }
}