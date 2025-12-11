package no.snabel.ehf.ubl.cac;

import no.snabel.ehf.ubl.types.CodeType;
import no.snabel.ehf.ubl.types.NumericType;
import no.snabel.ehf.ubl.types.TextType;

/**
 * CAC TaxCategoryType - VAT category with rate and exemption details.
 */
public class TaxCategoryType {
    private CodeType id;
    private NumericType percent;
    private CodeType taxExemptionReasonCode;
    private TextType taxExemptionReason;
    private TaxSchemeType taxScheme;

    public TaxCategoryType() {
    }

    public CodeType getId() {
        return id;
    }

    public void setId(CodeType id) {
        this.id = id;
    }

    public NumericType getPercent() {
        return percent;
    }

    public void setPercent(NumericType percent) {
        this.percent = percent;
    }

    public CodeType getTaxExemptionReasonCode() {
        return taxExemptionReasonCode;
    }

    public void setTaxExemptionReasonCode(CodeType taxExemptionReasonCode) {
        this.taxExemptionReasonCode = taxExemptionReasonCode;
    }

    public TextType getTaxExemptionReason() {
        return taxExemptionReason;
    }

    public void setTaxExemptionReason(TextType taxExemptionReason) {
        this.taxExemptionReason = taxExemptionReason;
    }

    public TaxSchemeType getTaxScheme() {
        return taxScheme;
    }

    public void setTaxScheme(TaxSchemeType taxScheme) {
        this.taxScheme = taxScheme;
    }
}
