package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.IdentifierType;

/**
 * CAC ItemIdentificationType - Item identifier (buyers/sellers/standard item ID).
 */
public class ItemIdentificationType {
    private IdentifierType id;

    public ItemIdentificationType() {
    }

    public ItemIdentificationType(String id) {
        this.id = new IdentifierType(id);
    }

    public ItemIdentificationType(String id, String schemeID) {
        this.id = new IdentifierType(id, schemeID);
    }

    public IdentifierType getId() {
        return id;
    }

    public void setId(IdentifierType id) {
        this.id = id;
    }
}
