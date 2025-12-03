package no.snabel.service;

import jakarta.enterprise.context.ApplicationScoped;
import no.snabel.model.Customer;
import no.snabel.model.Invoice;
import no.snabel.model.InvoiceLine;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Service for generating eFaktura (Norwegian electronic invoices) in EHF 3.0 format
 * EHF 3.0 is based on PEPPOL BIS Billing 3.0 (UBL 2.1)
 * Specification: https://anskaffelser.dev/postaward/g3/spec/current/billing-3.0/norway/
 */
@ApplicationScoped
public class EFakturaService {

    private static final String UBL_NAMESPACE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
    private static final String CAC_NAMESPACE = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String CBC_NAMESPACE = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Generate EHF 3.0 XML for an invoice
     */
    public String generateEHF(Invoice invoice) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // Root element: Invoice
            Element rootElement = doc.createElementNS(UBL_NAMESPACE, "Invoice");
            rootElement.setAttribute("xmlns:cac", CAC_NAMESPACE);
            rootElement.setAttribute("xmlns:cbc", CBC_NAMESPACE);
            doc.appendChild(rootElement);

            // Customization ID (EHF 3.0)
            addCbcElement(doc, rootElement, "CustomizationID",
                "urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0");

            // Profile ID (PEPPOL BIS Billing)
            addCbcElement(doc, rootElement, "ProfileID", "urn:fdc:peppol.eu:2017:poacc:billing:01:1.0");

            // Invoice number
            addCbcElement(doc, rootElement, "ID", invoice.invoiceNumber);

            // Issue date
            addCbcElement(doc, rootElement, "IssueDate", invoice.invoiceDate.format(DATE_FORMATTER));

            // Due date
            addCbcElement(doc, rootElement, "DueDate", invoice.dueDate.format(DATE_FORMATTER));

            // Invoice type code (380 = Commercial invoice)
            addCbcElement(doc, rootElement, "InvoiceTypeCode", "380");

            // Document currency code
            addCbcElement(doc, rootElement, "DocumentCurrencyCode", invoice.currency);

            // Buyer reference (if available)
            if (invoice.buyerReference != null && !invoice.buyerReference.isEmpty()) {
                addCbcElement(doc, rootElement, "BuyerReference", invoice.buyerReference);
            }

            // Contract reference (if available)
            if (invoice.contractReference != null && !invoice.contractReference.isEmpty()) {
                Element contractDocRef = addCacElement(doc, rootElement, "ContractDocumentReference");
                addCbcElement(doc, contractDocRef, "ID", invoice.contractReference);
            }

            // Accounting Supplier Party (Seller/Supplier)
            addSupplierParty(doc, rootElement, invoice.customer);

            // Accounting Customer Party (Buyer/Customer)
            addCustomerParty(doc, rootElement, invoice);

            // Payment means (bank transfer)
            addPaymentMeans(doc, rootElement, invoice);

            // Payment terms
            if (invoice.paymentTerms != null && !invoice.paymentTerms.isEmpty()) {
                Element paymentTermsElement = addCacElement(doc, rootElement, "PaymentTerms");
                addCbcElement(doc, paymentTermsElement, "Note", invoice.paymentTerms);
            }

            // Tax total
            addTaxTotal(doc, rootElement, invoice);

            // Legal monetary total
            addLegalMonetaryTotal(doc, rootElement, invoice);

            // Invoice lines
            for (InvoiceLine line : invoice.lines) {
                addInvoiceLine(doc, rootElement, line);
            }

            // Transform to XML string
            return transformToString(doc);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate EHF XML", e);
        }
    }

    private void addSupplierParty(Document doc, Element parent, Customer supplier) {
        Element supplierParty = addCacElement(doc, parent, "AccountingSupplierParty");
        Element party = addCacElement(doc, supplierParty, "Party");

        // Party identification (organization number)
        Element partyIdentification = addCacElement(doc, party, "PartyIdentification");
        addCbcElement(doc, partyIdentification, "ID", supplier.organizationNumber);

        // Party name
        Element partyName = addCacElement(doc, party, "PartyName");
        addCbcElement(doc, partyName, "Name", supplier.companyName);

        // Postal address
        if (supplier.address != null || supplier.city != null) {
            Element postalAddress = addCacElement(doc, party, "PostalAddress");
            if (supplier.address != null) {
                addCbcElement(doc, postalAddress, "StreetName", supplier.address);
            }
            if (supplier.city != null) {
                addCbcElement(doc, postalAddress, "CityName", supplier.city);
            }
            if (supplier.postalCode != null) {
                addCbcElement(doc, postalAddress, "PostalZone", supplier.postalCode);
            }
            if (supplier.country != null) {
                Element country = addCacElement(doc, postalAddress, "Country");
                addCbcElement(doc, country, "IdentificationCode", getCountryCode(supplier.country));
            }
        }

        // Party tax scheme (VAT number = organization number in Norway)
        Element partyTaxScheme = addCacElement(doc, party, "PartyTaxScheme");
        addCbcElement(doc, partyTaxScheme, "CompanyID", "NO" + supplier.organizationNumber + "MVA");
        Element taxScheme = addCacElement(doc, partyTaxScheme, "TaxScheme");
        addCbcElement(doc, taxScheme, "ID", "VAT");

        // Party legal entity
        Element partyLegalEntity = addCacElement(doc, party, "PartyLegalEntity");
        addCbcElement(doc, partyLegalEntity, "RegistrationName", supplier.companyName);
        addCbcElement(doc, partyLegalEntity, "CompanyID", supplier.organizationNumber);

        // Contact
        if (supplier.email != null || supplier.phone != null) {
            Element contact = addCacElement(doc, party, "Contact");
            if (supplier.contactPerson != null) {
                addCbcElement(doc, contact, "Name", supplier.contactPerson);
            }
            if (supplier.phone != null) {
                addCbcElement(doc, contact, "Telephone", supplier.phone);
            }
            if (supplier.email != null) {
                addCbcElement(doc, contact, "ElectronicMail", supplier.email);
            }
        }
    }

    private void addCustomerParty(Document doc, Element parent, Invoice invoice) {
        Element customerParty = addCacElement(doc, parent, "AccountingCustomerParty");
        Element party = addCacElement(doc, customerParty, "Party");

        // Party identification (organization number if available)
        if (invoice.clientOrganizationNumber != null && !invoice.clientOrganizationNumber.isEmpty()) {
            Element partyIdentification = addCacElement(doc, party, "PartyIdentification");
            addCbcElement(doc, partyIdentification, "ID", invoice.clientOrganizationNumber);
        }

        // Party name
        Element partyName = addCacElement(doc, party, "PartyName");
        addCbcElement(doc, partyName, "Name", invoice.clientName);

        // Postal address
        if (invoice.clientAddress != null || invoice.clientCity != null) {
            Element postalAddress = addCacElement(doc, party, "PostalAddress");
            if (invoice.clientAddress != null) {
                addCbcElement(doc, postalAddress, "StreetName", invoice.clientAddress);
            }
            if (invoice.clientCity != null) {
                addCbcElement(doc, postalAddress, "CityName", invoice.clientCity);
            }
            if (invoice.clientPostalCode != null) {
                addCbcElement(doc, postalAddress, "PostalZone", invoice.clientPostalCode);
            }
            Element country = addCacElement(doc, postalAddress, "Country");
            addCbcElement(doc, country, "IdentificationCode", "NO");
        }

        // Party legal entity
        Element partyLegalEntity = addCacElement(doc, party, "PartyLegalEntity");
        addCbcElement(doc, partyLegalEntity, "RegistrationName", invoice.clientName);
        if (invoice.clientOrganizationNumber != null && !invoice.clientOrganizationNumber.isEmpty()) {
            addCbcElement(doc, partyLegalEntity, "CompanyID", invoice.clientOrganizationNumber);
        }
    }

    private void addPaymentMeans(Document doc, Element parent, Invoice invoice) {
        Element paymentMeans = addCacElement(doc, parent, "PaymentMeans");

        // Payment means type code (30 = Credit transfer / bank transfer)
        addCbcElement(doc, paymentMeans, "PaymentMeansCode", "30");

        // Payment ID (KID number or payment reference)
        if (invoice.paymentReference != null && !invoice.paymentReference.isEmpty()) {
            addCbcElement(doc, paymentMeans, "PaymentID", invoice.paymentReference);
        }

        // Payee financial account (bank account)
        String bankAccount = invoice.bankAccount != null ? invoice.bankAccount :
                           (invoice.customer.bankAccount != null ? invoice.customer.bankAccount : null);

        if (bankAccount != null) {
            Element payeeFinancialAccount = addCacElement(doc, paymentMeans, "PayeeFinancialAccount");
            addCbcElement(doc, payeeFinancialAccount, "ID", bankAccount);

            if (invoice.customer.bankName != null) {
                Element financialInstitutionBranch = addCacElement(doc, payeeFinancialAccount, "FinancialInstitutionBranch");
                addCbcElement(doc, financialInstitutionBranch, "ID", invoice.customer.swiftBic != null ? invoice.customer.swiftBic : "");
                addCbcElement(doc, financialInstitutionBranch, "Name", invoice.customer.bankName);
            }
        }
    }

    private void addTaxTotal(Document doc, Element parent, Invoice invoice) {
        Element taxTotal = addCacElement(doc, parent, "TaxTotal");

        // Total tax amount
        addCbcElement(doc, taxTotal, "TaxAmount", invoice.vatAmount.setScale(2, RoundingMode.HALF_UP).toString())
            .setAttribute("currencyID", invoice.currency);

        // Tax subtotal (group by VAT rate)
        // For simplicity, we'll create one subtotal with the total VAT
        Element taxSubtotal = addCacElement(doc, taxTotal, "TaxSubtotal");
        addCbcElement(doc, taxSubtotal, "TaxableAmount", invoice.subtotal.setScale(2, RoundingMode.HALF_UP).toString())
            .setAttribute("currencyID", invoice.currency);
        addCbcElement(doc, taxSubtotal, "TaxAmount", invoice.vatAmount.setScale(2, RoundingMode.HALF_UP).toString())
            .setAttribute("currencyID", invoice.currency);

        Element taxCategory = addCacElement(doc, taxSubtotal, "TaxCategory");
        addCbcElement(doc, taxCategory, "ID", "S"); // S = Standard rate

        // Calculate average VAT percentage
        BigDecimal vatPercent = invoice.subtotal.compareTo(BigDecimal.ZERO) > 0
            ? invoice.vatAmount.divide(invoice.subtotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO;
        addCbcElement(doc, taxCategory, "Percent", vatPercent.setScale(2, RoundingMode.HALF_UP).toString());

        Element taxScheme = addCacElement(doc, taxCategory, "TaxScheme");
        addCbcElement(doc, taxScheme, "ID", "VAT");
    }

    private void addLegalMonetaryTotal(Document doc, Element parent, Invoice invoice) {
        Element legalMonetaryTotal = addCacElement(doc, parent, "LegalMonetaryTotal");

        addCbcElement(doc, legalMonetaryTotal, "LineExtensionAmount",
            invoice.subtotal.setScale(2, RoundingMode.HALF_UP).toString())
            .setAttribute("currencyID", invoice.currency);

        addCbcElement(doc, legalMonetaryTotal, "TaxExclusiveAmount",
            invoice.subtotal.setScale(2, RoundingMode.HALF_UP).toString())
            .setAttribute("currencyID", invoice.currency);

        addCbcElement(doc, legalMonetaryTotal, "TaxInclusiveAmount",
            invoice.totalAmount.setScale(2, RoundingMode.HALF_UP).toString())
            .setAttribute("currencyID", invoice.currency);

        addCbcElement(doc, legalMonetaryTotal, "PayableAmount",
            invoice.totalAmount.setScale(2, RoundingMode.HALF_UP).toString())
            .setAttribute("currencyID", invoice.currency);
    }

    private void addInvoiceLine(Document doc, Element parent, InvoiceLine line) {
        Element invoiceLine = addCacElement(doc, parent, "InvoiceLine");

        // Line ID
        addCbcElement(doc, invoiceLine, "ID", line.lineNumber.toString());

        // Invoiced quantity
        Element invoicedQuantity = addCbcElement(doc, invoiceLine, "InvoicedQuantity",
            line.quantity.setScale(2, RoundingMode.HALF_UP).toString());
        invoicedQuantity.setAttribute("unitCode", line.unitCode != null ? line.unitCode : "EA");

        // Line extension amount (total for this line excluding VAT)
        BigDecimal lineAmount = line.unitPrice.multiply(line.quantity);
        addCbcElement(doc, invoiceLine, "LineExtensionAmount",
            lineAmount.setScale(2, RoundingMode.HALF_UP).toString())
            .setAttribute("currencyID", "NOK");

        // Item
        Element item = addCacElement(doc, invoiceLine, "Item");
        addCbcElement(doc, item, "Description", line.description);
        addCbcElement(doc, item, "Name", line.itemName != null ? line.itemName : line.description);

        // Sellers item identification (product ID/SKU)
        if (line.itemId != null && !line.itemId.isEmpty()) {
            Element sellersItemId = addCacElement(doc, item, "SellersItemIdentification");
            addCbcElement(doc, sellersItemId, "ID", line.itemId);
        }

        // Classified tax category
        Element classifiedTaxCategory = addCacElement(doc, item, "ClassifiedTaxCategory");
        addCbcElement(doc, classifiedTaxCategory, "ID", "S"); // S = Standard rate
        addCbcElement(doc, classifiedTaxCategory, "Percent",
            line.vatRate.setScale(2, RoundingMode.HALF_UP).toString());
        Element taxScheme = addCacElement(doc, classifiedTaxCategory, "TaxScheme");
        addCbcElement(doc, taxScheme, "ID", "VAT");

        // Price
        Element price = addCacElement(doc, invoiceLine, "Price");
        addCbcElement(doc, price, "PriceAmount",
            line.unitPrice.setScale(2, RoundingMode.HALF_UP).toString())
            .setAttribute("currencyID", "NOK");
    }

    private Element addCbcElement(Document doc, Element parent, String name, String textContent) {
        Element element = doc.createElementNS(CBC_NAMESPACE, "cbc:" + name);
        element.setTextContent(textContent);
        parent.appendChild(element);
        return element;
    }

    private Element addCacElement(Document doc, Element parent, String name) {
        Element element = doc.createElementNS(CAC_NAMESPACE, "cac:" + name);
        parent.appendChild(element);
        return element;
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

    private String transformToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}
