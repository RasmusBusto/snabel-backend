package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.AmountType;

/**
 * CAC TaxSubtotalType - Individual VAT breakdown by category.
 */
public class TaxSubtotalType {
    private AmountType taxableAmount;
    private AmountType taxAmount;
    private TaxCategoryType taxCategory;

    public TaxSubtotalType() {
    }

    public AmountType getTaxableAmount() {
        return taxableAmount;
    }

    public void setTaxableAmount(AmountType taxableAmount) {
        this.taxableAmount = taxableAmount;
    }

    public AmountType getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(AmountType taxAmount) {
        this.taxAmount = taxAmount;
    }

    public TaxCategoryType getTaxCategory() {
        return taxCategory;
    }

    public void setTaxCategory(TaxCategoryType taxCategory) {
        this.taxCategory = taxCategory;
    }
}
