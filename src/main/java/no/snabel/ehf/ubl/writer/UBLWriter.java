package no.snabel.ehf.ubl.writer;

import no.snabel.ehf.ubl.InvoiceType;
import no.snabel.ehf.ubl.cac.*;
import no.snabel.ehf.ubl.types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * UBLWriter - Converts InvoiceType objects to UBL 2.1 XML format.
 *
 * Generates PEPPOL BIS Billing 3.0 / EHF 3.0 compliant XML with proper namespaces.
 */
public class UBLWriter {

    private static final String NS_INVOICE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
    private static final String NS_CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String NS_CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";

    private final DocumentBuilder documentBuilder;

    public UBLWriter() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        this.documentBuilder = factory.newDocumentBuilder();
    }

    /**
     * Converts an InvoiceType to UBL XML string.
     */
    public String writeToString(InvoiceType invoice) throws Exception {
        Document doc = documentBuilder.newDocument();

        Element root = doc.createElementNS(NS_INVOICE, "Invoice");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", NS_INVOICE);
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cac", NS_CAC);
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cbc", NS_CBC);
        doc.appendChild(root);

        // Core metadata
        appendCbcIdentifier(doc, root, "CustomizationID", invoice.getCustomizationID());
        appendCbcIdentifier(doc, root, "ProfileID", invoice.getProfileID());
        appendCbcIdentifier(doc, root, "ID", invoice.getId());
        appendCbcDate(doc, root, "IssueDate", invoice.getIssueDate());
        appendCbcCode(doc, root, "InvoiceTypeCode", invoice.getInvoiceTypeCode());
        appendCbcCode(doc, root, "DocumentCurrencyCode", invoice.getDocumentCurrencyCode());

        // Optional headers
        if (invoice.getDueDate() != null) {
            appendCbcDate(doc, root, "DueDate", invoice.getDueDate());
        }
        if (invoice.getNote() != null) {
            appendCbcText(doc, root, "Note", invoice.getNote());
        }
        if (invoice.getTaxPointDate() != null) {
            appendCbcDate(doc, root, "TaxPointDate", invoice.getTaxPointDate());
        }
        if (invoice.getTaxCurrencyCode() != null) {
            appendCbcCode(doc, root, "TaxCurrencyCode", invoice.getTaxCurrencyCode());
        }
        if (invoice.getAccountingCost() != null) {
            appendCbcText(doc, root, "AccountingCost", invoice.getAccountingCost());
        }
        if (invoice.getBuyerReference() != null) {
            appendCbcText(doc, root, "BuyerReference", invoice.getBuyerReference());
        }

        // Period
        if (invoice.getInvoicePeriod() != null) {
            appendPeriod(doc, root, "InvoicePeriod", invoice.getInvoicePeriod());
        }

        // References
        if (invoice.getOrderReference() != null) {
            appendOrderReference(doc, root, invoice.getOrderReference());
        }
        if (invoice.getContractDocumentReference() != null) {
            appendDocumentReference(doc, root, "ContractDocumentReference", invoice.getContractDocumentReference());
        }
        for (DocumentReferenceType docRef : invoice.getAdditionalDocumentReferences()) {
            appendDocumentReference(doc, root, "AdditionalDocumentReference", docRef);
        }

        // Parties
        appendSupplierParty(doc, root, invoice.getAccountingSupplierParty());
        appendCustomerParty(doc, root, invoice.getAccountingCustomerParty());

        // Payment
        for (PaymentMeansType pm : invoice.getPaymentMeans()) {
            appendPaymentMeans(doc, root, pm);
        }

        // Allowances/Charges
        for (AllowanceChargeType ac : invoice.getAllowanceCharges()) {
            appendAllowanceCharge(doc, root, ac);
        }

        // Tax
        for (TaxTotalType tt : invoice.getTaxTotals()) {
            appendTaxTotal(doc, root, tt);
        }

        // Monetary total
        appendMonetaryTotal(doc, root, invoice.getLegalMonetaryTotal());

        // Lines
        for (InvoiceLineType line : invoice.getInvoiceLines()) {
            appendInvoiceLine(doc, root, line);
        }

        return documentToString(doc);
    }

    // === Helper Methods ===

    private void appendCbcIdentifier(Document doc, Element parent, String name, IdentifierType id) {
        if (id == null || id.getValue() == null) return;
        Element elem = doc.createElementNS(NS_CBC, "cbc:" + name);
        if (id.getSchemeID() != null) {
            elem.setAttribute("schemeID", id.getSchemeID());
        }
        if (id.getSchemeAgencyID() != null) {
            elem.setAttribute("schemeAgencyID", id.getSchemeAgencyID());
        }
        elem.setTextContent(id.getValue());
        parent.appendChild(elem);
    }

    private void appendCbcCode(Document doc, Element parent, String name, CodeType code) {
        if (code == null || code.getValue() == null) return;
        Element elem = doc.createElementNS(NS_CBC, "cbc:" + name);
        if (code.getListID() != null) {
            elem.setAttribute("listID", code.getListID());
        }
        if (code.getName() != null) {
            elem.setAttribute("name", code.getName());
        }
        elem.setTextContent(code.getValue());
        parent.appendChild(elem);
    }

    private void appendCbcText(Document doc, Element parent, String name, TextType text) {
        if (text == null || text.getValue() == null) return;
        Element elem = doc.createElementNS(NS_CBC, "cbc:" + name);
        if (text.getLanguageID() != null) {
            elem.setAttribute("languageID", text.getLanguageID());
        }
        elem.setTextContent(text.getValue());
        parent.appendChild(elem);
    }

    private void appendCbcAmount(Document doc, Element parent, String name, AmountType amount) {
        if (amount == null || amount.getValue() == null) return;
        Element elem = doc.createElementNS(NS_CBC, "cbc:" + name);
        elem.setAttribute("currencyID", amount.getCurrencyID());
        elem.setTextContent(formatDecimal(amount.getValue()));
        parent.appendChild(elem);
    }

    private void appendCbcQuantity(Document doc, Element parent, String name, QuantityType qty) {
        if (qty == null || qty.getValue() == null) return;
        Element elem = doc.createElementNS(NS_CBC, "cbc:" + name);
        elem.setAttribute("unitCode", qty.getUnitCode());
        elem.setTextContent(formatDecimal(qty.getValue()));
        parent.appendChild(elem);
    }

    private void appendCbcNumeric(Document doc, Element parent, String name, NumericType num) {
        if (num == null || num.getValue() == null) return;
        Element elem = doc.createElementNS(NS_CBC, "cbc:" + name);
        elem.setTextContent(formatDecimal(num.getValue()));
        parent.appendChild(elem);
    }

    private void appendCbcDate(Document doc, Element parent, String name, DateType date) {
        if (date == null || date.getValue() == null) return;
        Element elem = doc.createElementNS(NS_CBC, "cbc:" + name);
        elem.setTextContent(date.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE));
        parent.appendChild(elem);
    }

    private void appendCbcIndicator(Document doc, Element parent, String name, IndicatorType indicator) {
        if (indicator == null || indicator.getValue() == null) return;
        Element elem = doc.createElementNS(NS_CBC, "cbc:" + name);
        elem.setTextContent(indicator.getValue().toString().toLowerCase());
        parent.appendChild(elem);
    }

    private void appendPeriod(Document doc, Element parent, String name, PeriodType period) {
        Element elem = doc.createElementNS(NS_CAC, "cac:" + name);
        if (period.getStartDate() != null) {
            appendCbcDate(doc, elem, "StartDate", period.getStartDate());
        }
        if (period.getEndDate() != null) {
            appendCbcDate(doc, elem, "EndDate", period.getEndDate());
        }
        if (period.getDescriptionCode() != null) {
            appendCbcCode(doc, elem, "DescriptionCode", period.getDescriptionCode());
        }
        parent.appendChild(elem);
    }

    private void appendOrderReference(Document doc, Element parent, OrderReferenceType orderRef) {
        Element elem = doc.createElementNS(NS_CAC, "cac:OrderReference");
        appendCbcIdentifier(doc, elem, "ID", orderRef.getId());
        if (orderRef.getSalesOrderID() != null) {
            appendCbcIdentifier(doc, elem, "SalesOrderID", orderRef.getSalesOrderID());
        }
        parent.appendChild(elem);
    }

    private void appendDocumentReference(Document doc, Element parent, String name, DocumentReferenceType docRef) {
        Element elem = doc.createElementNS(NS_CAC, "cac:" + name);
        appendCbcIdentifier(doc, elem, "ID", docRef.getId());
        if (docRef.getIssueDate() != null) {
            appendCbcDate(doc, elem, "IssueDate", docRef.getIssueDate());
        }
        if (docRef.getDocumentTypeCode() != null) {
            appendCbcIdentifier(doc, elem, "DocumentTypeCode", docRef.getDocumentTypeCode());
        }
        parent.appendChild(elem);
    }

    private void appendSupplierParty(Document doc, Element parent, SupplierPartyType supplier) {
        Element elem = doc.createElementNS(NS_CAC, "cac:AccountingSupplierParty");
        appendParty(doc, elem, supplier.getParty());
        parent.appendChild(elem);
    }

    private void appendCustomerParty(Document doc, Element parent, CustomerPartyType customer) {
        Element elem = doc.createElementNS(NS_CAC, "cac:AccountingCustomerParty");
        appendParty(doc, elem, customer.getParty());
        parent.appendChild(elem);
    }

    private void appendParty(Document doc, Element parent, PartyType party) {
        Element partyElem = doc.createElementNS(NS_CAC, "cac:Party");

        // EndpointID
        appendCbcIdentifier(doc, partyElem, "EndpointID", party.getEndpointID());

        // PartyIdentification
        for (PartyIdentificationType partyId : party.getPartyIdentifications()) {
            Element pidElem = doc.createElementNS(NS_CAC, "cac:PartyIdentification");
            appendCbcIdentifier(doc, pidElem, "ID", partyId.getId());
            partyElem.appendChild(pidElem);
        }

        // PartyName
        if (party.getPartyName() != null) {
            Element pnElem = doc.createElementNS(NS_CAC, "cac:PartyName");
            appendCbcText(doc, pnElem, "Name", party.getPartyName().getName());
            partyElem.appendChild(pnElem);
        }

        // PostalAddress
        if (party.getPostalAddress() != null) {
            appendAddress(doc, partyElem, party.getPostalAddress());
        }

        // PartyTaxScheme
        for (PartyTaxSchemeType pts : party.getPartyTaxSchemes()) {
            Element ptsElem = doc.createElementNS(NS_CAC, "cac:PartyTaxScheme");
            appendCbcIdentifier(doc, ptsElem, "CompanyID", pts.getCompanyID());
            if (pts.getTaxScheme() != null) {
                Element tsElem = doc.createElementNS(NS_CAC, "cac:TaxScheme");
                appendCbcIdentifier(doc, tsElem, "ID", pts.getTaxScheme().getId());
                ptsElem.appendChild(tsElem);
            }
            partyElem.appendChild(ptsElem);
        }

        // PartyLegalEntity
        if (party.getPartyLegalEntity() != null) {
            Element pleElem = doc.createElementNS(NS_CAC, "cac:PartyLegalEntity");
            appendCbcText(doc, pleElem, "RegistrationName", party.getPartyLegalEntity().getRegistrationName());
            appendCbcIdentifier(doc, pleElem, "CompanyID", party.getPartyLegalEntity().getCompanyID());
            if (party.getPartyLegalEntity().getCompanyLegalForm() != null) {
                appendCbcText(doc, pleElem, "CompanyLegalForm", party.getPartyLegalEntity().getCompanyLegalForm());
            }
            partyElem.appendChild(pleElem);
        }

        // Contact
        if (party.getContact() != null) {
            Element contactElem = doc.createElementNS(NS_CAC, "cac:Contact");
            appendCbcText(doc, contactElem, "Name", party.getContact().getName());
            appendCbcText(doc, contactElem, "Telephone", party.getContact().getTelephone());
            appendCbcText(doc, contactElem, "ElectronicMail", party.getContact().getElectronicMail());
            partyElem.appendChild(contactElem);
        }

        parent.appendChild(partyElem);
    }

    private void appendAddress(Document doc, Element parent, AddressType address) {
        Element elem = doc.createElementNS(NS_CAC, "cac:PostalAddress");
        appendCbcText(doc, elem, "StreetName", address.getStreetName());
        appendCbcText(doc, elem, "AdditionalStreetName", address.getAdditionalStreetName());
        appendCbcText(doc, elem, "CityName", address.getCityName());
        appendCbcText(doc, elem, "PostalZone", address.getPostalZone());
        appendCbcText(doc, elem, "CountrySubentity", address.getCountrySubentity());

        for (AddressLineType al : address.getAddressLines()) {
            Element alElem = doc.createElementNS(NS_CAC, "cac:AddressLine");
            appendCbcText(doc, alElem, "Line", al.getLine());
            elem.appendChild(alElem);
        }

        if (address.getCountry() != null) {
            Element countryElem = doc.createElementNS(NS_CAC, "cac:Country");
            appendCbcCode(doc, countryElem, "IdentificationCode", address.getCountry().getIdentificationCode());
            elem.appendChild(countryElem);
        }

        parent.appendChild(elem);
    }

    private void appendPaymentMeans(Document doc, Element parent, PaymentMeansType pm) {
        Element elem = doc.createElementNS(NS_CAC, "cac:PaymentMeans");
        appendCbcCode(doc, elem, "PaymentMeansCode", pm.getPaymentMeansCode());
        appendCbcIdentifier(doc, elem, "PaymentID", pm.getPaymentID());

        if (pm.getPayeeFinancialAccount() != null) {
            Element pfaElem = doc.createElementNS(NS_CAC, "cac:PayeeFinancialAccount");
            appendCbcIdentifier(doc, pfaElem, "ID", pm.getPayeeFinancialAccount().getId());
            appendCbcIdentifier(doc, pfaElem, "Name", pm.getPayeeFinancialAccount().getName());
            if (pm.getPayeeFinancialAccount().getFinancialInstitutionBranchID() != null) {
                Element fibElem = doc.createElementNS(NS_CAC, "cac:FinancialInstitutionBranch");
                appendCbcIdentifier(doc, fibElem, "ID", pm.getPayeeFinancialAccount().getFinancialInstitutionBranchID());
                pfaElem.appendChild(fibElem);
            }
            elem.appendChild(pfaElem);
        }

        parent.appendChild(elem);
    }

    private void appendAllowanceCharge(Document doc, Element parent, AllowanceChargeType ac) {
        Element elem = doc.createElementNS(NS_CAC, "cac:AllowanceCharge");
        appendCbcIndicator(doc, elem, "ChargeIndicator", ac.getChargeIndicator());
        appendCbcCode(doc, elem, "AllowanceChargeReasonCode", ac.getAllowanceChargeReasonCode());
        appendCbcText(doc, elem, "AllowanceChargeReason", ac.getAllowanceChargeReason());
        appendCbcNumeric(doc, elem, "MultiplierFactorNumeric", ac.getMultiplierFactorNumeric());
        appendCbcAmount(doc, elem, "Amount", ac.getAmount());
        appendCbcAmount(doc, elem, "BaseAmount", ac.getBaseAmount());

        if (ac.getTaxCategory() != null) {
            appendTaxCategory(doc, elem, ac.getTaxCategory());
        }

        parent.appendChild(elem);
    }

    private void appendTaxTotal(Document doc, Element parent, TaxTotalType tt) {
        Element elem = doc.createElementNS(NS_CAC, "cac:TaxTotal");
        appendCbcAmount(doc, elem, "TaxAmount", tt.getTaxAmount());

        for (TaxSubtotalType ts : tt.getTaxSubtotals()) {
            Element tsElem = doc.createElementNS(NS_CAC, "cac:TaxSubtotal");
            appendCbcAmount(doc, tsElem, "TaxableAmount", ts.getTaxableAmount());
            appendCbcAmount(doc, tsElem, "TaxAmount", ts.getTaxAmount());
            if (ts.getTaxCategory() != null) {
                appendTaxCategory(doc, tsElem, ts.getTaxCategory());
            }
            elem.appendChild(tsElem);
        }

        parent.appendChild(elem);
    }

    private void appendTaxCategory(Document doc, Element parent, TaxCategoryType tc) {
        Element elem = doc.createElementNS(NS_CAC, "cac:TaxCategory");
        appendCbcCode(doc, elem, "ID", tc.getId());
        appendCbcNumeric(doc, elem, "Percent", tc.getPercent());
        appendCbcCode(doc, elem, "TaxExemptionReasonCode", tc.getTaxExemptionReasonCode());
        appendCbcText(doc, elem, "TaxExemptionReason", tc.getTaxExemptionReason());

        if (tc.getTaxScheme() != null) {
            Element tsElem = doc.createElementNS(NS_CAC, "cac:TaxScheme");
            appendCbcIdentifier(doc, tsElem, "ID", tc.getTaxScheme().getId());
            elem.appendChild(tsElem);
        }

        parent.appendChild(elem);
    }

    private void appendMonetaryTotal(Document doc, Element parent, MonetaryTotalType mt) {
        Element elem = doc.createElementNS(NS_CAC, "cac:LegalMonetaryTotal");
        appendCbcAmount(doc, elem, "LineExtensionAmount", mt.getLineExtensionAmount());
        appendCbcAmount(doc, elem, "TaxExclusiveAmount", mt.getTaxExclusiveAmount());
        appendCbcAmount(doc, elem, "TaxInclusiveAmount", mt.getTaxInclusiveAmount());
        appendCbcAmount(doc, elem, "AllowanceTotalAmount", mt.getAllowanceTotalAmount());
        appendCbcAmount(doc, elem, "ChargeTotalAmount", mt.getChargeTotalAmount());
        appendCbcAmount(doc, elem, "PrepaidAmount", mt.getPrepaidAmount());
        appendCbcAmount(doc, elem, "PayableRoundingAmount", mt.getPayableRoundingAmount());
        appendCbcAmount(doc, elem, "PayableAmount", mt.getPayableAmount());
        parent.appendChild(elem);
    }

    private void appendInvoiceLine(Document doc, Element parent, InvoiceLineType line) {
        Element elem = doc.createElementNS(NS_CAC, "cac:InvoiceLine");

        appendCbcIdentifier(doc, elem, "ID", line.getId());
        appendCbcText(doc, elem, "Note", line.getNote());
        appendCbcQuantity(doc, elem, "InvoicedQuantity", line.getInvoicedQuantity());
        appendCbcAmount(doc, elem, "LineExtensionAmount", line.getLineExtensionAmount());
        appendCbcText(doc, elem, "AccountingCost", line.getAccountingCost());

        if (line.getInvoicePeriod() != null) {
            appendPeriod(doc, elem, "InvoicePeriod", line.getInvoicePeriod());
        }

        if (line.getOrderLineReference() != null) {
            Element olrElem = doc.createElementNS(NS_CAC, "cac:OrderLineReference");
            appendCbcIdentifier(doc, olrElem, "LineID", line.getOrderLineReference().getLineID());
            elem.appendChild(olrElem);
        }

        if (line.getDocumentReference() != null) {
            appendDocumentReference(doc, elem, "DocumentReference", line.getDocumentReference());
        }

        for (AllowanceChargeType ac : line.getAllowanceCharges()) {
            appendAllowanceCharge(doc, elem, ac);
        }

        if (line.getItem() != null) {
            appendItem(doc, elem, line.getItem());
        }

        if (line.getPrice() != null) {
            appendPrice(doc, elem, line.getPrice());
        }

        parent.appendChild(elem);
    }

    private void appendItem(Document doc, Element parent, ItemType item) {
        Element elem = doc.createElementNS(NS_CAC, "cac:Item");

        appendCbcText(doc, elem, "Description", item.getDescription());
        appendCbcText(doc, elem, "Name", item.getName());

        if (item.getBuyersItemIdentification() != null) {
            Element biiElem = doc.createElementNS(NS_CAC, "cac:BuyersItemIdentification");
            appendCbcIdentifier(doc, biiElem, "ID", item.getBuyersItemIdentification().getId());
            elem.appendChild(biiElem);
        }

        if (item.getSellersItemIdentification() != null) {
            Element siiElem = doc.createElementNS(NS_CAC, "cac:SellersItemIdentification");
            appendCbcIdentifier(doc, siiElem, "ID", item.getSellersItemIdentification().getId());
            elem.appendChild(siiElem);
        }

        if (item.getStandardItemIdentification() != null) {
            Element stdElem = doc.createElementNS(NS_CAC, "cac:StandardItemIdentification");
            appendCbcIdentifier(doc, stdElem, "ID", item.getStandardItemIdentification().getId());
            elem.appendChild(stdElem);
        }

        if (item.getOriginCountry() != null) {
            Element ocElem = doc.createElementNS(NS_CAC, "cac:OriginCountry");
            appendCbcCode(doc, ocElem, "IdentificationCode", item.getOriginCountry().getIdentificationCode());
            elem.appendChild(ocElem);
        }

        for (CommodityClassificationType cc : item.getCommodityClassifications()) {
            Element ccElem = doc.createElementNS(NS_CAC, "cac:CommodityClassification");
            appendCbcCode(doc, ccElem, "ItemClassificationCode", cc.getItemClassificationCode());
            elem.appendChild(ccElem);
        }

        if (item.getClassifiedTaxCategory() != null) {
            appendTaxCategory(doc, elem, item.getClassifiedTaxCategory());
        }

        for (ItemPropertyType prop : item.getAdditionalItemProperties()) {
            Element propElem = doc.createElementNS(NS_CAC, "cac:AdditionalItemProperty");
            appendCbcText(doc, propElem, "Name", prop.getName());
            appendCbcText(doc, propElem, "Value", prop.getValue());
            elem.appendChild(propElem);
        }

        parent.appendChild(elem);
    }

    private void appendPrice(Document doc, Element parent, PriceType price) {
        Element elem = doc.createElementNS(NS_CAC, "cac:Price");
        appendCbcAmount(doc, elem, "PriceAmount", price.getPriceAmount());
        appendCbcQuantity(doc, elem, "BaseQuantity", price.getBaseQuantity());

        if (price.getAllowanceCharge() != null) {
            appendAllowanceCharge(doc, elem, price.getAllowanceCharge());
        }

        parent.appendChild(elem);
    }

    private String formatDecimal(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}
