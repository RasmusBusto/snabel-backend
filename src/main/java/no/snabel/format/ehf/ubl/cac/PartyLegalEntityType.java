package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.IdentifierType;
import no.snabel.format.ehf.ubl.types.TextType;

/**
 * CAC PartyLegalEntityType - Legal entity information.
 */
public class PartyLegalEntityType {
    private TextType registrationName;
    private IdentifierType companyID;
    private TextType companyLegalForm;

    public PartyLegalEntityType() {
    }

    public TextType getRegistrationName() {
        return registrationName;
    }

    public void setRegistrationName(TextType registrationName) {
        this.registrationName = registrationName;
    }

    public IdentifierType getCompanyID() {
        return companyID;
    }

    public void setCompanyID(IdentifierType companyID) {
        this.companyID = companyID;
    }

    public TextType getCompanyLegalForm() {
        return companyLegalForm;
    }

    public void setCompanyLegalForm(TextType companyLegalForm) {
        this.companyLegalForm = companyLegalForm;
    }
}
