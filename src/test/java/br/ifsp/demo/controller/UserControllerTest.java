package br.ifsp.demo.controller;

import br.ifsp.demo.security.auth.RegisterUserRequest;
import br.ifsp.demo.security.auth.RegisterUserResponse;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    private Faker faker = new Faker(new Locale("pt", "BR"));

    @Nested
    class RegisterEndpointTest{
        private RegisterUserRequest registerUserRequest;

        @BeforeEach
        public void setUp() {
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = port;
            RestAssured.basePath = "/api/v1/register";

            String email = faker.internet().emailAddress();
            String password = faker.internet().password(8, 12, true, true);
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();

            registerUserRequest = new RegisterUserRequest(email, password, firstName, lastName);
        }

        @Tag("ApiTest")
        @Tag("IntegrationTest")
        @Test
        @DisplayName("Should a post to register with valid inputs return 201.")
        void shouldAPostToRegisterWithValidInputsReturn201(){

            given()
            .contentType(ContentType.JSON)
            .body(registerUserRequest)
            .when()
            .post()
            .then()
            .statusCode(201)
            .body("id", notNullValue());
        }
    }
}
