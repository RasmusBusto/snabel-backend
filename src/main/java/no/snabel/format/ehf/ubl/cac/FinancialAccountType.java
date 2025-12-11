package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.IdentifierType;

/**
 * CAC FinancialAccountType - Bank account details.
 */
public class FinancialAccountType {
    private IdentifierType id;
    private IdentifierType name;
    private IdentifierType financialInstitutionBranchID;

    public FinancialAccountType() {
    }

    public IdentifierType getId() {
        return id;
    }

    public void setId(IdentifierType id) {
        this.id = id;
    }

    public IdentifierType getName() {
        return name;
    }

    public void setName(IdentifierType name) {
        this.name = name;
    }

    public IdentifierType getFinancialInstitutionBranchID() {
        return financialInstitutionBranchID;
    }

    public void setFinancialInstitutionBranchID(IdentifierType financialInstitutionBranchID) {
        this.financialInstitutionBranchID = financialInstitutionBranchID;
    }
}
