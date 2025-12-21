package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.IdentifierType;

/**
 * CAC PartyIdentificationType - Party identifier (organization number, GLN, etc.).
 */
public class PartyIdentificationType {
    private IdentifierType id;

    public PartyIdentificationType() {
    }

    public PartyIdentificationType(String id) {
        this.id = new IdentifierType(id);
    }

    public PartyIdentificationType(String id, String schemeID) {
        this.id = new IdentifierType(id, schemeID);
    }

    public IdentifierType getId() {
        return id;
    }

    public void setId(IdentifierType id) {
        this.id = id;
    }
}
