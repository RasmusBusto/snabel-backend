package no.snabel.ehf.ubl.cac;

import no.snabel.ehf.ubl.types.IdentifierType;

/**
 * CAC TaxSchemeType - Tax scheme identification (typically "VAT").
 */
public class TaxSchemeType {
    private IdentifierType id;

    public TaxSchemeType() {
    }

    public TaxSchemeType(String id) {
        this.id = new IdentifierType(id);
    }

    public IdentifierType getId() {
        return id;
    }

    public void setId(IdentifierType id) {
        this.id = id;
    }
}
