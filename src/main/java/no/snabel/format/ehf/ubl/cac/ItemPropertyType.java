package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.TextType;

/**
 * CAC ItemPropertyType - Additional item attributes (name-value pairs).
 */
public class ItemPropertyType {
    private TextType name;
    private TextType value;

    public ItemPropertyType() {
    }

    public ItemPropertyType(String name, String value) {
        this.name = new TextType(name);
        this.value = new TextType(value);
    }

    public TextType getName() {
        return name;
    }

    public void setName(TextType name) {
        this.name = name;
    }

    public TextType getValue() {
        return value;
    }

    public void setValue(TextType value) {
        this.value = value;
    }
}
