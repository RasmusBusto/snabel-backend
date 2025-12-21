package no.snabel.format.ehf.ubl.types;

/**
 * UBL TextType - Represents text content with optional language metadata.
 * Used for names, descriptions, notes, reasons, etc.
 */
public class TextType {
    private String value;
    private String languageID;

    public TextType() {
    }

    public TextType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLanguageID() {
        return languageID;
    }

    public void setLanguageID(String languageID) {
        this.languageID = languageID;
    }
}
