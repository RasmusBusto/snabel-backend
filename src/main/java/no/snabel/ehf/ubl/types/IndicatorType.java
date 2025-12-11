package no.snabel.ehf.ubl.types;

/**
 * UBL IndicatorType - Represents a boolean indicator.
 * Used for charge indicators, copy indicators, etc.
 */
public class IndicatorType {
    private Boolean value;

    public IndicatorType() {
    }

    public IndicatorType(Boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
