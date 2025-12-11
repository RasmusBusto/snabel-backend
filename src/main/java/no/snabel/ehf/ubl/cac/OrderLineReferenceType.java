package no.snabel.ehf.ubl.cac;

import no.snabel.ehf.ubl.types.IdentifierType;

/**
 * CAC OrderLineReferenceType - Purchase order line reference.
 */
public class OrderLineReferenceType {
    private IdentifierType lineID;

    public OrderLineReferenceType() {
    }

    public OrderLineReferenceType(String lineID) {
        this.lineID = new IdentifierType(lineID);
    }

    public IdentifierType getLineID() {
        return lineID;
    }

    public void setLineID(IdentifierType lineID) {
        this.lineID = lineID;
    }
}
