package no.snabel.ehf.ubl.types;

import java.math.BigDecimal;

/**
 * UBL NumericType - Represents a numeric value.
 * Used for percentages, multipliers, etc.
 */
public class NumericType {
    private BigDecimal value;

    public NumericType() {
    }

    public NumericType(BigDecimal value) {
        this.value = value;
    }

    public NumericType(String value) {
        this.value = new BigDecimal(value);
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
