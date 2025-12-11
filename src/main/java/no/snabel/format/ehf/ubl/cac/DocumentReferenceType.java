package no.snabel.format.ehf.ubl.cac;

import no.snabel.format.ehf.ubl.types.IdentifierType;
import no.snabel.format.ehf.ubl.types.DateType;

/**
 * CAC DocumentReferenceType - Generic document reference.
 */
public class DocumentReferenceType {
    private IdentifierType id;
    private DateType issueDate;
    private IdentifierType documentTypeCode;

    public DocumentReferenceType() {
    }

    public DocumentReferenceType(String id) {
        this.id = new IdentifierType(id);
    }

    public IdentifierType getId() {
        return id;
    }

    public void setId(IdentifierType id) {
        this.id = id;
    }

    public DateType getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(DateType issueDate) {
        this.issueDate = issueDate;
    }

    public IdentifierType getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(IdentifierType documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }
}
