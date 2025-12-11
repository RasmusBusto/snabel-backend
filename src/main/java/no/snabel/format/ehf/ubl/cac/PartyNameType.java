package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.TextType;

/**
 * CAC PartyNameType - Party name wrapper.
 */
public class PartyNameType {
    private TextType name;

    public PartyNameType() {
    }

    public PartyNameType(String name) {
        this.name = new TextType(name);
    }

    public TextType getName() {
        return name;
    }

    public void setName(TextType name) {
        this.name = name;
    }
}
