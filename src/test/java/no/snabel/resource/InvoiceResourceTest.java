package no.snabel.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
public class InvoiceResourceTest {

    @Test
    @TestSecurity(user = "testuser", roles = "ADMIN")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "ADMIN")
    })
    public void testListInvoices() {
        given()
        .when()
            .get("/api/invoices")
        .then()
            .statusCode(200);
    }

    @Test
    @TestSecurity(user = "testuser", roles = "ACCOUNTANT")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "ACCOUNTANT")
    })
    public void testCreateInvoice() {
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);

        String requestBody = String.format("""
            {
                "invoiceNumber": "INV-001",
                "invoiceDate": "%s",
                "dueDate": "%s",
                "clientName": "Test Client AS",
                "clientOrganizationNumber": "987654321",
                "clientAddress": "Test Street 123",
                "clientPostalCode": "0123",
                "clientCity": "Oslo",
                "subtotal": 10000.00,
                "vatAmount": 2500.00,
                "totalAmount": 12500.00,
                "currency": "NOK",
                "paymentTerms": "14 dager",
                "notes": "Test invoice"
            }
            """, today, dueDate);

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/invoices")
        .then()
            .statusCode(201)
            .body("invoiceNumber", equalTo("INV-001"))
            .body("clientName", equalTo("Test Client AS"))
            .body("status", equalTo("DRAFT"))
            .body("totalAmount", equalTo(12500.00f));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "ACCOUNTANT")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "ACCOUNTANT")
    })
    public void testGetInvoiceById() {
        // First create an invoice
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);

        String createBody = String.format("""
            {
                "invoiceNumber": "INV-002",
                "invoiceDate": "%s",
                "dueDate": "%s",
                "clientName": "Another Client AS",
                "subtotal": 5000.00,
                "vatAmount": 1250.00,
                "totalAmount": 6250.00
            }
            """, today, dueDate);

        Integer invoiceId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/invoices")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Then retrieve it
        given()
        .when()
            .get("/api/invoices/" + invoiceId)
        .then()
            .statusCode(200)
            .body("id", equalTo(invoiceId))
            .body("invoiceNumber", equalTo("INV-002"));
    }

    @Test
    @TestSecurity(user = "testuser", roles = "ACCOUNTANT")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "ACCOUNTANT")
    })
    public void testSendInvoice() {
        // Create invoice
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);

        String createBody = String.format("""
            {
                "invoiceNumber": "INV-003",
                "invoiceDate": "%s",
                "dueDate": "%s",
                "clientName": "Send Test Client",
                "subtotal": 1000.00,
                "vatAmount": 250.00,
                "totalAmount": 1250.00
            }
            """, today, dueDate);

        Integer invoiceId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/invoices")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Send invoice
        given()
        .when()
            .put("/api/invoices/" + invoiceId + "/send")
        .then()
            .statusCode(200)
            .body("status", equalTo("SENT"))
            .body("sentAt", notNullValue());
    }

    @Test
    @TestSecurity(user = "testuser", roles = "ACCOUNTANT")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "ACCOUNTANT")
    })
    public void testMarkInvoicePaid() {
        // Create invoice
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);

        String createBody = String.format("""
            {
                "invoiceNumber": "INV-004",
                "invoiceDate": "%s",
                "dueDate": "%s",
                "clientName": "Payment Test Client",
                "subtotal": 2000.00,
                "vatAmount": 500.00,
                "totalAmount": 2500.00
            }
            """, today, dueDate);

        Integer invoiceId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/api/invoices")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Mark as paid
        given()
        .when()
            .put("/api/invoices/" + invoiceId + "/pay")
        .then()
            .statusCode(200)
            .body("status", equalTo("PAID"))
            .body("paidAt", notNullValue());
    }

    @Test
    @TestSecurity(user = "regularuser", roles = "USER")
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1"),
        @Claim(key = "role", value = "USER")
    })
    public void testUserCannotCreateInvoice() {
        String requestBody = """
            {
                "invoiceNumber": "INV-FORBIDDEN",
                "invoiceDate": "2025-01-01",
                "dueDate": "2025-01-15",
                "clientName": "Forbidden Client",
                "totalAmount": 1000.00
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/invoices")
        .then()
            .statusCode(403);
    }
}
