package no.snabel.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_entries", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"customer_id", "entry_number"})
})
public class JournalEntry extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;

    @Column(name = "entry_number", length = 50)
    public String entryNumber;

    @Column(name = "entry_date", nullable = false)
    public LocalDate entryDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String description;

    @Column(length = 100)
    public String reference;

    @Column(name = "entry_type", length = 50)
    public String entryType = "MANUAL"; // MANUAL, INVOICE, PAYMENT, AUTOMATED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    public User createdBy;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now();

    public Boolean posted = false;

    @Column(name = "posted_at")
    public LocalDateTime postedAt;

    public Boolean reversed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversed_by")
    public JournalEntry reversedBy;
}
