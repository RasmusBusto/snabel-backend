package no.snabel.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_lines")
public class InvoiceLine extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    public Invoice invoice;

    @Column(name = "line_number", nullable = false)
    public Integer lineNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    public String description;

    @Column(name = "item_name")
    public String itemName;

    @Column(name = "item_id", length = 50)
    public String itemId;

    @Column(name = "unit_code", length = 10)
    public String unitCode = "EA";

    @Column(precision = 10, scale = 2)
    public BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_price", precision = 19, scale = 2, nullable = false)
    public BigDecimal unitPrice;

    @Column(name = "vat_rate", precision = 5, scale = 2)
    public BigDecimal vatRate = BigDecimal.ZERO;

    @Column(name = "vat_amount", precision = 19, scale = 2)
    public BigDecimal vatAmount = BigDecimal.ZERO;

    @Column(name = "line_total", precision = 19, scale = 2, nullable = false)
    public BigDecimal lineTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    public Account account;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
}
