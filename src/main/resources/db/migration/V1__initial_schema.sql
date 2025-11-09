-- Snabel Accounting System - Initial Schema
-- Norwegian Accounting System compliant with NS 4102

-- Customers table (Kunder)
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    organization_number VARCHAR(9) UNIQUE NOT NULL,  -- Norwegian org number (9 digits)
    company_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    postal_code VARCHAR(10),
    city VARCHAR(100),
    country VARCHAR(100) DEFAULT 'Norge',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT true
);

-- Users table (Brukere) - for authentication
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'USER', -- USER, ADMIN, ACCOUNTANT
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    active BOOLEAN DEFAULT true
);

-- Standard Chart of Accounts (Standard kontoplan NS 4102)
CREATE TABLE standard_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(10) UNIQUE NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL, -- ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    account_class VARCHAR(50),  -- Class 1-8 in Norwegian standard
    vat_code VARCHAR(10),  -- Norwegian VAT codes (0%, 15%, 25%, etc.)
    description TEXT,
    is_system BOOLEAN DEFAULT true,
    active BOOLEAN DEFAULT true
);

-- Customer-specific Chart of Accounts (Kunde-spesifikk kontoplan)
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    standard_account_id BIGINT REFERENCES standard_accounts(id),
    account_number VARCHAR(10) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    vat_code VARCHAR(10),
    balance DECIMAL(19, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'NOK',
    description TEXT,
    parent_account_id BIGINT REFERENCES accounts(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT true,
    UNIQUE(customer_id, account_number)
);

-- Journal Entries (Bilag/Posteringer)
CREATE TABLE journal_entries (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    entry_number VARCHAR(50),
    entry_date DATE NOT NULL,
    description TEXT NOT NULL,
    reference VARCHAR(100),  -- External reference (invoice number, etc.)
    entry_type VARCHAR(50) DEFAULT 'MANUAL', -- MANUAL, INVOICE, PAYMENT, AUTOMATED
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    posted BOOLEAN DEFAULT false,
    posted_at TIMESTAMP,
    reversed BOOLEAN DEFAULT false,
    reversed_by BIGINT REFERENCES journal_entries(id),
    UNIQUE(customer_id, entry_number)
);

-- Journal Entry Lines (Posteringslinjer)
CREATE TABLE journal_entry_lines (
    id BIGSERIAL PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL REFERENCES journal_entries(id) ON DELETE CASCADE,
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    description TEXT,
    debit_amount DECIMAL(19, 2) DEFAULT 0.00,
    credit_amount DECIMAL(19, 2) DEFAULT 0.00,
    vat_amount DECIMAL(19, 2) DEFAULT 0.00,
    vat_code VARCHAR(10),
    currency VARCHAR(3) DEFAULT 'NOK',
    exchange_rate DECIMAL(10, 6) DEFAULT 1.000000,
    line_number INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_debit_or_credit CHECK (
        (debit_amount > 0 AND credit_amount = 0) OR
        (credit_amount > 0 AND debit_amount = 0)
    )
);

-- Invoices (Fakturaer)
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    client_organization_number VARCHAR(9),
    client_address VARCHAR(500),
    client_postal_code VARCHAR(10),
    client_city VARCHAR(100),
    subtotal DECIMAL(19, 2) DEFAULT 0.00,
    vat_amount DECIMAL(19, 2) DEFAULT 0.00,
    total_amount DECIMAL(19, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'NOK',
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, SENT, PAID, OVERDUE, CANCELLED
    payment_terms VARCHAR(255),
    notes TEXT,
    journal_entry_id BIGINT REFERENCES journal_entries(id),
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    paid_at TIMESTAMP
);

-- Invoice Lines (Fakturalinjer)
CREATE TABLE invoice_lines (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    line_number INT NOT NULL,
    description TEXT NOT NULL,
    quantity DECIMAL(10, 2) DEFAULT 1.00,
    unit_price DECIMAL(19, 2) NOT NULL,
    vat_rate DECIMAL(5, 2) DEFAULT 0.00, -- 0.00, 15.00, 25.00 for Norway
    vat_amount DECIMAL(19, 2) DEFAULT 0.00,
    line_total DECIMAL(19, 2) NOT NULL,
    account_id BIGINT REFERENCES accounts(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Payments (Betalinger)
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    invoice_id BIGINT REFERENCES invoices(id),
    payment_date DATE NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'NOK',
    payment_method VARCHAR(50), -- BANK_TRANSFER, VIPPS, CARD, CASH
    reference VARCHAR(100),
    notes TEXT,
    journal_entry_id BIGINT REFERENCES journal_entries(id),
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- VAT Report entries (MVA-oppgave)
CREATE TABLE vat_reports (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_sales_vat DECIMAL(19, 2) DEFAULT 0.00,
    total_purchase_vat DECIMAL(19, 2) DEFAULT 0.00,
    net_vat DECIMAL(19, 2) DEFAULT 0.00,
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, SUBMITTED, PAID
    submitted_at TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Audit log for all changes
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id),
    user_id BIGINT REFERENCES users(id),
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE
    old_values JSONB,
    new_values JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_users_customer ON users(customer_id);
CREATE INDEX idx_accounts_customer ON accounts(customer_id);
CREATE INDEX idx_accounts_standard ON accounts(standard_account_id);
CREATE INDEX idx_journal_entries_customer ON journal_entries(customer_id);
CREATE INDEX idx_journal_entries_date ON journal_entries(entry_date);
CREATE INDEX idx_journal_entry_lines_journal ON journal_entry_lines(journal_entry_id);
CREATE INDEX idx_journal_entry_lines_account ON journal_entry_lines(account_id);
CREATE INDEX idx_invoices_customer ON invoices(customer_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoice_lines_invoice ON invoice_lines(invoice_id);
CREATE INDEX idx_payments_customer ON payments(customer_id);
CREATE INDEX idx_payments_invoice ON payments(invoice_id);
CREATE INDEX idx_vat_reports_customer ON vat_reports(customer_id);
CREATE INDEX idx_audit_log_customer ON audit_log(customer_id);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
