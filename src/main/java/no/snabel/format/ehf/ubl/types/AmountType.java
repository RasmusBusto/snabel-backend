package no.snabel.format.ehf.ubl.types;

import java.math.BigDecimal;

/**
 * UBL AmountType - Represents a monetary amount with mandatory currency.
 * Used for all financial values in the invoice.
 */
public class AmountType {
    private BigDecimal value;
    private String currencyID;

    public AmountType() {
    }

    public AmountType(BigDecimal value, String currencyID) {
        this.value = value;
        this.currencyID = currencyID;
    }

    public AmountType(String value, String currencyID) {
        this.value = new BigDecimal(value);
        this.currencyID = currencyID;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getCurrencyID() {
        return currencyID;
    }

    public void setCurrencyID(String currencyID) {
        this.currencyID = currencyID;
    }
}
