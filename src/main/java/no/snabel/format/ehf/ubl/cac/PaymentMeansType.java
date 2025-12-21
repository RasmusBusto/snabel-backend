package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.CodeType;
import no.snabel.format.ehf.ubl.types.IdentifierType;

/**
 * CAC PaymentMeansType - Payment method details.
 */
public class PaymentMeansType {
    private CodeType paymentMeansCode;
    private IdentifierType paymentID;
    private FinancialAccountType payeeFinancialAccount;

    public PaymentMeansType() {
    }

    public CodeType getPaymentMeansCode() {
        return paymentMeansCode;
    }

    public void setPaymentMeansCode(CodeType paymentMeansCode) {
        this.paymentMeansCode = paymentMeansCode;
    }

    public IdentifierType getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(IdentifierType paymentID) {
        this.paymentID = paymentID;
    }

    public FinancialAccountType getPayeeFinancialAccount() {
        return payeeFinancialAccount;
    }

    public void setPayeeFinancialAccount(FinancialAccountType payeeFinancialAccount) {
        this.payeeFinancialAccount = payeeFinancialAccount;
    }
}
