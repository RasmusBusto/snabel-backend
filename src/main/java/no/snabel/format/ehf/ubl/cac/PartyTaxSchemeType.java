package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.IdentifierType;

/**
 * CAC PartyTaxSchemeType - Party's tax registration details.
 */
public class PartyTaxSchemeType {
    private IdentifierType companyID;
    private TaxSchemeType taxScheme;

    public PartyTaxSchemeType() {
    }

    public IdentifierType getCompanyID() {
        return companyID;
    }

    public void setCompanyID(IdentifierType companyID) {
        this.companyID = companyID;
    }

    public TaxSchemeType getTaxScheme() {
        return taxScheme;
    }

    public void setTaxScheme(TaxSchemeType taxScheme) {
        this.taxScheme = taxScheme;
    }
}
