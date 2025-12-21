package no.snabel.format.ehf.ubl.cac;

/**
 * CAC CustomerPartyType - Accounting customer party wrapper.
 */
public class CustomerPartyType {
    private PartyType party;

    public CustomerPartyType() {
    }

    public CustomerPartyType(PartyType party) {
        this.party = party;
    }

    public PartyType getParty() {
        return party;
    }

    public void setParty(PartyType party) {
        this.party = party;
    }
}
