package no.snabel.format.ehf.ubl.types;

/**
 * UBL CodeType - Represents a code value with optional list metadata.
 * Used for type codes, reason codes, classification codes, etc.
 */
public class CodeType {
    private String value;
    private String listID;
    private String listAgencyID;
    private String listVersionID;
    private String name;

    public CodeType() {
    }

    public CodeType(String value) {
        this.value = value;
    }

    public CodeType(String value, String listID) {
        this.value = value;
        this.listID = listID;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getListID() {
        return listID;
    }

    public void setListID(String listID) {
        this.listID = listID;
    }

    public String getListAgencyID() {
        return listAgencyID;
    }

    public void setListAgencyID(String listAgencyID) {
        this.listAgencyID = listAgencyID;
    }

    public String getListVersionID() {
        return listVersionID;
    }

    public void setListVersionID(String listVersionID) {
        this.listVersionID = listVersionID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
