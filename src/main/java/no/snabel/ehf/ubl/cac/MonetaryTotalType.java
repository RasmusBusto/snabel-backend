package no.snabel.ehf.ubl.cac;

import no.snabel.ehf.ubl.types.AmountType;

/**
 * CAC MonetaryTotalType (LegalMonetaryTotal) - Document-level monetary totals.
 */
public class MonetaryTotalType {
    private AmountType lineExtensionAmount;
    private AmountType taxExclusiveAmount;
    private AmountType taxInclusiveAmount;
    private AmountType allowanceTotalAmount;
    private AmountType chargeTotalAmount;
    private AmountType prepaidAmount;
    private AmountType payableRoundingAmount;
    private AmountType payableAmount;

    public MonetaryTotalType() {
    }

    public AmountType getLineExtensionAmount() {
        return lineExtensionAmount;
    }

    public void setLineExtensionAmount(AmountType lineExtensionAmount) {
        this.lineExtensionAmount = lineExtensionAmount;
    }

    public AmountType getTaxExclusiveAmount() {
        return taxExclusiveAmount;
    }

    public void setTaxExclusiveAmount(AmountType taxExclusiveAmount) {
        this.taxExclusiveAmount = taxExclusiveAmount;
    }

    public AmountType getTaxInclusiveAmount() {
        return taxInclusiveAmount;
    }

    public void setTaxInclusiveAmount(AmountType taxInclusiveAmount) {
        this.taxInclusiveAmount = taxInclusiveAmount;
    }

    public AmountType getAllowanceTotalAmount() {
        return allowanceTotalAmount;
    }

    public void setAllowanceTotalAmount(AmountType allowanceTotalAmount) {
        this.allowanceTotalAmount = allowanceTotalAmount;
    }

    public AmountType getChargeTotalAmount() {
        return chargeTotalAmount;
    }

    public void setChargeTotalAmount(AmountType chargeTotalAmount) {
        this.chargeTotalAmount = chargeTotalAmount;
    }

    public AmountType getPrepaidAmount() {
        return prepaidAmount;
    }

    public void setPrepaidAmount(AmountType prepaidAmount) {
        this.prepaidAmount = prepaidAmount;
    }

    public AmountType getPayableRoundingAmount() {
        return payableRoundingAmount;
    }

    public void setPayableRoundingAmount(AmountType payableRoundingAmount) {
        this.payableRoundingAmount = payableRoundingAmount;
    }

    public AmountType getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(AmountType payableAmount) {
        this.payableAmount = payableAmount;
    }
}
