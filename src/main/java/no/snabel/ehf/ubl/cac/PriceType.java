package no.snabel.ehf.ubl.cac;

import no.snabel.ehf.ubl.types.AmountType;
import no.snabel.ehf.ubl.types.QuantityType;

/**
 * CAC PriceType - Unit price details with optional discount.
 */
public class PriceType {
    private AmountType priceAmount;
    private QuantityType baseQuantity;
    private AllowanceChargeType allowanceCharge;

    public PriceType() {
    }

    public AmountType getPriceAmount() {
        return priceAmount;
    }

    public void setPriceAmount(AmountType priceAmount) {
        this.priceAmount = priceAmount;
    }

    public QuantityType getBaseQuantity() {
        return baseQuantity;
    }

    public void setBaseQuantity(QuantityType baseQuantity) {
        this.baseQuantity = baseQuantity;
    }

    public AllowanceChargeType getAllowanceCharge() {
        return allowanceCharge;
    }

    public void setAllowanceCharge(AllowanceChargeType allowanceCharge) {
        this.allowanceCharge = allowanceCharge;
    }
}
