package no.snabel.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"customer_id", "account_number"})
})
public class Account extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_account_id")
    public StandardAccount standardAccount;

    @Column(name = "account_number", nullable = false, length = 10)
    public String accountNumber;

    @Column(name = "account_name", nullable = false)
    public String accountName;

    @Column(name = "account_type", nullable = false, length = 50)
    public String accountType; // ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE

    @Column(name = "vat_code", length = 10)
    public String vatCode;

    @Column(precision = 19, scale = 2)
    public BigDecimal balance = BigDecimal.ZERO;

    @Column(length = 3)
    public String currency = "NOK";

    @Column(columnDefinition = "TEXT")
    public String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id")
    public Account parentAccount;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now();

    public Boolean active = true;

    public static Uni<Account> findByCustomerAndNumber(Long customerId, String accountNumber) {
        return find("customer.id = ?1 and accountNumber = ?2", customerId, accountNumber).firstResult();
    }
}
