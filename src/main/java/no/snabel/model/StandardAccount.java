package no.snabel.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "standard_accounts")
public class StandardAccount extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 10)
    public String accountNumber;

    @Column(name = "account_name", nullable = false)
    public String accountName;

    @Column(name = "account_type", nullable = false, length = 50)
    public String accountType;

    @Column(name = "account_class", length = 50)
    public String accountClass;

    @Column(name = "vat_code", length = 10)
    public String vatCode;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "is_system")
    public Boolean isSystem = true;

    public Boolean active = true;
}
