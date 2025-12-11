package no.snabel.ehf.ubl.cac;

import no.snabel.ehf.ubl.types.IndicatorType;
import no.snabel.ehf.ubl.types.CodeType;
import no.snabel.ehf.ubl.types.TextType;
import no.snabel.ehf.ubl.types.NumericType;
import no.snabel.ehf.ubl.types.AmountType;

/**
 * CAC AllowanceChargeType - Document or line-level charges and allowances.
 */
public class AllowanceChargeType {
    private IndicatorType chargeIndicator;
    private CodeType allowanceChargeReasonCode;
    private TextType allowanceChargeReason;
    private NumericType multiplierFactorNumeric;
    private AmountType amount;
    private AmountType baseAmount;
    private TaxCategoryType taxCategory;

    public AllowanceChargeType() {
    }

    public IndicatorType getChargeIndicator() {
        return chargeIndicator;
    }

    public void setChargeIndicator(IndicatorType chargeIndicator) {
        this.chargeIndicator = chargeIndicator;
    }

    public CodeType getAllowanceChargeReasonCode() {
        return allowanceChargeReasonCode;
    }

    public void setAllowanceChargeReasonCode(CodeType allowanceChargeReasonCode) {
        this.allowanceChargeReasonCode = allowanceChargeReasonCode;
    }

    public TextType getAllowanceChargeReason() {
        return allowanceChargeReason;
    }

    public void setAllowanceChargeReason(TextType allowanceChargeReason) {
        this.allowanceChargeReason = allowanceChargeReason;
    }

    public NumericType getMultiplierFactorNumeric() {
        return multiplierFactorNumeric;
    }

    public void setMultiplierFactorNumeric(NumericType multiplierFactorNumeric) {
        this.multiplierFactorNumeric = multiplierFactorNumeric;
    }

    public AmountType getAmount() {
        return amount;
    }

    public void setAmount(AmountType amount) {
        this.amount = amount;
    }

    public AmountType getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(AmountType baseAmount) {
        this.baseAmount = baseAmount;
    }

    public TaxCategoryType getTaxCategory() {
        return taxCategory;
    }

    public void setTaxCategory(TaxCategoryType taxCategory) {
        this.taxCategory = taxCategory;
    }
}
