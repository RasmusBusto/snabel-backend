package no.snabel.format.ehf.ubl.types;

import java.time.LocalDate;

/**
 * UBL DateType - Represents a date value.
 * Used for issue date, due date, tax point date, etc.
 */
public class DateType {
    private LocalDate value;

    public DateType() {
    }

    public DateType(LocalDate value) {
        this.value = value;
    }

    public LocalDate getValue() {
        return value;
    }

    public void setValue(LocalDate value) {
        this.value = value;
    }
}
