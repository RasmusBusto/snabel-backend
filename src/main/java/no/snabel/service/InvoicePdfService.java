package no.snabel.service;

import jakarta.enterprise.context.ApplicationScoped;
import no.snabel.model.Customer;
import no.snabel.model.Invoice;
import no.snabel.model.InvoiceLine;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating PDF invoices
 */
@ApplicationScoped
public class InvoicePdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 20;
    private static final float FONT_SIZE_HEADING = 12;
    private static final float FONT_SIZE_NORMAL = 10;
    private static final float FONT_SIZE_SMALL = 8;
    private static final float LINE_HEIGHT = 14;

    /**
     * Generate PDF for an invoice
     */
    public byte[] generatePdf(Invoice invoice) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;

                // Supplier (seller) information - top left
                yPosition = addSupplierInfo(contentStream, invoice.customer, yPosition);

                // Invoice title and number - top right
                addInvoiceHeader(contentStream, invoice, page.getMediaBox().getWidth());

                // Customer (buyer) information
                yPosition -= 40;
                yPosition = addCustomerInfo(contentStream, invoice, yPosition);

                // Invoice details (dates, references)
                yPosition -= 20;
                yPosition = addInvoiceDetails(contentStream, invoice, yPosition);

                // Invoice lines table
                yPosition -= 30;
                yPosition = addInvoiceLinesTable(contentStream, invoice, yPosition, page.getMediaBox().getWidth());

                // Totals
                yPosition = addTotals(contentStream, invoice, yPosition, page.getMediaBox().getWidth());

                // Payment information
                yPosition -= 20;
                addPaymentInfo(contentStream, invoice, yPosition);

                // Footer
                addFooter(contentStream, invoice, page.getMediaBox().getHeight());
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private float addSupplierInfo(PDPageContentStream contentStream, Customer supplier, float yPosition) throws Exception {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_HEADING);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(supplier.companyName);
        contentStream.endText();

        yPosition -= LINE_HEIGHT;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        if (supplier.address != null) {
            contentStream.showText(supplier.address);
        }
        contentStream.endText();

        yPosition -= LINE_HEIGHT;

        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        if (supplier.postalCode != null && supplier.city != null) {
            contentStream.showText(supplier.postalCode + " " + supplier.city);
        }
        contentStream.endText();

        yPosition -= LINE_HEIGHT;

        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Org.nr: " + supplier.organizationNumber);
        contentStream.endText();

        if (supplier.email != null || supplier.phone != null) {
            yPosition -= LINE_HEIGHT;
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            String contact = "";
            if (supplier.phone != null) contact += "Tlf: " + supplier.phone;
            if (supplier.email != null) {
                if (!contact.isEmpty()) contact += "  ";
                contact += "E-post: " + supplier.email;
            }
            contentStream.showText(contact);
            contentStream.endText();
        }

        return yPosition - LINE_HEIGHT;
    }

    private void addInvoiceHeader(PDPageContentStream contentStream, Invoice invoice, float pageWidth) throws Exception {
        float rightX = pageWidth - MARGIN;
        float yPosition = pageWidth - MARGIN * 2;

        // Title
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_TITLE);
        String title = "FAKTURA";
        float titleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(title) / 1000 * FONT_SIZE_TITLE;
        contentStream.newLineAtOffset(rightX - titleWidth, yPosition);
        contentStream.showText(title);
        contentStream.endText();

        yPosition -= LINE_HEIGHT * 2;

        // Invoice number
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
        String invoiceNumber = "Fakturanr: " + invoice.invoiceNumber;
        float numberWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(invoiceNumber) / 1000 * FONT_SIZE_NORMAL;
        contentStream.newLineAtOffset(rightX - numberWidth, yPosition);
        contentStream.showText(invoiceNumber);
        contentStream.endText();
    }

    private float addCustomerInfo(PDPageContentStream contentStream, Invoice invoice, float yPosition) throws Exception {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_HEADING);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Kunde");
        contentStream.endText();

        yPosition -= LINE_HEIGHT * 1.5f;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(invoice.clientName);
        contentStream.endText();

        yPosition -= LINE_HEIGHT;

        if (invoice.clientAddress != null) {
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(invoice.clientAddress);
            contentStream.endText();
            yPosition -= LINE_HEIGHT;
        }

        if (invoice.clientPostalCode != null && invoice.clientCity != null) {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(invoice.clientPostalCode + " " + invoice.clientCity);
            contentStream.endText();
            yPosition -= LINE_HEIGHT;
        }

        if (invoice.clientOrganizationNumber != null && !invoice.clientOrganizationNumber.isEmpty()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Org.nr: " + invoice.clientOrganizationNumber);
            contentStream.endText();
            yPosition -= LINE_HEIGHT;
        }

        return yPosition;
    }

    private float addInvoiceDetails(PDPageContentStream contentStream, Invoice invoice, float yPosition) throws Exception {
        float col1X = MARGIN;
        float col2X = MARGIN + 150;

        // Invoice date
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(col1X, yPosition);
        contentStream.showText("Fakturadato:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(col2X, yPosition);
        contentStream.showText(invoice.invoiceDate.format(DATE_FORMATTER));
        contentStream.endText();

        yPosition -= LINE_HEIGHT;

        // Due date
        contentStream.beginText();
        contentStream.newLineAtOffset(col1X, yPosition);
        contentStream.showText("Forfallsdato:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(col2X, yPosition);
        contentStream.showText(invoice.dueDate.format(DATE_FORMATTER));
        contentStream.endText();

        yPosition -= LINE_HEIGHT;

        // Payment terms
        if (invoice.paymentTerms != null && !invoice.paymentTerms.isEmpty()) {
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(col1X, yPosition);
            contentStream.showText("Betalingsbetingelser:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(col2X, yPosition);
            contentStream.showText(invoice.paymentTerms);
            contentStream.endText();

            yPosition -= LINE_HEIGHT;
        }

        // Buyer reference
        if (invoice.buyerReference != null && !invoice.buyerReference.isEmpty()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(col1X, yPosition);
            contentStream.showText("Deres referanse:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(col2X, yPosition);
            contentStream.showText(invoice.buyerReference);
            contentStream.endText();

            yPosition -= LINE_HEIGHT;
        }

        return yPosition;
    }

    private float addInvoiceLinesTable(PDPageContentStream contentStream, Invoice invoice, float yPosition, float pageWidth) throws Exception {
        float tableWidth = pageWidth - 2 * MARGIN;
        float col1Width = 40; // Line number
        float col2Width = tableWidth - col1Width - 80 - 80 - 80 - 100; // Description (flexible)
        float col3Width = 80; // Quantity
        float col4Width = 80; // Unit price
        float col5Width = 80; // VAT %
        float col6Width = 100; // Amount

        float col1X = MARGIN;
        float col2X = col1X + col1Width;
        float col3X = col2X + col2Width;
        float col4X = col3X + col3Width;
        float col5X = col4X + col4Width;
        float col6X = col5X + col5Width;

        // Table header
        contentStream.setLineWidth(1);
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(pageWidth - MARGIN, yPosition);
        contentStream.stroke();

        yPosition -= LINE_HEIGHT * 1.2f;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(col1X, yPosition);
        contentStream.showText("#");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(col2X, yPosition);
        contentStream.showText("Beskrivelse");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(col3X, yPosition);
        contentStream.showText("Antall");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(col4X, yPosition);
        contentStream.showText("Pris");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(col5X, yPosition);
        contentStream.showText("MVA %");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(col6X, yPosition);
        contentStream.showText("Beløp");
        contentStream.endText();

        yPosition -= LINE_HEIGHT * 0.5f;

        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(pageWidth - MARGIN, yPosition);
        contentStream.stroke();

        yPosition -= LINE_HEIGHT;

        // Table rows
        for (InvoiceLine line : invoice.lines) {
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(col1X, yPosition);
            contentStream.showText(String.valueOf(line.lineNumber));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(col2X, yPosition);
            String description = line.description;
            if (description.length() > 50) {
                description = description.substring(0, 47) + "...";
            }
            contentStream.showText(description);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(col3X, yPosition);
            contentStream.showText(formatNumber(line.quantity));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(col4X, yPosition);
            contentStream.showText(formatCurrency(line.unitPrice));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(col5X, yPosition);
            contentStream.showText(formatNumber(line.vatRate) + "%");
            contentStream.endText();

            BigDecimal lineTotal = line.unitPrice.multiply(line.quantity);
            contentStream.beginText();
            contentStream.newLineAtOffset(col6X, yPosition);
            contentStream.showText(formatCurrency(lineTotal));
            contentStream.endText();

            yPosition -= LINE_HEIGHT;
        }

        yPosition -= LINE_HEIGHT * 0.5f;

        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(pageWidth - MARGIN, yPosition);
        contentStream.stroke();

        return yPosition - LINE_HEIGHT;
    }

    private float addTotals(PDPageContentStream contentStream, Invoice invoice, float yPosition, float pageWidth) throws Exception {
        float labelX = pageWidth - MARGIN - 200;
        float amountX = pageWidth - MARGIN - 100;

        // Subtotal
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
        contentStream.newLineAtOffset(labelX, yPosition);
        contentStream.showText("Subtotal:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(amountX, yPosition);
        contentStream.showText(formatCurrency(invoice.subtotal));
        contentStream.endText();

        yPosition -= LINE_HEIGHT;

        // VAT
        contentStream.beginText();
        contentStream.newLineAtOffset(labelX, yPosition);
        contentStream.showText("MVA:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(amountX, yPosition);
        contentStream.showText(formatCurrency(invoice.vatAmount));
        contentStream.endText();

        yPosition -= LINE_HEIGHT * 1.5f;

        // Total
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_HEADING);
        contentStream.newLineAtOffset(labelX, yPosition);
        contentStream.showText("TOTALT:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(amountX, yPosition);
        contentStream.showText(formatCurrency(invoice.totalAmount) + " " + invoice.currency);
        contentStream.endText();

        return yPosition - LINE_HEIGHT;
    }

    private void addPaymentInfo(PDPageContentStream contentStream, Invoice invoice, float yPosition) throws Exception {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_HEADING);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Betalingsinformasjon");
        contentStream.endText();

        yPosition -= LINE_HEIGHT * 1.5f;

        String bankAccount = invoice.bankAccount != null ? invoice.bankAccount :
                           (invoice.customer.bankAccount != null ? invoice.customer.bankAccount : null);

        if (bankAccount != null) {
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Kontonummer: " + bankAccount);
            contentStream.endText();
            yPosition -= LINE_HEIGHT;
        }

        if (invoice.paymentReference != null && !invoice.paymentReference.isEmpty()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("KID/Referanse: " + invoice.paymentReference);
            contentStream.endText();
            yPosition -= LINE_HEIGHT;
        }

        if (invoice.notes != null && !invoice.notes.isEmpty()) {
            yPosition -= LINE_HEIGHT;
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_SMALL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Merknad: " + invoice.notes);
            contentStream.endText();
        }
    }

    private void addFooter(PDPageContentStream contentStream, Invoice invoice, float pageHeight) throws Exception {
        float footerY = MARGIN / 2;

        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_SMALL);
        contentStream.newLineAtOffset(MARGIN, footerY);
        contentStream.showText("Generert med Snabel Regnskap");
        contentStream.endText();
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.2f", amount).replace(",", " ");
    }

    private String formatNumber(BigDecimal number) {
        if (number.stripTrailingZeros().scale() <= 0) {
            return number.toBigInteger().toString();
        }
        return number.stripTrailingZeros().toPlainString();
    }
}
