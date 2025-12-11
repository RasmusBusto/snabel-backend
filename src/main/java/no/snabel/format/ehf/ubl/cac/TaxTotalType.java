package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.AmountType;
import java.util.ArrayList;
import java.util.List;

/**
 * CAC TaxTotalType - Total tax with subtotals by category.
 */
public class TaxTotalType {
    private AmountType taxAmount;
    private List<TaxSubtotalType> taxSubtotals;

    public TaxTotalType() {
        this.taxSubtotals = new ArrayList<>();
    }

    public AmountType getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(AmountType taxAmount) {
        this.taxAmount = taxAmount;
    }

    public List<TaxSubtotalType> getTaxSubtotals() {
        return taxSubtotals;
    }

    public void setTaxSubtotals(List<TaxSubtotalType> taxSubtotals) {
        this.taxSubtotals = taxSubtotals;
    }
}
