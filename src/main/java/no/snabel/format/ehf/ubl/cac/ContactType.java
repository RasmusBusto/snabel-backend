package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.TextType;

/**
 * CAC ContactType - Contact information.
 */
public class ContactType {
    private TextType name;
    private TextType telephone;
    private TextType electronicMail;

    public ContactType() {
    }

    public TextType getName() {
        return name;
    }

    public void setName(TextType name) {
        this.name = name;
    }

    public TextType getTelephone() {
        return telephone;
    }

    public void setTelephone(TextType telephone) {
        this.telephone = telephone;
    }

    public TextType getElectronicMail() {
        return electronicMail;
    }

    public void setElectronicMail(TextType electronicMail) {
        this.electronicMail = electronicMail;
    }
}
