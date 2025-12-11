package no.snabel.format.ehf.ubl.cac;

/**
 * CAC SupplierPartyType - Accounting supplier party wrapper.
 */
public class SupplierPartyType {
    private PartyType party;

    public SupplierPartyType() {
    }

    public SupplierPartyType(PartyType party) {
        this.party = party;
    }

    public PartyType getParty() {
        return party;
    }

    public void setParty(PartyType party) {
        this.party = party;
    }
}
