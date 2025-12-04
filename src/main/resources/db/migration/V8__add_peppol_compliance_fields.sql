-- Migration to add PEPPOL BIS 3.0 and Norwegian NS4102 compliance fields
-- Adds electronic addresses and order reference for full standard compliance

-- Add electronic address fields to customers table (for seller/supplier)
ALTER TABLE customers ADD COLUMN IF NOT EXISTS endpoint_id VARCHAR(100);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS endpoint_scheme VARCHAR(20) DEFAULT '0192';

-- Remove bank_name column (not used in EHF XML per UBL-CR-429 or PDF invoices)
ALTER TABLE customers DROP COLUMN IF EXISTS bank_name;

-- Add electronic address and order reference fields to invoices table (for buyer/customer)
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS order_reference VARCHAR(100);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS client_endpoint_id VARCHAR(100);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS client_endpoint_scheme VARCHAR(20) DEFAULT '0192';

-- Add comments
COMMENT ON COLUMN customers.endpoint_id IS 'Electronic address for PEPPOL network (PEPPOL-EN16931-R020)';
COMMENT ON COLUMN customers.endpoint_scheme IS 'Scheme identifier for endpoint (default 0192 = Norwegian org number)';
COMMENT ON COLUMN invoices.order_reference IS 'Purchase order reference (alternative to buyer_reference per PEPPOL-EN16931-R003)';
COMMENT ON COLUMN invoices.client_endpoint_id IS 'Buyer electronic address for PEPPOL network (PEPPOL-EN16931-R010)';
COMMENT ON COLUMN invoices.client_endpoint_scheme IS 'Scheme identifier for buyer endpoint (default 0192 = Norwegian org number)';
