package no.snabel.format.ehf.ubl;

import no.snabel.format.ehf.ubl.cac.*;
import no.snabel.format.ehf.ubl.types.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UBL InvoiceType - Root invoice document for PEPPOL BIS Billing 3.0 / EHF 3.0.
 *
 * Represents a complete electronic invoice compliant with:
 * - PEPPOL BIS Billing 3.0
 * - Norwegian EHF 3.0
 * - EN16931 (European e-invoicing standard)
 */
public class InvoiceType {

    // === Core Invoice Metadata (Mandatory) ===
    private IdentifierType customizationID;
    private IdentifierType profileID;
    private IdentifierType id;
    private DateType issueDate;
    private CodeType invoiceTypeCode;
    private CodeType documentCurrencyCode;

    // === Optional Invoice Headers ===
    private DateType dueDate;
    private TextType note;
    private DateType taxPointDate;
    private CodeType taxCurrencyCode;
    private TextType accountingCost;
    private TextType buyerReference;

    // === Period & Reference Information ===
    private PeriodType invoicePeriod;
    private OrderReferenceType orderReference;
    private List<DocumentReferenceType> billingReferences;
    private DocumentReferenceType despatchDocumentReference;
    private DocumentReferenceType receiptDocumentReference;
    private DocumentReferenceType originatorDocumentReference;
    private DocumentReferenceType contractDocumentReference;
    private List<DocumentReferenceType> additionalDocumentReferences;

    // === Party Information (Mandatory) ===
    private SupplierPartyType accountingSupplierParty;
    private CustomerPartyType accountingCustomerParty;

    // === Optional Party Information ===
    private PartyType payeeParty;
    private PartyType taxRepresentativeParty;

    // === Payment Information ===
    private List<PaymentMeansType> paymentMeans;
    private TextType paymentTerms;

    // === Financial Charges & Allowances ===
    private List<AllowanceChargeType> allowanceCharges;

    // === Tax Information (Mandatory) ===
    private List<TaxTotalType> taxTotals;

    // === Document Totals (Mandatory) ===
    private MonetaryTotalType legalMonetaryTotal;

    // === Invoice Lines (Mandatory, at least 1) ===
    private List<InvoiceLineType> invoiceLines;

    public InvoiceType() {
        this.billingReferences = new ArrayList<>();
        this.additionalDocumentReferences = new ArrayList<>();
        this.paymentMeans = new ArrayList<>();
        this.allowanceCharges = new ArrayList<>();
        this.taxTotals = new ArrayList<>();
        this.invoiceLines = new ArrayList<>();
    }

    // === Getters and Setters ===

    public IdentifierType getCustomizationID() {
        return customizationID;
    }

    public void setCustomizationID(IdentifierType customizationID) {
        this.customizationID = customizationID;
    }

    public IdentifierType getProfileID() {
        return profileID;
    }

    public void setProfileID(IdentifierType profileID) {
        this.profileID = profileID;
    }

    public IdentifierType getId() {
        return id;
    }

    public void setId(IdentifierType id) {
        this.id = id;
    }

    public DateType getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(DateType issueDate) {
        this.issueDate = issueDate;
    }

    public CodeType getInvoiceTypeCode() {
        return invoiceTypeCode;
    }

    public void setInvoiceTypeCode(CodeType invoiceTypeCode) {
        this.invoiceTypeCode = invoiceTypeCode;
    }

    public CodeType getDocumentCurrencyCode() {
        return documentCurrencyCode;
    }

    public void setDocumentCurrencyCode(CodeType documentCurrencyCode) {
        this.documentCurrencyCode = documentCurrencyCode;
    }

    public DateType getDueDate() {
        return dueDate;
    }

    public void setDueDate(DateType dueDate) {
        this.dueDate = dueDate;
    }

    public TextType getNote() {
        return note;
    }

    public void setNote(TextType note) {
        this.note = note;
    }

    public DateType getTaxPointDate() {
        return taxPointDate;
    }

    public void setTaxPointDate(DateType taxPointDate) {
        this.taxPointDate = taxPointDate;
    }

    public CodeType getTaxCurrencyCode() {
        return taxCurrencyCode;
    }

    public void setTaxCurrencyCode(CodeType taxCurrencyCode) {
        this.taxCurrencyCode = taxCurrencyCode;
    }

    public TextType getAccountingCost() {
        return accountingCost;
    }

    public void setAccountingCost(TextType accountingCost) {
        this.accountingCost = accountingCost;
    }

    public TextType getBuyerReference() {
        return buyerReference;
    }

    public void setBuyerReference(TextType buyerReference) {
        this.buyerReference = buyerReference;
    }

    public PeriodType getInvoicePeriod() {
        return invoicePeriod;
    }

    public void setInvoicePeriod(PeriodType invoicePeriod) {
        this.invoicePeriod = invoicePeriod;
    }

    public OrderReferenceType getOrderReference() {
        return orderReference;
    }

    public void setOrderReference(OrderReferenceType orderReference) {
        this.orderReference = orderReference;
    }

    public List<DocumentReferenceType> getBillingReferences() {
        return billingReferences;
    }

    public void setBillingReferences(List<DocumentReferenceType> billingReferences) {
        this.billingReferences = billingReferences;
    }

    public DocumentReferenceType getDespatchDocumentReference() {
        return despatchDocumentReference;
    }

    public void setDespatchDocumentReference(DocumentReferenceType despatchDocumentReference) {
        this.despatchDocumentReference = despatchDocumentReference;
    }

    public DocumentReferenceType getReceiptDocumentReference() {
        return receiptDocumentReference;
    }

    public void setReceiptDocumentReference(DocumentReferenceType receiptDocumentReference) {
        this.receiptDocumentReference = receiptDocumentReference;
    }

    public DocumentReferenceType getOriginatorDocumentReference() {
        return originatorDocumentReference;
    }

    public void setOriginatorDocumentReference(DocumentReferenceType originatorDocumentReference) {
        this.originatorDocumentReference = originatorDocumentReference;
    }

    public DocumentReferenceType getContractDocumentReference() {
        return contractDocumentReference;
    }

    public void setContractDocumentReference(DocumentReferenceType contractDocumentReference) {
        this.contractDocumentReference = contractDocumentReference;
    }

    public List<DocumentReferenceType> getAdditionalDocumentReferences() {
        return additionalDocumentReferences;
    }

    public void setAdditionalDocumentReferences(List<DocumentReferenceType> additionalDocumentReferences) {
        this.additionalDocumentReferences = additionalDocumentReferences;
    }

    public SupplierPartyType getAccountingSupplierParty() {
        return accountingSupplierParty;
    }

    public void setAccountingSupplierParty(SupplierPartyType accountingSupplierParty) {
        this.accountingSupplierParty = accountingSupplierParty;
    }

    public CustomerPartyType getAccountingCustomerParty() {
        return accountingCustomerParty;
    }

    public void setAccountingCustomerParty(CustomerPartyType accountingCustomerParty) {
        this.accountingCustomerParty = accountingCustomerParty;
    }

    public PartyType getPayeeParty() {
        return payeeParty;
    }

    public void setPayeeParty(PartyType payeeParty) {
        this.payeeParty = payeeParty;
    }

    public PartyType getTaxRepresentativeParty() {
        return taxRepresentativeParty;
    }

    public void setTaxRepresentativeParty(PartyType taxRepresentativeParty) {
        this.taxRepresentativeParty = taxRepresentativeParty;
    }

    public List<PaymentMeansType> getPaymentMeans() {
        return paymentMeans;
    }

    public void setPaymentMeans(List<PaymentMeansType> paymentMeans) {
        this.paymentMeans = paymentMeans;
    }

    public TextType getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(TextType paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public List<AllowanceChargeType> getAllowanceCharges() {
        return allowanceCharges;
    }

    public void setAllowanceCharges(List<AllowanceChargeType> allowanceCharges) {
        this.allowanceCharges = allowanceCharges;
    }

    public List<TaxTotalType> getTaxTotals() {
        return taxTotals;
    }

    public void setTaxTotals(List<TaxTotalType> taxTotals) {
        this.taxTotals = taxTotals;
    }

    public MonetaryTotalType getLegalMonetaryTotal() {
        return legalMonetaryTotal;
    }

    public void setLegalMonetaryTotal(MonetaryTotalType legalMonetaryTotal) {
        this.legalMonetaryTotal = legalMonetaryTotal;
    }

    public List<InvoiceLineType> getInvoiceLines() {
        return invoiceLines;
    }

    public void setInvoiceLines(List<InvoiceLineType> invoiceLines) {
        this.invoiceLines = invoiceLines;
    }
}
