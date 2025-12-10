package no.snabel.service;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.outbound.OxalisOutboundComponent;
import network.oxalis.outbound.transmission.TransmissionRequestBuilder;
import network.oxalis.outbound.transmission.Transmitter;
import network.oxalis.vefa.peppol.common.model.*;
import no.snabel.model.Invoice;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * PEPPOL Access Point Service using Oxalis API
 *
 * This service handles sending EHF invoices via the PEPPOL network using Oxalis.
 *
 * Prerequisites:
 * - Oxalis home directory configured (/opt/oxalis-home)
 * - PEPPOL certificates imported to keystore
 * - oxalis.conf properly configured
 * - PEPPOL participant ID registered
 *
 * @see <a href="https://github.com/OxalisCommunity/oxalis">Oxalis Documentation</a>
 */
@ApplicationScoped
public class PeppolAccessPointService {

    private static final Logger LOG = Logger.getLogger(PeppolAccessPointService.class);

    @Inject
    EHFInvoiceService ehfService;

    @ConfigProperty(name = "oxalis.enabled", defaultValue = "false")
    boolean oxalisEnabled;

    @ConfigProperty(name = "oxalis.home", defaultValue = "/opt/oxalis-home")
    String oxalisHome;

    @ConfigProperty(name = "oxalis.participant.identifier")
    String defaultParticipantId;

    // Oxalis component - initialized once
    private OxalisOutboundComponent oxalisComponent;

    @PostConstruct
    void init() {
        if (oxalisEnabled) {
            try {
                // Set Oxalis home system property
                System.setProperty("oxalis.home", oxalisHome);

                // Initialize Oxalis component
                // This handles all internal dependency injection via Guice
                oxalisComponent = new OxalisOutboundComponent();
                LOG.infof("Oxalis PEPPOL Access Point initialized (home: %s)", oxalisHome);
            } catch (Exception e) {
                LOG.errorf(e, "Failed to initialize Oxalis. PEPPOL sending will not be available.");
                oxalisEnabled = false;
            }
        } else {
            LOG.info("Oxalis PEPPOL Access Point is disabled. Set oxalis.enabled=true to enable.");
        }
    }

    /**
     * Send invoice via PEPPOL network using Oxalis API
     *
     * @param invoice Invoice to send
     * @return Result of PEPPOL transmission
     */
    public Uni<PeppolSendResult> sendInvoice(Invoice invoice) {
        if (!oxalisEnabled || oxalisComponent == null) {
            return Uni.createFrom().failure(
                new UnsupportedOperationException(
                    "PEPPOL sending is not enabled. Configure Oxalis and set oxalis.enabled=true"
                )
            );
        }

        return Uni.createFrom().item(() -> {
            try {
                LOG.infof("Sending invoice %s to PEPPOL endpoint %s",
                    invoice.invoiceNumber, invoice.clientEndpointId);

                // 1. Generate EHF XML
                String ehfXml = ehfService.generateEHF(invoice);
                InputStream xmlStream = new ByteArrayInputStream(ehfXml.getBytes(StandardCharsets.UTF_8));

                // 2. Get TransmissionRequestBuilder from Oxalis component
                TransmissionRequestBuilder requestBuilder = oxalisComponent.getTransmissionRequestBuilder();

                // 3. Build transmission request with payload and metadata
                ParticipantIdentifier sender = parseParticipantId(
                    invoice.customer.organizationNumber,
                    invoice.customer.endpointScheme
                );

                ParticipantIdentifier receiver = parseParticipantId(
                    invoice.clientOrganizationNumber != null
                        ? invoice.clientOrganizationNumber
                        : invoice.clientEndpointId,
                    invoice.clientEndpointScheme
                );

                TransmissionRequest request = requestBuilder
                    .payLoad(xmlStream)
                    .sender(sender)
                    .receiver(receiver)
                    .documentType(getEHFDocumentType())
                    .processType(getEHFProcessType())
                    .build();

                // 4. Get Transmitter from Oxalis component
                Transmitter transmitter = oxalisComponent.getTransmitter();

                // 5. Transmit the message
                TransmissionResponse response = transmitter.transmit(request);

                // 6. Map response to result
                PeppolSendResult result = new PeppolSendResult();
                result.success = true;
                result.messageId = response.getHeader().getInstanceId().getValue();
                result.transmissionId = response.getTransmissionIdentifier() != null
                    ? response.getTransmissionIdentifier().toString()
                    : null;
                result.recipientAccessPoint = response.getEndpoint() != null
                    ? response.getEndpoint().getAddress().toString()
                    : null;
                result.protocol = response.getProtocol() != null
                    ? response.getProtocol().toString()
                    : "AS4";
                result.timestamp = LocalDateTime.now();

                LOG.infof("Successfully sent invoice %s via PEPPOL. Message ID: %s",
                    invoice.invoiceNumber, result.messageId);

                return result;

            } catch (OxalisTransmissionException e) {
                LOG.errorf(e, "PEPPOL transmission failed for invoice %s", invoice.invoiceNumber);
                throw new RuntimeException("PEPPOL transmission failed: " + e.getMessage(), e);
            } catch (Exception e) {
                LOG.errorf(e, "Unexpected error sending invoice %s via PEPPOL", invoice.invoiceNumber);
                throw new RuntimeException("Failed to send via PEPPOL: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Parse organization number to PEPPOL ParticipantId
     *
     * @param identifier Organization number or PEPPOL ID
     * @param scheme PEPPOL scheme (default: 0192 for Norwegian org numbers)
     * @return ParticipantIdentifier
     */
    private ParticipantIdentifier parseParticipantId(String identifier, String scheme) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Participant identifier cannot be empty");
        }

        // If identifier already contains scheme (e.g., "0192:123456789"), parse it
        if (identifier.contains(":")) {
            String[] parts = identifier.split(":", 2);
            return ParticipantIdentifier.of(parts[1], Scheme.of(parts[0]));
        }

        // Otherwise, use provided scheme or default to 0192 (Norwegian org numbers)
        String effectiveScheme = scheme != null && !scheme.isEmpty() ? scheme : "0192";
        return ParticipantIdentifier.of(identifier, Scheme.of(effectiveScheme));
    }

    /**
     * Get EHF 3.0 document type identifier
     * This corresponds to UBL 2.1 Invoice with PEPPOL BIS Billing 3.0 customization
     */
    private DocumentTypeIdentifier getEHFDocumentType() {
        return DocumentTypeIdentifier.of(
            "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##" +
            "urn:cen.eu:en16931:2017#compliant#" +
            "urn:fdc:peppol.eu:2017:poacc:billing:3.0::2.1",
            Scheme.of("busdox-docid-qns")
        );
    }

    /**
     * Get EHF 3.0 process type identifier
     * This is the PEPPOL BIS Billing 3.0 process
     */
    private ProcessIdentifier getEHFProcessType() {
        return ProcessIdentifier.of(
            "urn:fdc:peppol.eu:2017:poacc:billing:01:1.0",
            Scheme.of("cenbii-procid-ubl")
        );
    }

    /**
     * Check if Oxalis is properly configured and ready
     */
    public boolean isReady() {
        return oxalisEnabled && oxalisComponent != null;
    }

    /**
     * Result object for PEPPOL transmission
     */
    public static class PeppolSendResult {
        public boolean success;
        public String messageId;
        public String transmissionId;
        public String recipientAccessPoint;
        public String protocol;
        public LocalDateTime timestamp;
        public String errorMessage;

        public String toJson() {
            return String.format(
                "{\"success\":%b,\"messageId\":\"%s\",\"transmissionId\":\"%s\"," +
                "\"recipientAccessPoint\":\"%s\",\"protocol\":\"%s\",\"timestamp\":\"%s\"}",
                success, messageId, transmissionId, recipientAccessPoint, protocol, timestamp
            );
        }
    }
}
