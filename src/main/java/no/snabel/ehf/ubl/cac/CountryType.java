package no.snabel.ehf.ubl.cac;

import no.snabel.ehf.ubl.types.CodeType;

/**
 * CAC CountryType - Country identification.
 */
public class CountryType {
    private CodeType identificationCode;

    public CountryType() {
    }

    public CountryType(String countryCode) {
        this.identificationCode = new CodeType(countryCode);
    }

    public CodeType getIdentificationCode() {
        return identificationCode;
    }

    public void setIdentificationCode(CodeType identificationCode) {
        this.identificationCode = identificationCode;
    }
}
