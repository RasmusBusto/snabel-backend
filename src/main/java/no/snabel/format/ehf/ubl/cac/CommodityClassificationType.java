package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.CodeType;

/**
 * CAC CommodityClassificationType - Product classification code.
 */
public class CommodityClassificationType {
    private CodeType itemClassificationCode;

    public CommodityClassificationType() {
    }

    public CommodityClassificationType(String code, String listID) {
        this.itemClassificationCode = new CodeType(code, listID);
    }

    public CodeType getItemClassificationCode() {
        return itemClassificationCode;
    }

    public void setItemClassificationCode(CodeType itemClassificationCode) {
        this.itemClassificationCode = itemClassificationCode;
    }
}
