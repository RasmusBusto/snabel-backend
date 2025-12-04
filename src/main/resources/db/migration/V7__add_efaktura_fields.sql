-- Add fields required for eFaktura (EHF 3.0 / PEPPOL BIS Billing 3.0)

-- Add bank account information to customers (supplier/seller information)
ALTER TABLE customers ADD COLUMN bank_account VARCHAR(20);
ALTER TABLE customers ADD COLUMN bank_name VARCHAR(255);
ALTER TABLE customers ADD COLUMN iban VARCHAR(34);
ALTER TABLE customers ADD COLUMN swift_bic VARCHAR(11);

-- Add payment reference and additional EHF fields to invoices
ALTER TABLE invoices ADD COLUMN payment_reference VARCHAR(50); -- KID number or payment reference
ALTER TABLE invoices ADD COLUMN bank_account VARCHAR(20); -- Can override customer's default bank account
ALTER TABLE invoices ADD COLUMN buyer_reference VARCHAR(100); -- Customer's reference/order number
ALTER TABLE invoices ADD COLUMN contract_reference VARCHAR(100); -- Contract or agreement reference

-- Add line-level optional fields for EHF compliance
ALTER TABLE invoice_lines ADD COLUMN unit_code VARCHAR(10) DEFAULT 'EA'; -- Unit of measure code (EA=each, HUR=hour, etc.)
ALTER TABLE invoice_lines ADD COLUMN item_name VARCHAR(255); -- Product/service name
ALTER TABLE invoice_lines ADD COLUMN item_id VARCHAR(50); -- Product/service ID or SKU

-- Comments
COMMENT ON COLUMN customers.bank_account IS 'Norwegian bank account number (11 digits)';
COMMENT ON COLUMN customers.iban IS 'International Bank Account Number';
COMMENT ON COLUMN customers.swift_bic IS 'SWIFT/BIC code for international payments';
COMMENT ON COLUMN invoices.payment_reference IS 'KID number or payment reference for matching payments';
COMMENT ON COLUMN invoices.buyer_reference IS 'Customer reference or order number';
COMMENT ON COLUMN invoice_lines.unit_code IS 'UN/ECE unit code (EA, HUR, DAY, etc.)';
