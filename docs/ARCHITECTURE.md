# Snabel Accounting System - Architecture Documentation

## System Overview

The Snabel Accounting System is a modern, fully asynchronous Norwegian accounting backend built with Quarkus. The system follows reactive programming principles throughout, ensuring high performance and scalability.

## Architecture Principles

### 1. Reactive Everything
- **Async I/O**: All database operations use Hibernate Reactive with Mutiny
- **Non-blocking**: REST endpoints return `Uni<Response>` for async processing
- **Backpressure**: Reactive streams handle load gracefully
- **Performance**: No thread blocking, efficient resource utilization

### 2. Multi-Tenancy
- **Customer Isolation**: All data is scoped to a customer
- **JWT-based**: Customer ID extracted from JWT token claims
- **Automatic Filtering**: All queries filter by customer ID
- **Security**: No cross-customer data access possible

### 3. Norwegian Compliance
- **NS 4102**: Norwegian standard chart of accounts
- **VAT Codes**: Norwegian VAT system (0%, 15%, 25%)
- **Organization Numbers**: 9-digit Norwegian business IDs
- **Localization**: Norwegian account names and terminology

## Technology Stack

```
┌─────────────────────────────────────────┐
│         REST API Layer                   │
│    (RESTEasy Reactive + Jackson)        │
├─────────────────────────────────────────┤
│      Security Layer                      │
│    (SmallRye JWT + BCrypt)              │
├─────────────────────────────────────────┤
│      Business Logic Layer               │
│         (Service Classes)                │
├─────────────────────────────────────────┤
│       Data Access Layer                 │
│  (Hibernate Reactive Panache)           │
├─────────────────────────────────────────┤
│      Reactive Driver                    │
│     (reactive-pg-client)                │
├─────────────────────────────────────────┤
│         PostgreSQL 16                   │
└─────────────────────────────────────────┘
```

### Core Technologies

- **Quarkus 3.29.2**: Supersonic Subatomic Java Framework
- **Java 21**: Latest LTS version with modern language features
- **Mutiny**: Reactive programming library for async operations
- **Hibernate Reactive**: Async ORM with Panache repositories
- **PostgreSQL 16**: Production-grade relational database
- **Flyway**: Version-controlled database migrations
- **SmallRye JWT**: JWT authentication and authorization
- **BCrypt**: Password hashing

## Project Structure

```
accounting-backend/
├── src/main/java/no/snabel/
│   ├── model/              # Domain entities
│   │   ├── Customer.java
│   │   ├── User.java
│   │   ├── Account.java
│   │   ├── StandardAccount.java
│   │   ├── Invoice.java
│   │   ├── InvoiceLine.java
│   │   ├── JournalEntry.java
│   │   └── JournalEntryLine.java
│   │
│   ├── resource/           # REST endpoints
│   │   ├── SecureResource.java     # Base class for secured endpoints
│   │   ├── AuthResource.java       # Login endpoint
│   │   ├── AccountResource.java    # Account CRUD
│   │   └── InvoiceResource.java    # Invoice management
│   │
│   ├── service/            # Business logic
│   │   └── AuthService.java        # Authentication logic
│   │
│   ├── security/           # Security components
│   │   └── TokenService.java       # JWT token generation
│   │
│   ├── dto/                # Data transfer objects
│   │   ├── LoginRequest.java
│   │   └── LoginResponse.java
│   │
│   └── format/             # External format support
│       └── ehf/            # EHF/PEPPOL format support
│           └── ubl/        # UBL 2.1 data model
│               ├── InvoiceType.java      # Main invoice structure
│               ├── types/                # Base types (Amount, Code, etc.)
│               ├── cac/                  # Common aggregate components
│               └── writer/               # XML serialization
│                   └── UBLWriter.java
│
├── src/main/resources/
│   ├── db/migration/       # Flyway migrations
│   │   ├── V1__initial_schema.sql
│   │   └── V2__seed_norwegian_accounts.sql
│   │
│   ├── META-INF/resources/
│   │   └── publicKey.pem   # RSA public key for JWT verification
│   │
│   ├── privateKey.pem      # RSA private key for JWT signing
│   └── application.properties
│
└── src/test/
    ├── java/no/snabel/
    │   └── resource/       # API tests
    └── resources/
        ├── application.properties
        └── test-data.sql   # Test data
```

## Component Details

### 1. Entity Layer (Model)

All entities extend `PanacheEntityBase` for reactive Hibernate support.

**Key Entities:**
- `Customer`: Multi-tenant root entity
- `User`: Authentication and authorization
- `Account`: Customer-specific chart of accounts
- `StandardAccount`: Norwegian standard accounts (NS 4102)
- `Invoice`: Sales invoices with Norwegian VAT
- `JournalEntry`: Accounting transactions

**Entity Features:**
- Automatic timestamps (created_at, updated_at)
- Soft delete support (active flag)
- Bidirectional relationships
- Custom finder methods using Mutiny

**Example:**
```java
@Entity
@Table(name = "accounts")
public class Account extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    public Customer customer;

    public String accountNumber;
    public String accountName;

    public static Uni<Account> findByCustomerAndNumber(Long customerId, String accountNumber) {
        return find("customer.id = ?1 and accountNumber = ?2", customerId, accountNumber).firstResult();
    }
}
```

### 2. Resource Layer (REST API)

REST endpoints use JAX-RS annotations with reactive types.

**Base Class Pattern:**
```java
@Authenticated
public abstract class SecureResource {
    @Inject
    JsonWebToken jwt;

    protected Long getCustomerId() {
        return jwt.getClaim("customerId");
    }

    protected Long getUserId() {
        return jwt.getClaim("userId");
    }

    protected String getRole() {
        return jwt.getClaim("role");
    }
}
```

**Endpoint Pattern:**
```java
@Path("/api/accounts")
@Authenticated
public class AccountResource extends SecureResource {

    @GET
    @RolesAllowed({"USER", "ADMIN", "ACCOUNTANT"})
    public Uni<List<Account>> listAccounts() {
        Long customerId = getCustomerId();
        return Account.find("customer.id = ?1 and active = true", customerId).list();
    }

    @POST
    @RolesAllowed({"ADMIN", "ACCOUNTANT"})
    public Uni<Response> createAccount(Account account) {
        Long customerId = getCustomerId();
        return Customer.<Customer>findById(customerId)
            .chain(customer -> {
                account.customer = customer;
                return account.persistAndFlush();
            })
            .map(a -> Response.status(Response.Status.CREATED).entity(a).build());
    }
}
```

### 3. Security Layer

**JWT Token Flow:**
1. User sends username/password to `/api/auth/login`
2. System verifies credentials with BCrypt
3. System generates JWT with RSA signature
4. JWT contains: userId, username, customerId, role, deviceType
5. Client includes JWT in Authorization header for all requests
6. Quarkus validates JWT signature and extracts claims
7. `@RolesAllowed` enforces role-based access control

**Token Structure:**
```json
{
  "iss": "https://snabel.no",
  "upn": "testuser",
  "userId": 1,
  "customerId": 1,
  "role": "ADMIN",
  "deviceType": "web",
  "groups": ["ADMIN"],
  "exp": 1699545600
}
```

**Token Expiration:**
- Web tokens: 24 hours (86400 seconds)
- App tokens: 30 days (2592000 seconds)

**Key Generation:**
```bash
# Generate RSA key pair (2048-bit)
openssl genrsa -out privateKey.pem 2048
openssl rsa -in privateKey.pem -pubout -out publicKey.pem
```

### 4. Service Layer

Business logic is encapsulated in service classes.

**Example: AuthService**
```java
@ApplicationScoped
public class AuthService {

    @Inject
    TokenService tokenService;

    public Uni<LoginResponse> login(LoginRequest request) {
        return User.findByUsername(request.username)
            .chain(user -> {
                if (user == null || !BCrypt.checkpw(request.password, user.passwordHash)) {
                    return Uni.createFrom().failure(new UnauthorizedException());
                }

                String token = tokenService.generateToken(
                    user.id, user.username, user.customer.id,
                    user.role, request.deviceType
                );

                user.lastLogin = LocalDateTime.now();
                return user.persistAndFlush()
                    .map(u -> new LoginResponse(token, u.id, u.username,
                                                u.customer.id, u.role,
                                                tokenService.getExpirationTime(request.deviceType)));
            });
    }
}
```

### 5. Database Layer

**Migration Strategy:**
- Flyway manages schema versions
- V1__initial_schema.sql: Creates all tables
- V2__seed_norwegian_accounts.sql: Seeds standard accounts
- Migrations run automatically at startup

**Connection Pooling:**
- Reactive driver uses async connection pooling
- Configured in application.properties
- No blocking connections

**Query Patterns:**
```java
// Find with filtering
Account.find("customer.id = ?1 and active = true", customerId).list()

// Find single result
Account.find("id = ?1 and customer.id = ?2", id, customerId).firstResult()

// Persist with flush
account.persistAndFlush()

// Using Mutiny chain
Customer.<Customer>findById(customerId)
    .chain(customer -> {
        account.customer = customer;
        return account.persistAndFlush();
    })
```

## Data Flow Examples

### 1. Login Flow

```
User → POST /api/auth/login
       ↓
   AuthResource
       ↓
   AuthService.login()
       ↓
   User.findByUsername() → Database (async)
       ↓
   BCrypt.checkpw() (password verification)
       ↓
   TokenService.generateToken() (JWT with RSA)
       ↓
   User.persistAndFlush() (update last_login)
       ↓
   LoginResponse → User
```

### 2. Create Account Flow

```
User → POST /api/accounts + JWT
       ↓
   Quarkus validates JWT signature
       ↓
   @RolesAllowed checks user role
       ↓
   AccountResource.createAccount()
       ↓
   Extract customerId from JWT
       ↓
   Customer.findById(customerId) → Database (async)
       ↓
   Set account.customer reference
       ↓
   account.persistAndFlush() → Database (async)
       ↓
   Response 201 Created → User
```

### 3. List Invoices Flow

```
User → GET /api/invoices?status=DRAFT + JWT
       ↓
   Quarkus validates JWT
       ↓
   @RolesAllowed checks role
       ↓
   InvoiceResource.listInvoices()
       ↓
   Extract customerId from JWT
       ↓
   Invoice.find("customer.id = ?1 and status = ?2", customerId, "DRAFT")
       ↓
   Database query (async) → List<Invoice>
       ↓
   Response 200 OK with JSON → User
```

## Reactive Programming with Mutiny

### Uni (Single Result)

Represents an async operation that produces 0 or 1 result.

```java
// Simple mapping
Uni<Account> account = Account.<Account>findById(1L);
Uni<String> accountName = account.map(a -> a.accountName);

// Chaining async operations
Uni<Response> response = Customer.<Customer>findById(customerId)
    .chain(customer -> {
        account.customer = customer;
        return account.persistAndFlush();
    })
    .map(a -> Response.ok(a).build());

// Error handling
Uni<Response> response = Account.<Account>findById(id)
    .onFailure().recoverWithItem(Response.status(500).build());
```

### Multi (Stream)

Represents an async stream that produces 0 to N results.

```java
// List results
Uni<List<Account>> accounts = Account.find("customer.id = ?1", customerId).list();

// Stream processing
Multi<Account> accountStream = Account.stream("customer.id = ?1", customerId);
```

### Best Practices

1. **Always use async types**: Return `Uni<T>` from all methods
2. **Use `.chain()` for async operations**: Not `.map()` when result is async
3. **Explicit types**: Use `Account.<Account>find()` for type inference
4. **Avoid blocking**: Never call `.await()` in reactive code
5. **Handle errors**: Use `.onFailure()` for error handling

## Security Considerations

### 1. Authentication
- Passwords hashed with BCrypt (work factor 10)
- JWT signed with RSA 2048-bit keys
- Private key never exposed via API
- Token expiration enforced

### 2. Authorization
- Role-based access control (RBAC)
- `@RolesAllowed` annotation on endpoints
- Three roles: USER (read), ACCOUNTANT (write), ADMIN (full access)

### 3. Multi-Tenancy
- Customer ID embedded in JWT
- All queries filter by customer ID
- No possibility of cross-customer data access
- Automatic filtering in base resource class

### 4. SQL Injection Prevention
- Parameterized queries only
- Hibernate handles escaping
- No raw SQL concatenation

### 5. Input Validation
- Bean validation annotations on DTOs
- Type safety with Java
- Jackson deserialization with type checking

### 6. CORS
- Configured for development (origins=*)
- **Production**: Restrict to specific origins

## Performance Optimization

### 1. Async I/O
- No thread blocking on database calls
- Efficient resource utilization
- High concurrency support

### 2. Connection Pooling
- Reactive PostgreSQL driver pools connections
- Async connection management
- No connection exhaustion

### 3. Indexing
- All foreign keys indexed
- Unique constraints on business keys
- Query performance optimized

### 4. Lazy Loading
- Relationships loaded on demand
- Use JOIN FETCH for eager loading when needed

### 5. Caching
- **Future enhancement**: Add Redis for session caching
- **Future enhancement**: Query result caching

## Testing Strategy

### Unit Tests
- Test individual methods in isolation
- Mock dependencies
- Fast execution

### Integration Tests
- Test REST endpoints end-to-end
- Use real PostgreSQL database
- `@QuarkusTest` annotation
- Test data loaded from test-data.sql

### Security Tests
- `@TestSecurity` for mock JWT
- `@JwtSecurity` for JWT claims
- Test role-based access control
- Test authentication failures

**Example Test:**
```java
@QuarkusTest
public class AccountResourceTest {

    @Test
    @TestSecurity(user = "testuser", roles = {"ADMIN"})
    @JwtSecurity(claims = {
        @Claim(key = "customerId", value = "1"),
        @Claim(key = "userId", value = "1")
    })
    public void testCreateAccount() {
        given()
            .contentType("application/json")
            .body("{\"accountNumber\":\"2000\",\"accountName\":\"Test Account\"}")
        .when()
            .post("/api/accounts")
        .then()
            .statusCode(201)
            .body("accountNumber", equalTo("2000"));
    }
}
```

## Deployment

### Development
```bash
./mvnw quarkus:dev
```
- Hot reload enabled
- Dev UI available at http://localhost:8080/q/dev
- H2 console disabled (using PostgreSQL)

### Production
```bash
# Build JAR
./mvnw package

# Run
java -jar target/quarkus-app/quarkus-run.jar
```

### Docker
```bash
# Build native image
./mvnw package -Pnative -Dquarkus.native.container-build=true

# Build Docker image
docker build -f src/main/docker/Dockerfile.native -t snabel-accounting .

# Run
docker run -p 8080:8080 snabel-accounting
```

### Environment Variables
```bash
# Database
QUARKUS_DATASOURCE_USERNAME=snabel
QUARKUS_DATASOURCE_PASSWORD=secure_password
QUARKUS_DATASOURCE_REACTIVE_URL=postgresql://db-host:5432/snabel_accounting

# JWT
MP_JWT_VERIFY_ISSUER=https://snabel.no
SNABEL_JWT_DURATION_WEB=86400
SNABEL_JWT_DURATION_APP=2592000
```

## Future Enhancements

### 1. Additional Features
- Payment processing integration (Vipps, Stripe)
- VAT report generation (MVA-melding)
- PDF invoice generation
- Email notification system
- Audit log with complete history
- Multi-currency support with exchange rates
- Bank reconciliation

### 2. Performance
- Redis caching layer
- Database read replicas
- Query optimization
- GraphQL API for flexible queries

### 3. Security
- Two-factor authentication (2FA)
- API rate limiting
- IP whitelisting for admin operations
- Security audit logging
- Encrypted fields (sensitive data)

### 4. DevOps
- Kubernetes deployment
- Health checks and metrics
- Distributed tracing
- Log aggregation
- Automated backups
- CI/CD pipeline

### 5. Integration
- Altinn integration for Norwegian tax reporting
- Bank API integration (Open Banking)
- EHF invoice format support
- Accounting system export (e.g., Tripletex, Fiken)

## Monitoring and Observability

### Health Checks
- Quarkus health endpoints: `/q/health`
- Liveness: `/q/health/live`
- Readiness: `/q/health/ready`

### Metrics
- Micrometer metrics exposed
- Prometheus format available
- Custom business metrics

### Logging
- Structured logging with JSON format
- Log levels configurable per package
- MDC for request correlation

## Norwegian Accounting Compliance

### NS 4102 Standard
- Class 1: Assets (Eiendeler)
- Class 2: Equity and Liabilities (Egenkapital og gjeld)
- Class 3: Operating Income (Driftsinntekter)
- Class 4: Cost of Goods Sold (Varekostnad)
- Class 5: Payroll Expenses (Lønnskostnader)
- Class 6: Other Operating Expenses (Annen driftskostnad)
- Class 7: Financial Income and Expenses (Finansinntekter og -kostnader)
- Class 8: Tax (Skattekostnad)

### VAT Codes
- Code 0: No VAT (Ingen MVA)
- Code 3: 25% standard rate (Alminnelig sats)
- Code 33: 15% reduced rate (Redusert sats - mat)
- Code 5: VAT exempt (Fritatt)

### Bookkeeping Act (Bokføringsloven)
- 5-year retention requirement
- Audit trail mandatory
- Chronological posting order
- Balanced entries (debit = credit)

## Conclusion

The Snabel Accounting System is built with modern, production-ready technologies that ensure scalability, security, and compliance with Norwegian accounting standards. The fully reactive architecture provides excellent performance, while the multi-tenant design ensures proper customer isolation and security.
