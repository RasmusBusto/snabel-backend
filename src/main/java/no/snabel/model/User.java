package no.snabel.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;

    @Column(unique = true, nullable = false, length = 100)
    public String username;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(nullable = false)
    public String email;

    @Column(name = "full_name")
    public String fullName;

    @Column(nullable = false, length = 50)
    public String role = "USER";

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_login")
    public LocalDateTime lastLogin;

    public Boolean active = true;

    public static Uni<User> findByUsername(String username) {
        return find("username", username).firstResult();
    }
}
