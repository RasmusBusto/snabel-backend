package no.snabel.ehf.ubl.cac;

import no.snabel.ehf.ubl.types.IdentifierType;
import no.snabel.ehf.ubl.types.TextType;
import no.snabel.ehf.ubl.types.QuantityType;
import no.snabel.ehf.ubl.types.AmountType;
import java.util.ArrayList;
import java.util.List;

/**
 * CAC InvoiceLineType - Individual invoice line item.
 */
public class InvoiceLineType {
    private IdentifierType id;
    private TextType note;
    private QuantityType invoicedQuantity;
    private AmountType lineExtensionAmount;
    private TextType accountingCost;
    private PeriodType invoicePeriod;
    private OrderLineReferenceType orderLineReference;
    private DocumentReferenceType documentReference;
    private List<AllowanceChargeType> allowanceCharges;
    private ItemType item;
    private PriceType price;

    public InvoiceLineType() {
        this.allowanceCharges = new ArrayList<>();
    }

    public IdentifierType getId() {
        return id;
    }

    public void setId(IdentifierType id) {
        this.id = id;
    }

    public TextType getNote() {
        return note;
    }

    public void setNote(TextType note) {
        this.note = note;
    }

    public QuantityType getInvoicedQuantity() {
        return invoicedQuantity;
    }

    public void setInvoicedQuantity(QuantityType invoicedQuantity) {
        this.invoicedQuantity = invoicedQuantity;
    }

    public AmountType getLineExtensionAmount() {
        return lineExtensionAmount;
    }

    public void setLineExtensionAmount(AmountType lineExtensionAmount) {
        this.lineExtensionAmount = lineExtensionAmount;
    }

    public TextType getAccountingCost() {
        return accountingCost;
    }

    public void setAccountingCost(TextType accountingCost) {
        this.accountingCost = accountingCost;
    }

    public PeriodType getInvoicePeriod() {
        return invoicePeriod;
    }

    public void setInvoicePeriod(PeriodType invoicePeriod) {
        this.invoicePeriod = invoicePeriod;
    }

    public OrderLineReferenceType getOrderLineReference() {
        return orderLineReference;
    }

    public void setOrderLineReference(OrderLineReferenceType orderLineReference) {
        this.orderLineReference = orderLineReference;
    }

    public DocumentReferenceType getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReferenceType documentReference) {
        this.documentReference = documentReference;
    }

    public List<AllowanceChargeType> getAllowanceCharges() {
        return allowanceCharges;
    }

    public void setAllowanceCharges(List<AllowanceChargeType> allowanceCharges) {
        this.allowanceCharges = allowanceCharges;
    }

    public ItemType getItem() {
        return item;
    }

    public void setItem(ItemType item) {
        this.item = item;
    }

    public PriceType getPrice() {
        return price;
    }

    public void setPrice(PriceType price) {
        this.price = price;
    }
}
