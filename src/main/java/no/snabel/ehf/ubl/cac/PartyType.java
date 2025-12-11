package no.snabel.ehf.ubl.cac;

import no.snabel.ehf.ubl.types.IdentifierType;
import java.util.ArrayList;
import java.util.List;

/**
 * CAC PartyType - Complete party (supplier/customer) structure.
 */
public class PartyType {
    private IdentifierType endpointID;
    private List<PartyIdentificationType> partyIdentifications;
    private PartyNameType partyName;
    private AddressType postalAddress;
    private List<PartyTaxSchemeType> partyTaxSchemes;
    private PartyLegalEntityType partyLegalEntity;
    private ContactType contact;

    public PartyType() {
        this.partyIdentifications = new ArrayList<>();
        this.partyTaxSchemes = new ArrayList<>();
    }

    public IdentifierType getEndpointID() {
        return endpointID;
    }

    public void setEndpointID(IdentifierType endpointID) {
        this.endpointID = endpointID;
    }

    public List<PartyIdentificationType> getPartyIdentifications() {
        return partyIdentifications;
    }

    public void setPartyIdentifications(List<PartyIdentificationType> partyIdentifications) {
        this.partyIdentifications = partyIdentifications;
    }

    public PartyNameType getPartyName() {
        return partyName;
    }

    public void setPartyName(PartyNameType partyName) {
        this.partyName = partyName;
    }

    public AddressType getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(AddressType postalAddress) {
        this.postalAddress = postalAddress;
    }

    public List<PartyTaxSchemeType> getPartyTaxSchemes() {
        return partyTaxSchemes;
    }

    public void setPartyTaxSchemes(List<PartyTaxSchemeType> partyTaxSchemes) {
        this.partyTaxSchemes = partyTaxSchemes;
    }

    public PartyLegalEntityType getPartyLegalEntity() {
        return partyLegalEntity;
    }

    public void setPartyLegalEntity(PartyLegalEntityType partyLegalEntity) {
        this.partyLegalEntity = partyLegalEntity;
    }

    public ContactType getContact() {
        return contact;
    }

    public void setContact(ContactType contact) {
        this.contact = contact;
    }
}
