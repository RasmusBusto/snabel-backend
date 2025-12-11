package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.DateType;
import no.snabel.format.ehf.ubl.types.CodeType;

/**
 * CAC PeriodType - Date period (invoice/delivery period).
 */
public class PeriodType {
    private DateType startDate;
    private DateType endDate;
    private CodeType descriptionCode;

    public PeriodType() {
    }

    public DateType getStartDate() {
        return startDate;
    }

    public void setStartDate(DateType startDate) {
        this.startDate = startDate;
    }

    public DateType getEndDate() {
        return endDate;
    }

    public void setEndDate(DateType endDate) {
        this.endDate = endDate;
    }

    public CodeType getDescriptionCode() {
        return descriptionCode;
    }

    public void setDescriptionCode(CodeType descriptionCode) {
        this.descriptionCode = descriptionCode;
    }
}
