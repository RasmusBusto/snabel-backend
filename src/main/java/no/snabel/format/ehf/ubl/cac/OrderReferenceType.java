package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.IdentifierType;

/**
 * CAC OrderReferenceType - Purchase order reference.
 */
public class OrderReferenceType {
    private IdentifierType id;
    private IdentifierType salesOrderID;

    public OrderReferenceType() {
    }

    public OrderReferenceType(String id) {
        this.id = new IdentifierType(id);
    }

    public IdentifierType getId() {
        return id;
    }

    public void setId(IdentifierType id) {
        this.id = id;
    }

    public IdentifierType getSalesOrderID() {
        return salesOrderID;
    }

    public void setSalesOrderID(IdentifierType salesOrderID) {
        this.salesOrderID = salesOrderID;
    }
}
