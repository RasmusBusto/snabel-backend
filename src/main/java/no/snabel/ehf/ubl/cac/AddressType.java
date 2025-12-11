package no.snabel.ehf.ubl.cac;

import no.snabel.ehf.ubl.types.TextType;
import java.util.ArrayList;
import java.util.List;

/**
 * CAC AddressType - Postal address structure.
 */
public class AddressType {
    private TextType streetName;
    private TextType additionalStreetName;
    private TextType cityName;
    private TextType postalZone;
    private TextType countrySubentity;
    private List<AddressLineType> addressLines;
    private CountryType country;

    public AddressType() {
        this.addressLines = new ArrayList<>();
    }

    public TextType getStreetName() {
        return streetName;
    }

    public void setStreetName(TextType streetName) {
        this.streetName = streetName;
    }

    public TextType getAdditionalStreetName() {
        return additionalStreetName;
    }

    public void setAdditionalStreetName(TextType additionalStreetName) {
        this.additionalStreetName = additionalStreetName;
    }

    public TextType getCityName() {
        return cityName;
    }

    public void setCityName(TextType cityName) {
        this.cityName = cityName;
    }

    public TextType getPostalZone() {
        return postalZone;
    }

    public void setPostalZone(TextType postalZone) {
        this.postalZone = postalZone;
    }

    public TextType getCountrySubentity() {
        return countrySubentity;
    }

    public void setCountrySubentity(TextType countrySubentity) {
        this.countrySubentity = countrySubentity;
    }

    public List<AddressLineType> getAddressLines() {
        return addressLines;
    }

    public void setAddressLines(List<AddressLineType> addressLines) {
        this.addressLines = addressLines;
    }

    public CountryType getCountry() {
        return country;
    }

    public void setCountry(CountryType country) {
        this.country = country;
    }
}
