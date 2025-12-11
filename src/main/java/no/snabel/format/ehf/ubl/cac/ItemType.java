package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.TextType;
import java.util.ArrayList;
import java.util.List;

/**
 * CAC ItemType - Product/service details.
 */
public class ItemType {
    private TextType description;
    private TextType name;
    private ItemIdentificationType buyersItemIdentification;
    private ItemIdentificationType sellersItemIdentification;
    private ItemIdentificationType standardItemIdentification;
    private CountryType originCountry;
    private List<CommodityClassificationType> commodityClassifications;
    private TaxCategoryType classifiedTaxCategory;
    private List<ItemPropertyType> additionalItemProperties;

    public ItemType() {
        this.commodityClassifications = new ArrayList<>();
        this.additionalItemProperties = new ArrayList<>();
    }

    public TextType getDescription() {
        return description;
    }

    public void setDescription(TextType description) {
        this.description = description;
    }

    public TextType getName() {
        return name;
    }

    public void setName(TextType name) {
        this.name = name;
    }

    public ItemIdentificationType getBuyersItemIdentification() {
        return buyersItemIdentification;
    }

    public void setBuyersItemIdentification(ItemIdentificationType buyersItemIdentification) {
        this.buyersItemIdentification = buyersItemIdentification;
    }

    public ItemIdentificationType getSellersItemIdentification() {
        return sellersItemIdentification;
    }

    public void setSellersItemIdentification(ItemIdentificationType sellersItemIdentification) {
        this.sellersItemIdentification = sellersItemIdentification;
    }

    public ItemIdentificationType getStandardItemIdentification() {
        return standardItemIdentification;
    }

    public void setStandardItemIdentification(ItemIdentificationType standardItemIdentification) {
        this.standardItemIdentification = standardItemIdentification;
    }

    public CountryType getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(CountryType originCountry) {
        this.originCountry = originCountry;
    }

    public List<CommodityClassificationType> getCommodityClassifications() {
        return commodityClassifications;
    }

    public void setCommodityClassifications(List<CommodityClassificationType> commodityClassifications) {
        this.commodityClassifications = commodityClassifications;
    }

    public TaxCategoryType getClassifiedTaxCategory() {
        return classifiedTaxCategory;
    }

    public void setClassifiedTaxCategory(TaxCategoryType classifiedTaxCategory) {
        this.classifiedTaxCategory = classifiedTaxCategory;
    }

    public List<ItemPropertyType> getAdditionalItemProperties() {
        return additionalItemProperties;
    }

    public void setAdditionalItemProperties(List<ItemPropertyType> additionalItemProperties) {
        this.additionalItemProperties = additionalItemProperties;
    }
}
