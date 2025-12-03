package no.snabel.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import no.snabel.model.Customer;
import no.snabel.model.Invoice;
import no.snabel.model.InvoiceLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class EFakturaServiceTest {

    @Inject
    EFakturaService eFakturaService;

    @Test
    public void testGenerateEHF() {
        // Create test data
        Customer supplier = createTestSupplier();
        Invoice invoice = createTestInvoice(supplier);
        invoice.lines = createTestInvoiceLines(invoice);

        // Generate EHF XML
        String ehfXml = eFakturaService.generateEHF(invoice);

        // Verify XML is generated
        assertNotNull(ehfXml);
        assertFalse(ehfXml.isEmpty());

        // Verify XML contains required EHF elements
        assertTrue(ehfXml.contains("xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\""));
        assertTrue(ehfXml.contains("xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\""));
        assertTrue(ehfXml.contains("<cbc:CustomizationID>urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0</cbc:CustomizationID>"));
        assertTrue(ehfXml.contains("<cbc:ProfileID>urn:fdc:peppol.eu:2017:poacc:billing:01:1.0</cbc:ProfileID>"));

        // Verify invoice data is present
        assertTrue(ehfXml.contains("TEST-2024-001"));
        assertTrue(ehfXml.contains("Test AS"));
        assertTrue(ehfXml.contains("123456789"));
        assertTrue(ehfXml.contains("Test Client AS"));

        // Verify amounts
        assertTrue(ehfXml.contains("1000.00"));
        assertTrue(ehfXml.contains("250.00"));
        assertTrue(ehfXml.contains("1250.00"));
    }

    @Test
    public void testGenerateEHFWithMultipleLines() {
        // Create test data with multiple lines
        Customer supplier = createTestSupplier();
        Invoice invoice = createTestInvoice(supplier);
        invoice.lines = new ArrayList<>();

        InvoiceLine line1 = new InvoiceLine();
        line1.invoice = invoice;
        line1.lineNumber = 1;
        line1.description = "Product 1";
        line1.itemName = "Product 1";
        line1.quantity = new BigDecimal("2.00");
        line1.unitPrice = new BigDecimal("500.00");
        line1.vatRate = new BigDecimal("25.00");
        line1.vatAmount = new BigDecimal("250.00");
        line1.lineTotal = new BigDecimal("1000.00");
        invoice.lines.add(line1);

        InvoiceLine line2 = new InvoiceLine();
        line2.invoice = invoice;
        line2.lineNumber = 2;
        line2.description = "Service 1";
        line2.itemName = "Service 1";
        line2.quantity = new BigDecimal("3.00");
        line2.unitPrice = new BigDecimal("200.00");
        line2.vatRate = new BigDecimal("25.00");
        line2.vatAmount = new BigDecimal("150.00");
        line2.lineTotal = new BigDecimal("600.00");
        invoice.lines.add(line2);

        invoice.subtotal = new BigDecimal("1600.00");
        invoice.vatAmount = new BigDecimal("400.00");
        invoice.totalAmount = new BigDecimal("2000.00");

        // Generate EHF XML
        String ehfXml = eFakturaService.generateEHF(invoice);

        // Verify multiple invoice lines
        assertTrue(ehfXml.contains("Product 1"));
        assertTrue(ehfXml.contains("Service 1"));
        assertTrue(ehfXml.contains("<cbc:ID>1</cbc:ID>"));
        assertTrue(ehfXml.contains("<cbc:ID>2</cbc:ID>"));
    }

    @Test
    public void testGenerateEHFWithPaymentReference() {
        // Create test data with payment reference
        Customer supplier = createTestSupplier();
        Invoice invoice = createTestInvoice(supplier);
        invoice.lines = createTestInvoiceLines(invoice);
        invoice.paymentReference = "1234567890128";
        invoice.bankAccount = "12345678901";

        // Generate EHF XML
        String ehfXml = eFakturaService.generateEHF(invoice);

        // Verify payment information
        assertTrue(ehfXml.contains("1234567890128"));
        assertTrue(ehfXml.contains("12345678901"));
        assertTrue(ehfXml.contains("<cbc:PaymentMeansCode>30</cbc:PaymentMeansCode>"));
    }

    private Customer createTestSupplier() {
        Customer customer = new Customer();
        customer.id = 1L;
        customer.organizationNumber = "123456789";
        customer.companyName = "Test AS";
        customer.address = "Testveien 1";
        customer.postalCode = "0001";
        customer.city = "Oslo";
        customer.country = "Norge";
        customer.email = "test@test.no";
        customer.phone = "+47 12345678";
        customer.bankAccount = "12345678901";
        customer.bankName = "Test Bank";
        return customer;
    }

    private Invoice createTestInvoice(Customer supplier) {
        Invoice invoice = new Invoice();
        invoice.id = 1L;
        invoice.customer = supplier;
        invoice.invoiceNumber = "TEST-2024-001";
        invoice.invoiceDate = LocalDate.of(2024, 12, 1);
        invoice.dueDate = LocalDate.of(2024, 12, 31);
        invoice.clientName = "Test Client AS";
        invoice.clientOrganizationNumber = "987654321";
        invoice.clientAddress = "Kundeveien 2";
        invoice.clientPostalCode = "0002";
        invoice.clientCity = "Oslo";
        invoice.subtotal = new BigDecimal("1000.00");
        invoice.vatAmount = new BigDecimal("250.00");
        invoice.totalAmount = new BigDecimal("1250.00");
        invoice.currency = "NOK";
        invoice.status = "SENT";
        invoice.paymentTerms = "30 dager";
        return invoice;
    }

    private ArrayList<InvoiceLine> createTestInvoiceLines(Invoice invoice) {
        ArrayList<InvoiceLine> lines = new ArrayList<>();

        InvoiceLine line = new InvoiceLine();
        line.invoice = invoice;
        line.lineNumber = 1;
        line.description = "Test product/service";
        line.itemName = "Test Item";
        line.quantity = new BigDecimal("1.00");
        line.unitPrice = new BigDecimal("1000.00");
        line.vatRate = new BigDecimal("25.00");
        line.vatAmount = new BigDecimal("250.00");
        line.lineTotal = new BigDecimal("1000.00");
        line.unitCode = "EA";

        lines.add(line);
        return lines;
    }
}
