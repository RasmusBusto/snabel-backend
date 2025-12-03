package no.snabel.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;

    @Column(name = "invoice_number", unique = true, nullable = false, length = 50)
    public String invoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    public LocalDate invoiceDate;

    @Column(name = "due_date", nullable = false)
    public LocalDate dueDate;

    @Column(name = "client_name", nullable = false)
    public String clientName;

    @Column(name = "client_organization_number", length = 9)
    public String clientOrganizationNumber;

    @Column(name = "client_address", length = 500)
    public String clientAddress;

    @Column(name = "client_postal_code", length = 10)
    public String clientPostalCode;

    @Column(name = "client_city", length = 100)
    public String clientCity;

    @Column(precision = 19, scale = 2)
    public BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "vat_amount", precision = 19, scale = 2)
    public BigDecimal vatAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 19, scale = 2)
    public BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 3)
    public String currency = "NOK";

    @Column(length = 50)
    public String status = "DRAFT"; // DRAFT, SENT, PAID, OVERDUE, CANCELLED

    @Column(name = "payment_terms")
    public String paymentTerms;

    @Column(name = "payment_reference", length = 50)
    public String paymentReference;

    @Column(name = "bank_account", length = 20)
    public String bankAccount;

    @Column(name = "buyer_reference", length = 100)
    public String buyerReference;

    @Column(name = "contract_reference", length = 100)
    public String contractReference;

    @Column(columnDefinition = "TEXT")
    public String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id")
    public JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    public User createdBy;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<InvoiceLine> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "sent_at")
    public LocalDateTime sentAt;

    @Column(name = "paid_at")
    public LocalDateTime paidAt;
}
