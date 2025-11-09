# Database Schema Documentation

This document describes the database structure for the Snabel Accounting System.

## Overview

The system uses PostgreSQL 16 with reactive async drivers. All database operations are performed asynchronously using Hibernate Reactive.

## Database Connection

- **JDBC URL**: `jdbc:postgresql://localhost:5432/snabel_accounting`
- **Reactive URL**: `postgresql://localhost:5432/snabel_accounting`
- **Username**: `snabel`
- **Password**: `snabel` (change in production!)

## Tables

### customers

Stores customer/company information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique identifier |
| organization_number | VARCHAR(9) | UNIQUE, NOT NULL | Norwegian organization number |
| company_name | VARCHAR(255) | NOT NULL | Company name |
| contact_person | VARCHAR(255) | | Primary contact person |
| email | VARCHAR(255) | | Contact email |
| phone | VARCHAR(20) | | Contact phone |
| address | VARCHAR(500) | | Street address |
| postal_code | VARCHAR(10) | | Postal code |
| city | VARCHAR(100) | | City name |
| country | VARCHAR(100) | DEFAULT 'Norge' | Country |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp |
| active | BOOLEAN | DEFAULT true | Active status |

**Indexes:**
- Primary key on `id`
- Unique index on `organization_number`

---

### users

Stores user accounts and authentication information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique identifier |
| customer_id | BIGINT | FK → customers(id), NOT NULL | Customer reference |
| username | VARCHAR(100) | UNIQUE, NOT NULL | Login username |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt password hash |
| email | VARCHAR(255) | NOT NULL | User email |
| full_name | VARCHAR(255) | | Full name |
| role | VARCHAR(50) | NOT NULL, DEFAULT 'USER' | User role |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp |
| last_login | TIMESTAMP | | Last login timestamp |
| active | BOOLEAN | DEFAULT true | Active status |

**Roles:**
- `USER` - Regular user (read-only)
- `ACCOUNTANT` - Can create/edit transactions
- `ADMIN` - Full access including user management

**Indexes:**
- Primary key on `id`
- Unique index on `username`
- Index on `customer_id`

---

### standard_accounts

Norwegian standard chart of accounts (NS 4102).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique identifier |
| account_number | VARCHAR(10) | UNIQUE, NOT NULL | Standard account number |
| account_name | VARCHAR(255) | NOT NULL | Account name |
| account_type | VARCHAR(50) | NOT NULL | Account type |
| account_class | VARCHAR(50) | | NS 4102 class (1-8) |
| vat_code | VARCHAR(10) | | Norwegian VAT code |
| description | TEXT | | Account description |
| is_system | BOOLEAN | DEFAULT true | System account flag |
| active | BOOLEAN | DEFAULT true | Active status |

**Account Types:**
- `ASSET` - Assets (Eiendeler)
- `LIABILITY` - Liabilities (Gjeld)
- `EQUITY` - Equity (Egenkapital)
- `REVENUE` - Revenue (Inntekter)
- `EXPENSE` - Expenses (Kostnader)

**Account Classes (NS 4102):**
- Class 1: Assets
- Class 2: Equity and Liabilities
- Class 3: Operating Income
- Class 4: Cost of Goods Sold
- Class 5: Payroll Expenses
- Class 6: Other Operating Expenses
- Class 7: Financial Income/Expenses
- Class 8: Tax

**VAT Codes:**
- `0` - No VAT
- `3` - 25% VAT (standard)
- `33` - 15% VAT (reduced rate)
- `5` - VAT exempt

---

### accounts

Customer-specific chart of accounts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique identifier |
| customer_id | BIGINT | FK → customers(id), NOT NULL | Customer reference |
| standard_account_id | BIGINT | FK → standard_accounts(id) | Standard account reference |
| account_number | VARCHAR(10) | NOT NULL | Account number |
| account_name | VARCHAR(255) | NOT NULL | Account name |
| account_type | VARCHAR(50) | NOT NULL | Account type |
| vat_code | VARCHAR(10) | | VAT code |
| balance | DECIMAL(19,2) | DEFAULT 0.00 | Current balance |
| currency | VARCHAR(3) | DEFAULT 'NOK' | Currency code |
| description | TEXT | | Account description |
| parent_account_id | BIGINT | FK → accounts(id) | Parent account for hierarchy |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp |
| active | BOOLEAN | DEFAULT true | Active status |

**Constraints:**
- Unique constraint on `(customer_id, account_number)`

**Indexes:**
- Primary key on `id`
- Index on `customer_id`
- Index on `standard_account_id`
- Unique index on `(customer_id, account_number)`

---

### journal_entries

Journal entry headers (Bilag/Posteringer).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique identifier |
| customer_id | BIGINT | FK → customers(id), NOT NULL | Customer reference |
| entry_number | VARCHAR(50) | | Entry number |
| entry_date | DATE | NOT NULL | Entry date |
| description | TEXT | NOT NULL | Entry description |
| reference | VARCHAR(100) | | External reference |
| entry_type | VARCHAR(50) | DEFAULT 'MANUAL' | Entry type |
| created_by | BIGINT | FK → users(id) | User who created entry |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp |
| posted | BOOLEAN | DEFAULT false | Posted status |
| posted_at | TIMESTAMP | | Posting timestamp |
| reversed | BOOLEAN | DEFAULT false | Reversed status |
| reversed_by | BIGINT | FK → journal_entries(id) | Reversing entry reference |

**Entry Types:**
- `MANUAL` - Manual entry
- `INVOICE` - From invoice
- `PAYMENT` - From payment
- `AUTOMATED` - System generated

**Constraints:**
- Unique constraint on `(customer_id, entry_number)`

**Indexes:**
- Primary key on `id`
- Index on `customer_id`
- Index on `entry_date`

---

### journal_entry_lines

Journal entry line items (Posteringslinjer).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique identifier |
| journal_entry_id | BIGINT | FK → journal_entries(id), NOT NULL | Journal entry reference |
| account_id | BIGINT | FK → accounts(id), NOT NULL | Account reference |
| description | TEXT | | Line description |
| debit_amount | DECIMAL(19,2) | DEFAULT 0.00 | Debit amount |
| credit_amount | DECIMAL(19,2) | DEFAULT 0.00 | Credit amount |
| vat_amount | DECIMAL(19,2) | DEFAULT 0.00 | VAT amount |
| vat_code | VARCHAR(10) | | VAT code |
| currency | VARCHAR(3) | DEFAULT 'NOK' | Currency code |
| exchange_rate | DECIMAL(10,6) | DEFAULT 1.000000 | Exchange rate |
| line_number | INT | | Line number |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |

**Constraints:**
- Check constraint: Either debit OR credit must be > 0, not both

**Indexes:**
- Primary key on `id`
- Index on `journal_entry_id`
- Index on `account_id`

---

### invoices

Invoice headers (Fakturaer).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique identifier |
| customer_id | BIGINT | FK → customers(id), NOT NULL | Customer reference |
| invoice_number | VARCHAR(50) | UNIQUE, NOT NULL | Invoice number |
| invoice_date | DATE | NOT NULL | Invoice date |
| due_date | DATE | NOT NULL | Payment due date |
| client_name | VARCHAR(255) | NOT NULL | Client name |
| client_organization_number | VARCHAR(9) | | Client org number |
| client_address | VARCHAR(500) | | Client address |
| client_postal_code | VARCHAR(10) | | Client postal code |
| client_city | VARCHAR(100) | | Client city |
| subtotal | DECIMAL(19,2) | DEFAULT 0.00 | Subtotal before VAT |
| vat_amount | DECIMAL(19,2) | DEFAULT 0.00 | Total VAT amount |
| total_amount | DECIMAL(19,2) | DEFAULT 0.00 | Total including VAT |
| currency | VARCHAR(3) | DEFAULT 'NOK' | Currency code |
| status | VARCHAR(50) | DEFAULT 'DRAFT' | Invoice status |
| payment_terms | VARCHAR(255) | | Payment terms |
| notes | TEXT | | Invoice notes |
| journal_entry_id | BIGINT | FK → journal_entries(id) | Related journal entry |
| created_by | BIGINT | FK → users(id) | User who created invoice |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp |
| sent_at | TIMESTAMP | | Sent timestamp |
| paid_at | TIMESTAMP | | Paid timestamp |

**Invoice Statuses:**
- `DRAFT` - Not yet sent
- `SENT` - Sent to client
- `PAID` - Payment received
- `OVERDUE` - Past due date
- `CANCELLED` - Cancelled

**Indexes:**
- Primary key on `id`
- Unique index on `invoice_number`
- Index on `customer_id`
- Index on `status`

---

### invoice_lines

Invoice line items (Fakturalinjer).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique identifier |
| invoice_id | BIGINT | FK → invoices(id), NOT NULL | Invoice reference |
| line_number | INT | NOT NULL | Line number |
| description | TEXT | NOT NULL | Line description |
| quantity | DECIMAL(10,2) | DEFAULT 1.00 | Quantity |
| unit_price | DECIMAL(19,2) | NOT NULL | Price per unit |
| vat_rate | DECIMAL(5,2) | DEFAULT 0.00 | VAT rate (0, 15, 25) |
| vat_amount | DECIMAL(19,2) | DEFAULT 0.00 | VAT amount |
| line_total | DECIMAL(19,2) | NOT NULL | Line total |
| account_id | BIGINT | FK → accounts(id) | Revenue account |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |

**Indexes:**
- Primary key on `id`
- Index on `invoice_id`

---

### payments

Payment records (Betalinger).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique identifier |
| customer_id | BIGINT | FK → customers(id), NOT NULL | Customer reference |
| invoice_id | BIGINT | FK → invoices(id) | Related invoice |
| payment_date | DATE | NOT NULL | Payment date |
| amount | DECIMAL(19,2) | NOT NULL | Payment amount |
| currency | VARCHAR(3) | DEFAULT 'NOK' | Currency code |
| payment_method | VARCHAR(50) | | Payment method |
| reference | VARCHAR(100) | | Payment reference |
| notes | TEXT | | Payment notes |
| journal_entry_id | BIGINT | FK → journal_entries(id) | Related journal entry |
| created_by | BIGINT | FK → users(id) | User who created payment |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |

**Payment Methods:**
- `BANK_TRANSFER` - Bank transfer
- `VIPPS` - Vipps payment
- `CARD` - Card payment
- `CASH` - Cash payment

**Indexes:**
- Primary key on `id`
- Index on `customer_id`
- Index on `invoice_id`

---

### vat_reports

VAT report entries (MVA-oppgave).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique identifier |
| customer_id | BIGINT | FK → customers(id), NOT NULL | Customer reference |
| period_start | DATE | NOT NULL | Period start date |
| period_end | DATE | NOT NULL | Period end date |
| total_sales_vat | DECIMAL(19,2) | DEFAULT 0.00 | Total sales VAT |
| total_purchase_vat | DECIMAL(19,2) | DEFAULT 0.00 | Total purchase VAT |
| net_vat | DECIMAL(19,2) | DEFAULT 0.00 | Net VAT to pay/receive |
| status | VARCHAR(50) | DEFAULT 'DRAFT' | Report status |
| submitted_at | TIMESTAMP | | Submission timestamp |
| created_by | BIGINT | FK → users(id) | User who created report |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp |

**Report Statuses:**
- `DRAFT` - Not submitted
- `SUBMITTED` - Submitted to tax authorities
- `PAID` - Payment completed

**Indexes:**
- Primary key on `id`
- Index on `customer_id`

---

### audit_log

Audit trail for all changes.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Unique identifier |
| customer_id | BIGINT | FK → customers(id) | Customer reference |
| user_id | BIGINT | FK → users(id) | User who made change |
| entity_type | VARCHAR(100) | NOT NULL | Entity type |
| entity_id | BIGINT | NOT NULL | Entity ID |
| action | VARCHAR(50) | NOT NULL | Action performed |
| old_values | JSONB | | Old values (JSON) |
| new_values | JSONB | | New values (JSON) |
| created_at | TIMESTAMP | NOT NULL | Change timestamp |

**Actions:**
- `CREATE` - Entity created
- `UPDATE` - Entity updated
- `DELETE` - Entity deleted

**Indexes:**
- Primary key on `id`
- Index on `customer_id`
- Index on `(entity_type, entity_id)`

---

## Migrations

The system uses Flyway for database migrations. Migration files are located in:
```
src/main/resources/db/migration/
```

**Current Migrations:**
- `V1__initial_schema.sql` - Creates all tables
- `V2__seed_norwegian_accounts.sql` - Seeds Norwegian standard accounts

## Data Integrity

### Cascading Deletes
- Deleting a customer cascades to all related records
- Deleting a journal entry cascades to all line items
- Deleting an invoice cascades to all line items

### Soft Deletes
- Users, accounts, and customers use soft delete (active flag)
- Actual deletion is prevented to maintain audit trail

### Constraints
- Foreign key constraints ensure referential integrity
- Check constraints validate business rules
- Unique constraints prevent duplicates

## Backup and Recovery

**Recommended Backup Strategy:**
1. Daily full backup
2. Continuous WAL archiving
3. Point-in-time recovery capability

**Backup Command:**
```bash
pg_dump -U snabel snabel_accounting > backup_$(date +%Y%m%d).sql
```

**Restore Command:**
```bash
psql -U snabel snabel_accounting < backup_20251109.sql
```

## Performance Considerations

1. **Indexes**: All foreign keys and frequently queried columns are indexed
2. **Connection Pooling**: Reactive driver uses async connection pooling
3. **Query Optimization**: Use prepared statements and parameterized queries
4. **Pagination**: Limit result sets for list queries

## Security

1. **Encryption at Rest**: Configure PostgreSQL for encryption
2. **Encryption in Transit**: Use SSL for database connections
3. **Row-Level Security**: Consider implementing for multi-tenancy
4. **Audit Logging**: All changes are tracked in audit_log table
