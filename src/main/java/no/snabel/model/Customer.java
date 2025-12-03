package no.snabel.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "organization_number", unique = true, nullable = false, length = 9)
    public String organizationNumber;

    @Column(name = "company_name", nullable = false)
    public String companyName;

    @Column(name = "contact_person")
    public String contactPerson;

    public String email;

    public String phone;

    public String address;

    @Column(name = "postal_code")
    public String postalCode;

    public String city;

    public String country = "Norge";

    @Column(name = "bank_account", length = 20)
    public String bankAccount;

    @Column(name = "bank_name")
    public String bankName;

    @Column(length = 34)
    public String iban;

    @Column(name = "swift_bic", length = 11)
    public String swiftBic;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now();

    public Boolean active = true;
}
