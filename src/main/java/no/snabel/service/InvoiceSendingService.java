package no.snabel.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.snabel.model.Invoice;
import org.jboss.logging.Logger;

/**
 * Invoice Sending Service - Orchestrates sending invoices via different channels
 *
 * This service coordinates the sending of invoices through various delivery methods:
 * - EHF (B2B) - PEPPOL network via Oxalis
 * - eFaktura B2C - Norwegian banking system (future)
 * - Email - PDF/XML attachments (future)
 *
 * Status: Stub implementation ready for integration
 *
 * TODO:
 * 1. Implement email sending (InvoiceEmailService)
 * 2. Implement eFaktura B2C (EFakturaB2CService)
 * 3. Add delivery tracking (InvoiceDelivery entity)
 * 4. Implement retry logic
 * 5. Add status polling
 */
@ApplicationScoped
public class InvoiceSendingService {

    private static final Logger LOG = Logger.getLogger(InvoiceSendingService.class);

    @Inject
    PeppolAccessPointService peppolService;

    // TODO: Inject when implemented
    // @Inject
    // InvoiceEmailService emailService;

    // @Inject
    // EFakturaB2CService eFakturaB2CService;

    // @Inject
    // InvoiceValidationService validationService;

    /**
     * Send invoice via specified delivery method
     *
     * @param invoice Invoice to send
     * @param method Delivery method (EHF, EFAKTURA_B2C, EMAIL)
     * @return Result of sending operation
     */
    public Uni<SendResult> sendInvoice(Invoice invoice, DeliveryMethod method) {
        LOG.infof("Sending invoice %s via %s", invoice.invoiceNumber, method);

        // TODO: Add validation before sending
        // return validationService.validateForSending(invoice)
        //     .chain(validationResult -> {
        //         if (!validationResult.isValid()) {
        //             return Uni.createFrom().failure(
        //                 new IllegalArgumentException("Invoice validation failed: " + validationResult.errors)
        //             );
        //         }
        //         return sendViaChannel(invoice, method);
        //     });

        return sendViaChannel(invoice, method);
    }

    /**
     * Route to appropriate delivery channel
     */
    private Uni<SendResult> sendViaChannel(Invoice invoice, DeliveryMethod method) {
        return switch (method) {
            case EHF -> sendViaEHF(invoice);
            case EFAKTURA_B2C -> sendViaEFakturaB2C(invoice);
            case EMAIL -> sendViaEmail(invoice);
        };
    }

    /**
     * Send via PEPPOL network (B2B EHF)
     */
    private Uni<SendResult> sendViaEHF(Invoice invoice) {
        if (!peppolService.isReady()) {
            return Uni.createFrom().failure(
                new UnsupportedOperationException(
                    "PEPPOL sending is not configured. See docs/OXALIS-SETUP.md for setup instructions."
                )
            );
        }

        return peppolService.sendInvoice(invoice)
            .map(peppolResult -> {
                SendResult result = new SendResult();
                result.success = peppolResult.success;
                result.deliveryMethod = DeliveryMethod.EHF;
                result.messageId = peppolResult.messageId;
                result.details = peppolResult.toJson();
                result.message = "Invoice sent via PEPPOL network";
                return result;
            })
            .onFailure().recoverWithItem(error -> {
                LOG.errorf(error, "Failed to send invoice %s via PEPPOL", invoice.invoiceNumber);
                SendResult result = new SendResult();
                result.success = false;
                result.deliveryMethod = DeliveryMethod.EHF;
                result.errorMessage = error.getMessage();
                result.message = "PEPPOL transmission failed";
                return result;
            });
    }

    /**
     * Send via eFaktura B2C (Norwegian banking system)
     * TODO: Implement when EFakturaB2CService is ready
     */
    private Uni<SendResult> sendViaEFakturaB2C(Invoice invoice) {
        LOG.warn("eFaktura B2C sending not yet implemented");
        return Uni.createFrom().failure(
            new UnsupportedOperationException(
                "eFaktura B2C sending is not yet implemented. " +
                "TODO: Implement EFakturaB2CService with Nets/Vipps integration."
            )
        );
    }

    /**
     * Send via email with PDF/XML attachments
     * TODO: Implement when InvoiceEmailService is ready
     */
    private Uni<SendResult> sendViaEmail(Invoice invoice) {
        LOG.warn("Email sending not yet implemented");
        return Uni.createFrom().failure(
            new UnsupportedOperationException(
                "Email sending is not yet implemented. " +
                "TODO: Implement InvoiceEmailService with Quarkus Mailer."
            )
        );
    }

    /**
     * Check which delivery methods are available
     */
    public DeliveryCapabilities getCapabilities() {
        DeliveryCapabilities caps = new DeliveryCapabilities();
        caps.ehfAvailable = peppolService.isReady();
        caps.eFakturaB2CAvailable = false; // TODO: Check when implemented
        caps.emailAvailable = false; // TODO: Check when implemented
        return caps;
    }

    /**
     * Delivery method enumeration
     */
    public enum DeliveryMethod {
        EHF,           // B2B via PEPPOL
        EFAKTURA_B2C,  // B2C via banking system
        EMAIL          // Email with attachments
    }

    /**
     * Result of sending operation
     */
    public static class SendResult {
        public boolean success;
        public DeliveryMethod deliveryMethod;
        public String messageId;
        public String message;
        public String details;
        public String errorMessage;
    }

    /**
     * Available delivery capabilities
     */
    public static class DeliveryCapabilities {
        public boolean ehfAvailable;
        public boolean eFakturaB2CAvailable;
        public boolean emailAvailable;

        public String getAvailableMethods() {
            StringBuilder methods = new StringBuilder();
            if (ehfAvailable) methods.append("EHF ");
            if (eFakturaB2CAvailable) methods.append("EFAKTURA_B2C ");
            if (emailAvailable) methods.append("EMAIL ");
            return methods.toString().trim();
        }
    }
}
