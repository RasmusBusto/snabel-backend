package no.snabel.service;

import jakarta.enterprise.context.ApplicationScoped;
import no.snabel.format.ehf.ubl.InvoiceType;
import no.snabel.format.ehf.ubl.cac.*;
import no.snabel.format.ehf.ubl.types.*;
import no.snabel.format.ehf.ubl.writer.UBLWriter;
import no.snabel.model.Customer;
import no.snabel.model.Invoice;
import no.snabel.model.InvoiceLine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating eFaktura (Norwegian electronic invoices) in EHF 3.0 format
 * EHF 3.0 is based on PEPPOL BIS Billing 3.0 (UBL 2.1)
 * Specification: https://anskaffelser.dev/postaward/g3/spec/current/billing-3.0/norway/
 */
@ApplicationScoped
public class EHFInvoiceService {

    private final UBLWriter ublWriter;

    public EHFInvoiceService() {
        try {
            this.ublWriter = new UBLWriter();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize UBLWriter", e);
        }
    }

    /**
     * Generate EHF 3.0 XML for an invoice using UBL classes
     */
    public String generateEHF(Invoice invoice) {
        try {
            InvoiceType ublInvoice = new InvoiceType();

            // Core metadata
            setInvoiceMetadata(ublInvoice, invoice);

            // Buyer reference or Order reference (at least one is mandatory per PEPPOL-EN16931-R003)
            setBuyerReferenceOrOrderReference(ublInvoice, invoice);

            // Contract reference (if available)
            if (invoice.contractReference != null && !invoice.contractReference.isEmpty()) {
                DocumentReferenceType contractRef = new DocumentReferenceType(invoice.contractReference);
                ublInvoice.setContractDocumentReference(contractRef);
            }

            // Parties
            ublInvoice.setAccountingSupplierParty(createSupplierParty(invoice.customer));
            ublInvoice.setAccountingCustomerParty(createCustomerParty(invoice));

            // Payment
            ublInvoice.getPaymentMeans().add(createPaymentMeans(invoice));

            // Payment terms
            if (invoice.paymentTerms != null && !invoice.paymentTerms.isEmpty()) {
                ublInvoice.setPaymentTerms(new TextType(invoice.paymentTerms));
            }

            // Tax total
            ublInvoice.getTaxTotals().add(createTaxTotal(invoice));

            // Legal monetary total
            ublInvoice.setLegalMonetaryTotal(createLegalMonetaryTotal(invoice));

            // Invoice lines
            int lineNumber = 1;
            for (InvoiceLine line : invoice.lines) {
                ublInvoice.getInvoiceLines().add(createInvoiceLine(line, lineNumber++, invoice.currency));
            }

            return ublWriter.writeToString(ublInvoice);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate EHF XML", e);
        }
    }

    private void setInvoiceMetadata(InvoiceType ublInvoice, Invoice invoice) {
        // Customization ID (EHF 3.0)
        ublInvoice.setCustomizationID(new IdentifierType(
            "urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0"));

        // Profile ID (PEPPOL BIS Billing)
        ublInvoice.setProfileID(new IdentifierType("urn:fdc:peppol.eu:2017:poacc:billing:01:1.0"));

        // Invoice number
        ublInvoice.setId(new IdentifierType(invoice.invoiceNumber));

        // Issue date
        DateType issueDate = new DateType();
        issueDate.setValue(invoice.invoiceDate);
        ublInvoice.setIssueDate(issueDate);

        // Due date
        DateType dueDate = new DateType();
        dueDate.setValue(invoice.dueDate);
        ublInvoice.setDueDate(dueDate);

        // Invoice type code (380 = Commercial invoice)
        ublInvoice.setInvoiceTypeCode(new CodeType("380"));

        // Document currency code
        ublInvoice.setDocumentCurrencyCode(new CodeType(invoice.currency));
    }

    private void setBuyerReferenceOrOrderReference(InvoiceType ublInvoice, Invoice invoice) {
        if (invoice.buyerReference != null && !invoice.buyerReference.isEmpty()) {
            ublInvoice.setBuyerReference(new TextType(invoice.buyerReference));
        } else if (invoice.orderReference != null && !invoice.orderReference.isEmpty()) {
            ublInvoice.setOrderReference(new OrderReferenceType(invoice.orderReference));
        } else {
            // Fallback: use invoice number as buyer reference to ensure compliance
            ublInvoice.setBuyerReference(new TextType(invoice.invoiceNumber));
        }
    }

    private SupplierPartyType createSupplierParty(Customer supplier) {
        PartyType party = new PartyType();

        // Seller electronic address (PEPPOL-EN16931-R020 - mandatory)
        String endpointId = supplier.endpointId != null && !supplier.endpointId.isEmpty()
            ? supplier.endpointId
            : supplier.organizationNumber;
        String endpointScheme = supplier.endpointScheme != null && !supplier.endpointScheme.isEmpty()
            ? supplier.endpointScheme
            : "0192";
        party.setEndpointID(new IdentifierType(endpointId, endpointScheme));

        // Party identification (organization number)
        PartyIdentificationType partyId = new PartyIdentificationType(
            supplier.organizationNumber, "0192");
        party.getPartyIdentifications().add(partyId);

        // Party name
        party.setPartyName(new PartyNameType(supplier.companyName));

        // Postal address
        party.setPostalAddress(createAddress(
            supplier.address,
            supplier.city,
            supplier.postalCode,
            getCountryCode(supplier.country)
        ));

        // Party tax scheme - VAT (NO-R-001: format NO{orgNumber}MVA)
        PartyTaxSchemeType vatTaxScheme = new PartyTaxSchemeType();
        vatTaxScheme.setCompanyID(new IdentifierType("NO" + supplier.organizationNumber + "MVA"));
        vatTaxScheme.setTaxScheme(new TaxSchemeType("VAT"));
        party.getPartyTaxSchemes().add(vatTaxScheme);

        // Party tax scheme - Foretaksregisteret (NO-R-002: Norwegian legal requirement)
        PartyTaxSchemeType taxRegScheme = new PartyTaxSchemeType();
        taxRegScheme.setCompanyID(new IdentifierType("Foretaksregisteret"));
        taxRegScheme.setTaxScheme(new TaxSchemeType("TAX"));
        party.getPartyTaxSchemes().add(taxRegScheme);

        // Party legal entity
        PartyLegalEntityType legalEntity = new PartyLegalEntityType();
        legalEntity.setRegistrationName(new TextType(supplier.companyName));
        legalEntity.setCompanyID(new IdentifierType(supplier.organizationNumber));
        party.setPartyLegalEntity(legalEntity);

        // Contact
        if (supplier.email != null || supplier.phone != null) {
            ContactType contact = new ContactType();
            if (supplier.contactPerson != null) {
                contact.setName(new TextType(supplier.contactPerson));
            }
            if (supplier.phone != null) {
                contact.setTelephone(new TextType(supplier.phone));
            }
            if (supplier.email != null) {
                contact.setElectronicMail(new TextType(supplier.email));
            }
            party.setContact(contact);
        }

        return new SupplierPartyType(party);
    }

    private CustomerPartyType createCustomerParty(Invoice invoice) {
        PartyType party = new PartyType();

        // Buyer electronic address (PEPPOL-EN16931-R010 - mandatory)
        String buyerEndpointId = invoice.clientEndpointId != null && !invoice.clientEndpointId.isEmpty()
            ? invoice.clientEndpointId
            : (invoice.clientOrganizationNumber != null && !invoice.clientOrganizationNumber.isEmpty()
                ? invoice.clientOrganizationNumber
                : "NO-ENDPOINT");
        String buyerEndpointScheme = invoice.clientEndpointScheme != null && !invoice.clientEndpointScheme.isEmpty()
            ? invoice.clientEndpointScheme
            : "0192";
        party.setEndpointID(new IdentifierType(buyerEndpointId, buyerEndpointScheme));

        // Party identification (organization number if available)
        if (invoice.clientOrganizationNumber != null && !invoice.clientOrganizationNumber.isEmpty()) {
            PartyIdentificationType partyId = new PartyIdentificationType(
                invoice.clientOrganizationNumber, "0192");
            party.getPartyIdentifications().add(partyId);
        }

        // Party name
        party.setPartyName(new PartyNameType(invoice.clientName));

        // Postal address
        party.setPostalAddress(createAddress(
            invoice.clientAddress,
            invoice.clientCity,
            invoice.clientPostalCode,
            "NO" // Default to Norway
        ));

        // Party legal entity
        PartyLegalEntityType legalEntity = new PartyLegalEntityType();
        legalEntity.setRegistrationName(new TextType(invoice.clientName));
        if (invoice.clientOrganizationNumber != null && !invoice.clientOrganizationNumber.isEmpty()) {
            legalEntity.setCompanyID(new IdentifierType(invoice.clientOrganizationNumber));
        }
        party.setPartyLegalEntity(legalEntity);

        return new CustomerPartyType(party);
    }

    private AddressType createAddress(String street, String city, String postalCode, String countryCode) {
        AddressType address = new AddressType();

        if (street != null && !street.isEmpty()) {
            address.setStreetName(new TextType(street));
        }
        if (city != null && !city.isEmpty()) {
            address.setCityName(new TextType(city));
        }
        if (postalCode != null && !postalCode.isEmpty()) {
            address.setPostalZone(new TextType(postalCode));
        }

        // Country code is mandatory
        address.setCountry(new CountryType(countryCode));

        return address;
    }

    private PaymentMeansType createPaymentMeans(Invoice invoice) {
        PaymentMeansType paymentMeans = new PaymentMeansType();

        // Payment means type code (30 = Credit transfer / bank transfer)
        paymentMeans.setPaymentMeansCode(new CodeType("30"));

        // Payment ID (KID number or payment reference)
        if (invoice.paymentReference != null && !invoice.paymentReference.isEmpty()) {
            paymentMeans.setPaymentID(new IdentifierType(invoice.paymentReference));
        }

        // Payee financial account (bank account)
        String bankAccount = invoice.bankAccount != null ? invoice.bankAccount :
                           (invoice.customer.bankAccount != null ? invoice.customer.bankAccount : null);

        if (bankAccount != null) {
            FinancialAccountType financialAccount = new FinancialAccountType();
            financialAccount.setId(new IdentifierType(bankAccount));

            // Add FinancialInstitutionBranch (SWIFT/BIC) if available
            if (invoice.customer.swiftBic != null && !invoice.customer.swiftBic.isEmpty()) {
                financialAccount.setFinancialInstitutionBranchID(
                    new IdentifierType(invoice.customer.swiftBic));
            }

            paymentMeans.setPayeeFinancialAccount(financialAccount);
        }

        return paymentMeans;
    }

    private TaxTotalType createTaxTotal(Invoice invoice) {
        TaxTotalType taxTotal = new TaxTotalType();

        // Total tax amount
        taxTotal.setTaxAmount(new AmountType(
            invoice.vatAmount.setScale(2, RoundingMode.HALF_UP),
            invoice.currency));

        // Group invoice lines by VAT rate to create proper tax subtotals
        Map<BigDecimal, BigDecimal> vatRateToTaxableAmount = new HashMap<>();
        Map<BigDecimal, BigDecimal> vatRateToTaxAmount = new HashMap<>();

        for (InvoiceLine line : invoice.lines) {
            BigDecimal rate = line.vatRate != null ? line.vatRate : BigDecimal.ZERO;
            BigDecimal lineAmount = line.unitPrice.multiply(line.quantity);
            BigDecimal lineVatAmount = line.vatAmount != null ? line.vatAmount : BigDecimal.ZERO;

            vatRateToTaxableAmount.merge(rate, lineAmount, BigDecimal::add);
            vatRateToTaxAmount.merge(rate, lineVatAmount, BigDecimal::add);
        }

        // Create a tax subtotal for each VAT rate
        for (Map.Entry<BigDecimal, BigDecimal> entry : vatRateToTaxableAmount.entrySet()) {
            BigDecimal vatRate = entry.getKey();
            BigDecimal taxableAmount = entry.getValue();
            BigDecimal taxAmount = vatRateToTaxAmount.get(vatRate);

            TaxSubtotalType subtotal = new TaxSubtotalType();
            subtotal.setTaxableAmount(new AmountType(
                taxableAmount.setScale(2, RoundingMode.HALF_UP),
                invoice.currency));
            subtotal.setTaxAmount(new AmountType(
                taxAmount.setScale(2, RoundingMode.HALF_UP),
                invoice.currency));

            // Tax category
            TaxCategoryType taxCategory = new TaxCategoryType();
            String categoryCode = getTaxCategoryCode(vatRate);
            taxCategory.setId(new CodeType(categoryCode));

            // Add percentage for standard rates
            if (!"Z".equals(categoryCode) && !"E".equals(categoryCode)) {
                taxCategory.setPercent(new NumericType(vatRate.setScale(2, RoundingMode.HALF_UP)));
            }

            taxCategory.setTaxScheme(new TaxSchemeType("VAT"));
            subtotal.setTaxCategory(taxCategory);

            taxTotal.getTaxSubtotals().add(subtotal);
        }

        return taxTotal;
    }

    /**
     * Get the appropriate tax category code based on VAT rate
     * S = Standard rate, Z = Zero rated, E = Exempt from VAT
     */
    private String getTaxCategoryCode(BigDecimal vatRate) {
        if (vatRate.compareTo(BigDecimal.ZERO) == 0) {
            return "Z"; // Zero rated
        } else if (vatRate.compareTo(new BigDecimal("6")) == 0 ||
                   vatRate.compareTo(new BigDecimal("12")) == 0 ||
                   vatRate.compareTo(new BigDecimal("15")) == 0 ||
                   vatRate.compareTo(new BigDecimal("25")) == 0) {
            return "S"; // Standard rate (includes all Norwegian VAT rates)
        } else {
            return "S"; // Default to standard rate
        }
    }

    private MonetaryTotalType createLegalMonetaryTotal(Invoice invoice) {
        MonetaryTotalType monetaryTotal = new MonetaryTotalType();

        monetaryTotal.setLineExtensionAmount(new AmountType(
            invoice.subtotal.setScale(2, RoundingMode.HALF_UP),
            invoice.currency));

        monetaryTotal.setTaxExclusiveAmount(new AmountType(
            invoice.subtotal.setScale(2, RoundingMode.HALF_UP),
            invoice.currency));

        monetaryTotal.setTaxInclusiveAmount(new AmountType(
            invoice.totalAmount.setScale(2, RoundingMode.HALF_UP),
            invoice.currency));

        monetaryTotal.setPayableAmount(new AmountType(
            invoice.totalAmount.setScale(2, RoundingMode.HALF_UP),
            invoice.currency));

        return monetaryTotal;
    }

    private InvoiceLineType createInvoiceLine(InvoiceLine line, int lineNumber, String currency) {
        InvoiceLineType invoiceLine = new InvoiceLineType();

        // Line ID
        invoiceLine.setId(new IdentifierType(String.valueOf(lineNumber)));

        // Invoiced quantity
        invoiceLine.setInvoicedQuantity(new QuantityType(
            line.quantity.setScale(2, RoundingMode.HALF_UP),
            line.unitCode != null ? line.unitCode : "EA"));

        // Line extension amount (total for this line excluding VAT)
        BigDecimal lineAmount = line.unitPrice.multiply(line.quantity);
        invoiceLine.setLineExtensionAmount(new AmountType(
            lineAmount.setScale(2, RoundingMode.HALF_UP),
            currency));

        // Item
        ItemType item = new ItemType();
        item.setDescription(new TextType(line.description));
        item.setName(new TextType(line.itemName != null ? line.itemName : line.description));

        // Sellers item identification (product ID/SKU)
        if (line.itemId != null && !line.itemId.isEmpty()) {
            item.setSellersItemIdentification(new ItemIdentificationType(line.itemId));
        }

        // Classified tax category
        TaxCategoryType taxCategory = new TaxCategoryType();
        taxCategory.setId(new CodeType("S")); // S = Standard rate
        taxCategory.setPercent(new NumericType(line.vatRate.setScale(2, RoundingMode.HALF_UP)));
        taxCategory.setTaxScheme(new TaxSchemeType("VAT"));
        item.setClassifiedTaxCategory(taxCategory);

        invoiceLine.setItem(item);

        // Price
        PriceType price = new PriceType();
        price.setPriceAmount(new AmountType(
            line.unitPrice.setScale(2, RoundingMode.HALF_UP),
            currency));
        invoiceLine.setPrice(price);

        return invoiceLine;
    }

    private String getCountryCode(String countryName) {
        // Simple mapping for common Norwegian terms
        if (countryName == null) return "NO";
        String lower = countryName.toLowerCase();
        if (lower.contains("norge") || lower.contains("norway")) return "NO";
        if (lower.contains("sweden") || lower.contains("sverige")) return "SE";
        if (lower.contains("denmark") || lower.contains("danmark")) return "DK";
        return "NO"; // Default to Norway
    }
}
