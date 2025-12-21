package no.snabel.format.ehf.ubl.types;

import java.math.BigDecimal;

/**
 * UBL QuantityType - Represents a quantity with mandatory unit of measure.
 * Used for invoiced quantities, base quantities, etc.
 */
public class QuantityType {
    private BigDecimal value;
    private String unitCode;

    public QuantityType() {
    }

    public QuantityType(BigDecimal value, String unitCode) {
        this.value = value;
        this.unitCode = unitCode;
    }

    public QuantityType(String value, String unitCode) {
        this.value = new BigDecimal(value);
        this.unitCode = unitCode;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }
}
