package no.snabel.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
public class AccountResourceTest {

    @Test
    @TestSecurity(user = "testuser", roles = "ADMIN")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "ADMIN")
    })
    public void testListAccounts() {
        given()
        .when()
            .get("/api/accounts")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].accountNumber", notNullValue())
            .body("[0].accountName", notNullValue());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "ADMIN")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "ADMIN")
    })
    public void testGetAccountById() {
        given()
        .when()
            .get("/api/accounts/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("accountNumber", equalTo("1900"))
            .body("accountName", equalTo("Bankkonto Hovedkonto"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "ADMIN")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "ADMIN")
    })
    public void testGetNonExistentAccount() {
        given()
        .when()
            .get("/api/accounts/999")
        .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "ADMIN")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "ADMIN")
    })
    public void testCreateAccount() {
        String requestBody = """
            {
                "accountNumber": "2000",
                "accountName": "Test Account",
                "accountType": "ASSET",
                "vatCode": "3",
                "description": "Test account description"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/accounts")
        .then()
            .statusCode(201)
            .body("accountNumber", equalTo("2000"))
            .body("accountName", equalTo("Test Account"))
            .body("accountType", equalTo("ASSET"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "ADMIN")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "ADMIN")
    })
    public void testUpdateAccount() {
        String requestBody = """
            {
                "accountName": "Updated Account Name",
                "description": "Updated description",
                "vatCode": "5"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .put("/api/accounts/1")
        .then()
            .statusCode(200)
            .body("accountName", equalTo("Updated Account Name"))
            .body("description", equalTo("Updated description"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "ADMIN")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "ADMIN")
    })
    public void testDeleteAccount() {
        given()
        .when()
            .delete("/api/accounts/2")
        .then()
            .statusCode(204);
    }

    @Test
    public void testUnauthorizedAccess() {
        given()
        .when()
            .get("/api/accounts")
        .then()
            .statusCode(401);
    }

    @Test
    @TestSecurity(user = "regularuser", roles = "USER")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "USER")
    })
    public void testUserCanListAccounts() {
        given()
        .when()
            .get("/api/accounts")
        .then()
            .statusCode(200);
    }

    @Test
    @TestSecurity(user = "regularuser", roles = "USER")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "USER")
    })
    public void testUserCannotCreateAccount() {
        String requestBody = """
            {
                "accountNumber": "3000",
                "accountName": "Forbidden Account",
                "accountType": "ASSET"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/accounts")
        .then()
            .statusCode(403);
    }
}
