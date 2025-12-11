package no.snabel.format.ehf.ubl.types;

/**
 * UBL IdentifierType - Represents an identifier with optional scheme metadata.
 * Used for IDs, endpoint IDs, party identifications, etc.
 */
public class IdentifierType {
    private String value;
    private String schemeID;
    private String schemeAgencyID;

    public IdentifierType() {
    }

    public IdentifierType(String value) {
        this.value = value;
    }

    public IdentifierType(String value, String schemeID) {
        this.value = value;
        this.schemeID = schemeID;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSchemeID() {
        return schemeID;
    }

    public void setSchemeID(String schemeID) {
        this.schemeID = schemeID;
    }

    public String getSchemeAgencyID() {
        return schemeAgencyID;
    }

    public void setSchemeAgencyID(String schemeAgencyID) {
        this.schemeAgencyID = schemeAgencyID;
    }
}
