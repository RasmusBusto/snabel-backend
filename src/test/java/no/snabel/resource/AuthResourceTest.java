package no.snabel.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import no.snabel.dto.LoginRequest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class AuthResourceTest {

    @Test
    public void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.username = "testuser";
        request.password = "password";
        request.deviceType = "web";

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("userId", equalTo(1))
            .body("username", equalTo("testuser"))
            .body("customerId", equalTo(1))
            .body("role", equalTo("ADMIN"))
            .body("expiresIn", notNullValue());
    }

    @Test
    public void testLoginWithInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.username = "testuser";
        request.password = "wrongpassword";
        request.deviceType = "web";

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("error", notNullValue());
    }

    @Test
    public void testLoginWithNonExistentUser() {
        LoginRequest request = new LoginRequest();
        request.username = "nonexistent";
        request.password = "password";
        request.deviceType = "web";

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("error", notNullValue());
    }

    @Test
    public void testLoginWithAppDeviceType() {
        LoginRequest request = new LoginRequest();
        request.username = "testuser";
        request.password = "password";
        request.deviceType = "app";

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("expiresIn", equalTo(7200)); // App token duration
    }
}
