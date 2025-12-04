# eFaktura Implementation Guide

## Overview

This document describes the eFaktura (Norwegian electronic invoicing) implementation in the Snabel accounting system. The implementation follows the **EHF 3.0 (PEPPOL BIS Billing 3.0)** standard, which is the current Norwegian standard for electronic invoicing.

## Features

- **EHF 3.0 XML Generation** - Generate invoices in EHF 3.0 format (PEPPOL BIS Billing 3.0)
- **Full PEPPOL BIS 3.0 Compliance** - Implements all mandatory business rules
- **Norwegian NS4102 Compliance** - Includes Foretaksregisteret and proper VAT formatting
- **PDF Invoice Generation** - Generate professional PDF invoices
- **RESTful API Endpoints** - Easy-to-use endpoints for downloading invoices
- **Multi-tenant Support** - Customer isolation built-in
- **Multi-VAT Rate Support** - Handles invoices with different VAT rates per line
- **PEPPOL Network Ready** - Electronic addresses for buyer and seller

## Database Schema Updates

The implementation adds the following fields to support eFaktura:

### Customers Table
- `bank_account` - Norwegian bank account number (11 digits)
- `iban` - International Bank Account Number
- `swift_bic` - SWIFT/BIC code for international payments (only ID used in XML, not name per UBL-CR-429)
- `endpoint_id` - Electronic address for PEPPOL network (mandatory per PEPPOL-EN16931-R020)
- `endpoint_scheme` - Scheme identifier for endpoint (default '0192' = Norwegian org number)

### Invoices Table
- `payment_reference` - KID number or payment reference
- `bank_account` - Override for customer's default bank account
- `buyer_reference` - Customer's reference (mandatory per PEPPOL-EN16931-R003)
- `order_reference` - Purchase order reference (alternative to buyer_reference)
- `contract_reference` - Contract or agreement reference
- `client_endpoint_id` - Buyer electronic address for PEPPOL network (mandatory per PEPPOL-EN16931-R010)
- `client_endpoint_scheme` - Scheme identifier for buyer endpoint (default '0192')

### Invoice Lines Table
- `unit_code` - UN/ECE unit code (EA, HUR, DAY, etc.)
- `item_name` - Product/service name
- `item_id` - Product/service ID or SKU

## API Endpoints

### 1. Download Invoice as PDF

```http
GET /api/invoices/{id}/pdf
```

**Description:** Downloads an invoice as a PDF file.

**Authorization:** Required (USER, ADMIN, ACCOUNTANT, CLIENT roles)

**Response:**
- `Content-Type: application/pdf`
- `Content-Disposition: attachment; filename="faktura-{invoiceNumber}.pdf"`

**Example:**
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/invoices/123/pdf \
     -o invoice-123.pdf
```

### 2. Download Invoice as EHF XML

```http
GET /api/invoices/{id}/efaktura
```

**Description:** Downloads an invoice in EHF 3.0 XML format (eFaktura).

**Authorization:** Required (USER, ADMIN, ACCOUNTANT, CLIENT roles)

**Response:**
- `Content-Type: application/xml`
- `Content-Disposition: attachment; filename="efaktura-{invoiceNumber}.xml"`

**Example:**
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/invoices/123/efaktura \
     -o invoice-123.xml
```

## EHF 3.0 XML Structure

The generated EHF XML follows the PEPPOL BIS Billing 3.0 specification and includes:

### Required Elements
- **CustomizationID** - EHF 3.0 profile identifier
- **ProfileID** - PEPPOL BIS Billing profile
- **Invoice Number** - Unique invoice identifier
- **Issue Date** - Invoice date
- **Due Date** - Payment due date
- **Invoice Type Code** - 380 (Commercial invoice)
- **Document Currency Code** - NOK

### Supplier (Seller) Information
- Organization number
- Company name
- Address
- VAT number (NO{orgNumber}MVA)
- Contact information

### Customer (Buyer) Information
- Organization number (if available)
- Company name
- Address

### Payment Information
- Payment means code (30 = Credit transfer)
- Bank account number
- Payment reference (KID)

### Invoice Lines
Each line includes:
- Line number
- Description
- Item name
- Quantity and unit code
- Unit price
- VAT rate and amount
- Line total

### Totals
- Line extension amount (subtotal)
- Tax exclusive amount
- Tax inclusive amount
- Payable amount

## Usage Examples

### Creating an Invoice with eFaktura Fields

```java
Invoice invoice = new Invoice();
invoice.invoiceNumber = "2024-001";
invoice.invoiceDate = LocalDate.now();
invoice.dueDate = LocalDate.now().plusDays(30);

// Customer information
invoice.clientName = "Test Client AS";
invoice.clientOrganizationNumber = "987654321";
invoice.clientAddress = "Kundeveien 1";
invoice.clientPostalCode = "0001";
invoice.clientCity = "Oslo";

// Payment information
invoice.paymentReference = "1234567890128"; // KID number
invoice.bankAccount = "12345678901"; // Override bank account if needed
invoice.buyerReference = "ORDER-123"; // Customer's order number

// Financial totals
invoice.subtotal = new BigDecimal("1000.00");
invoice.vatAmount = new BigDecimal("250.00");
invoice.totalAmount = new BigDecimal("1250.00");
invoice.currency = "NOK";

// Add invoice lines
InvoiceLine line = new InvoiceLine();
line.lineNumber = 1;
line.description = "Consulting services";
line.itemName = "Consulting";
line.itemId = "CONS-001";
line.quantity = new BigDecimal("10.00");
line.unitPrice = new BigDecimal("100.00");
line.unitCode = "HUR"; // Hours
line.vatRate = new BigDecimal("25.00");
line.vatAmount = new BigDecimal("250.00");
line.lineTotal = new BigDecimal("1000.00");

invoice.lines.add(line);
```

### Setting Up Customer Bank Information

```java
Customer customer = new Customer();
customer.companyName = "My Company AS";
customer.organizationNumber = "123456789";

// Bank information for eFaktura
customer.bankAccount = "12345678901"; // Norwegian account number
customer.bankName = "DNB Bank";
customer.iban = "NO9386011117947"; // For international payments
customer.swiftBic = "DNBANOKKXXX"; // For international payments
```

## Unit Codes (UN/ECE)

Common unit codes for invoice lines:

| Code | Description |
|------|-------------|
| EA | Each (piece) |
| HUR | Hour |
| DAY | Day |
| WEE | Week |
| MON | Month |
| ANN | Year |
| KGM | Kilogram |
| MTR | Meter |
| MTK | Square meter |
| MTQ | Cubic meter |
| LTR | Liter |

## VAT Rates in Norway

| Rate | Description |
|------|-------------|
| 0% | No VAT |
| 12% | Low rate (foodstuffs, transport, etc.) |
| 15% | Middle rate (discontinued, but may appear in old invoices) |
| 25% | Standard rate (default) |

## PDF Invoice Layout

The generated PDF includes:

### Header
- Supplier company name, address, org number
- "FAKTURA" title
- Invoice number

### Customer Section
- Customer name, address, org number

### Invoice Details
- Invoice date
- Due date (highlighted)
- Payment terms
- Buyer reference

### Invoice Lines Table
Columns:
- Line number
- Description
- Quantity
- Unit price
- VAT %
- Amount

### Totals Section
- Subtotal (ex. VAT)
- VAT amount
- **Total** (bold)

### Payment Information
- Bank account number
- KID/Payment reference
- Notes (if any)

## Testing

Run the eFaktura tests:

```bash
mvn test -Dtest=EFakturaServiceTest
```

## Validation

The EHF XML can be validated against the PEPPOL BIS Billing 3.0 specification using:

1. **ELMA Validator** - https://anskaffelser.dev/validator/
2. **PEPPOL Validation Service** - https://peppol.helger.com/public/menuitem-validation-bis3

## Troubleshooting

### Missing Customer Information

**Error:** Generated XML is missing supplier address information.

**Solution:** Ensure the Customer entity has all required fields populated:
- `address`
- `postalCode`
- `city`
- `organizationNumber`

### Missing Invoice Lines

**Error:** Invoice PDF/XML has no line items.

**Solution:** Ensure invoice lines are properly associated and persisted:
```java
invoice.lines = new ArrayList<>();
// Add lines...
invoice.persistAndFlush();
```

### Bank Account Not Showing

**Error:** Payment information is missing in generated documents.

**Solution:** Set bank account either on Customer or Invoice:
```java
customer.bankAccount = "12345678901";
// OR
invoice.bankAccount = "12345678901";
```

## Next Steps

### Future Enhancements

- **PEPPOL Integration** - Add PEPPOL access point integration for automatic delivery
- **Email Delivery** - Send invoices via email with PDF and XML attachments
- **Invoice Templates** - Custom PDF templates with company logo
- **Batch Generation** - Generate multiple invoices at once
- **Invoice Status Tracking** - Track when eFaktura is delivered and read

## References

- [EHF 3.0 Specification](https://anskaffelser.dev/postaward/g3/spec/current/billing-3.0/norway/)
- [PEPPOL BIS Billing 3.0](https://docs.peppol.eu/poacc/billing/3.0/)
- [Difi - Norwegian Digitalization Agency](https://www.digdir.no/)
- [UN/ECE Unit Codes](https://unece.org/trade/uncefact/cl-recommendations)

## PEPPOL BIS 3.0 Compliance

The implementation fully complies with PEPPOL BIS 3.0 Billing specification and Norwegian requirements:

### Core Business Rules (BR-XX)
- ✅ **BR-01 to BR-15**: All mandatory document fields
- ✅ **BR-08, BR-09**: Seller postal address with mandatory country code
- ✅ **BR-10, BR-11**: Buyer postal address with mandatory country code
- ✅ **BR-53**: Tax totals grouped by VAT rate

### PEPPOL Specific Rules
- ✅ **PEPPOL-EN16931-R001**: Business process identifier
- ✅ **PEPPOL-EN16931-R003**: Buyer reference or order reference (with fallback)
- ✅ **PEPPOL-EN16931-R004**: Specification identifier
- ✅ **PEPPOL-EN16931-R010**: Buyer electronic address with scheme identifier
- ✅ **PEPPOL-EN16931-R020**: Seller electronic address with scheme identifier
- ✅ **PEPPOL-EN16931-R053**: Single tax total with subtotals

### Norwegian Requirements (NO-R-XX)
- ✅ **NO-R-001**: VAT number format `NO{orgNumber}MVA` with mod-11 validation
- ✅ **NO-R-002**: "Foretaksregisteret" text for Norwegian suppliers
- ✅ **Organization number scheme**: Uses 0192 (Norwegian organization number)

### Multiple VAT Rate Support
The implementation now properly handles invoices with lines having different VAT rates:
- Creates separate `TaxSubtotal` elements for each VAT rate
- Groups invoice lines by VAT rate
- Correctly calculates taxable amount and tax amount per rate
- Uses appropriate tax category codes (S, Z, E)

### Validation
Generated EHF XML documents should pass validation at:
- [ELMA Validator](https://anskaffelser.dev/validator/)
- [PEPPOL Validation Service](https://peppol.helger.com/public/menuitem-validation-bis3)

## Support

For issues or questions about the eFaktura implementation, please contact the development team or file an issue in the project repository.

---

**Last Updated:** December 2025
**Version:** 2.0.0
**Standard:** EHF 3.0 (PEPPOL BIS Billing 3.0) - Full Compliance
**Compliance Status:** ✅ Fully compliant with PEPPOL BIS 3.0 and Norwegian NS4102
