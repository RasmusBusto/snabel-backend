package no.snabel.ehf.ubl.cac;

import no.snabel.ehf.ubl.types.TextType;

/**
 * CAC AddressLineType - Additional address line.
 */
public class AddressLineType {
    private TextType line;

    public AddressLineType() {
    }

    public AddressLineType(String line) {
        this.line = new TextType(line);
    }

    public TextType getLine() {
        return line;
    }

    public void setLine(TextType line) {
        this.line = line;
    }
}
