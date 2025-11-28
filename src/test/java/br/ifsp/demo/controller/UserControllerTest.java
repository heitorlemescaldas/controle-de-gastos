package br.ifsp.demo.controller;

import br.ifsp.demo.security.auth.AuthRequest;
import br.ifsp.demo.security.auth.RegisterUserRequest;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    private final Faker faker = new Faker(new Locale("pt", "BR"));
    private String email;
    private String password;
    private String firstName;
    private String lastName;


    @Nested
    class RegisterEndpointTest {
        private RegisterUserRequest registerUserRequest;

        @BeforeEach
        public void setUp() {
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = port;
            RestAssured.basePath = "/api/v1/register";
            email = faker.internet().emailAddress();
            password = faker.internet().password(8, 12, true, true);
            firstName = faker.name().firstName();
            lastName = faker.name().lastName();

            registerUserRequest = new RegisterUserRequest(firstName, lastName, email, password);
        }

        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Test
        @DisplayName("Should a post to register with valid inputs return 201.")
        void shouldAPostToRegisterWithValidInputsReturn201() {

            given()
                    .contentType(ContentType.JSON)
                    .body(registerUserRequest)
                    .when()
                    .post()
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue());
        }

        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Test
        @DisplayName("Should post to register return 409 when email is already in use.")
        void shouldAPostToRegisterReturn409WhenEmailIsAlreadyInUse() {

            String alreadyEmailExistsMessage = "Email already registered: " + email;

            given()
                    .contentType(ContentType.JSON)
                    .body(registerUserRequest)
                    .when()
                    .post()
                    .then()
                    .statusCode(201);

            given()
                    .contentType(ContentType.JSON)
                    .body(registerUserRequest)
                    .when()
                    .post()
                    .then()
                    .statusCode(409)
                    .body("message", is(alreadyEmailExistsMessage))
                    .body("status", is("CONFLICT"));
        }
    }

    @Nested
    class AuthenticateEndpointTest {

        @BeforeEach
        public void setUp() {
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = port;
            RestAssured.basePath = "/api/v1/authenticate";
            String apiBase = "http://localhost:" + port + "/api/v1";

            email = faker.internet().emailAddress();
            password = faker.internet().password(8, 12, true, true);
            firstName = faker.name().firstName();
            lastName = faker.name().lastName();

            var registerUserRequest = new RegisterUserRequest(firstName, lastName, email, password);

            given()
                    .contentType(ContentType.JSON)
                    .body(registerUserRequest)
                    .when()
                    .post(apiBase + "/register")
                    .then()
                    .statusCode(201);
        }

        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Test
        @DisplayName("Should a post to authenticate with valid credentials return 200.")
        void shouldAPostToAuthenticateWithValidCredentialsReturn200() {

            var authRequest = new AuthRequest(email, password);

            given()
                    .contentType(ContentType.JSON)
                    .body(authRequest)
                    .when()
                    .post()
                    .then()
                    .statusCode(200)
                    .body("token", notNullValue());

        }

        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Test
        @DisplayName("Should a post to authenticate with invalid credentials return 401.")
        void shouldAPostToAuthenticateWithValidCredentialsReturn401() {

            var invalidAuthRequest = new AuthRequest("somemail", "somePassWord");

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidAuthRequest)
                    .when()
                    .post()
                    .then()
                    .statusCode(401);

        }
    }
}
