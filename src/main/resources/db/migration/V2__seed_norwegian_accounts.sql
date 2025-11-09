-- Norwegian Standard Chart of Accounts (NS 4102)
-- This is a simplified version covering the main account classes

-- Class 1: ASSETS (EIENDELER)
INSERT INTO standard_accounts (account_number, account_name, account_type, account_class, description) VALUES
('1000', 'Eiendeler', 'ASSET', '1', 'Hovedkonto for eiendeler'),
('1100', 'Anleggsmidler', 'ASSET', '1', 'Varige driftsmidler og immaterielle eiendeler'),
('1200', 'Tomter, bygninger og annen fast eiendom', 'ASSET', '1', 'Fast eiendom'),
('1300', 'Maskiner, inventar, transportmidler mv.', 'ASSET', '1', 'Maskiner og utstyr'),
('1400', 'Driftsløsøre, inventar, verktøy og lignende', 'ASSET', '1', 'Inventar og verktøy'),
('1500', 'Varelager', 'ASSET', '1', 'Varebeholdning'),
('1600', 'Kundefordringer', 'ASSET', '1', 'Fordringer på kunder'),
('1900', 'Bankinnskudd, kontanter og lignende', 'ASSET', '1', 'Likvide midler');

-- Class 2: EQUITY AND LIABILITIES (EGENKAPITAL OG GJELD)
INSERT INTO standard_accounts (account_number, account_name, account_type, account_class, description) VALUES
('2000', 'Egenkapital og gjeld', 'LIABILITY', '2', 'Hovedkonto for egenkapital og gjeld'),
('2050', 'Innskutt egenkapital', 'EQUITY', '2', 'Aksjekapital og overkurs'),
('2060', 'Egenkapital', 'EQUITY', '2', 'Egenkapital'),
('2100', 'Opptjent egenkapital', 'EQUITY', '2', 'Annen egenkapital og fond'),
('2400', 'Leverandørgjeld', 'LIABILITY', '2', 'Gjeld til leverandører'),
('2600', 'Skyldig skattetrekk', 'LIABILITY', '2', 'Skattetrekk arbeidstakere'),
('2700', 'Skyldig merverdiavgift', 'LIABILITY', '2', 'Merverdiavgift'),
('2740', 'Påløpt feriepenger', 'LIABILITY', '2', 'Feriepenger'),
('2770', 'Skyldige offentlige avgifter', 'LIABILITY', '2', 'Offentlige avgifter'),
('2900', 'Annen kortsiktig gjeld', 'LIABILITY', '2', 'Annen gjeld');

-- Class 3: OPERATING INCOME (DRIFTSINNTEKTER)
INSERT INTO standard_accounts (account_number, account_name, account_type, account_class, vat_code, description) VALUES
('3000', 'Salgsinntekt', 'REVENUE', '3', '3', 'Inntekter fra salg av varer og tjenester (25% MVA)'),
('3100', 'Salgsinntekt avgiftspliktig', 'REVENUE', '3', '3', 'Salgsinntekt med 25% MVA'),
('3110', 'Salgsinntekt avgiftspliktig 15%', 'REVENUE', '3', '33', 'Salgsinntekt med 15% MVA (næringsmidler)'),
('3300', 'Salgsinntekt avgiftsfri', 'REVENUE', '3', '5', 'Salgsinntekt uten MVA'),
('3900', 'Annen driftsinntekt', 'REVENUE', '3', '5', 'Annen inntekt');

-- Class 4: COST OF GOODS SOLD (VAREKJØP)
INSERT INTO standard_accounts (account_number, account_name, account_type, account_class, vat_code, description) VALUES
('4000', 'Varekjøp', 'EXPENSE', '4', '3', 'Kjøp av varer for videresalg'),
('4005', 'Varekjøp med fradragsrett 25%', 'EXPENSE', '4', '3', 'Varekjøp med full MVA-fradragsrett'),
('4015', 'Varekjøp med fradragsrett 15%', 'EXPENSE', '4', '33', 'Varekjøp med 15% MVA');

-- Class 5: PAYROLL EXPENSES (LØNNSKOSTNADER)
INSERT INTO standard_accounts (account_number, account_name, account_type, account_class, description) VALUES
('5000', 'Lønnskostnader', 'EXPENSE', '5', 'Alle lønnsrelaterte kostnader'),
('5100', 'Lønn til ansatte', 'EXPENSE', '5', 'Brutto lønn'),
('5400', 'Arbeidsgiveravgift', 'EXPENSE', '5', 'Arbeidsgiveravgift'),
('5900', 'Andre personalkostnader', 'EXPENSE', '5', 'Andre lønnskostnader');

-- Class 6: OTHER OPERATING EXPENSES (ANDRE DRIFTSKOSTNADER)
INSERT INTO standard_accounts (account_number, account_name, account_type, account_class, vat_code, description) VALUES
('6000', 'Andre driftskostnader', 'EXPENSE', '6', '', 'Diverse driftskostnader'),
('6100', 'Leie av lokaler', 'EXPENSE', '6', '3', 'Kontorleie'),
('6300', 'Elektrisitet, oppvarming', 'EXPENSE', '6', '3', 'Strøm og oppvarming'),
('6340', 'Telefon', 'EXPENSE', '6', '3', 'Telefoni og kommunikasjon'),
('6360', 'Porto', 'EXPENSE', '6', '3', 'Postutgifter'),
('6540', 'Kontorrekvisita', 'EXPENSE', '6', '3', 'Kontormateriell'),
('6700', 'Regnskapstjenester', 'EXPENSE', '6', '3', 'Regnskapsførsel'),
('6800', 'Kontingenter og gaver', 'EXPENSE', '6', '0', 'Medlemskap og gaver'),
('6900', 'Annen fremmed tjeneste', 'EXPENSE', '6', '3', 'Konsulenter og andre tjenester');

-- Class 7: FINANCIAL INCOME AND EXPENSES (FINANSINNTEKTER OG KOSTNADER)
INSERT INTO standard_accounts (account_number, account_name, account_type, account_class, description) VALUES
('7000', 'Finansinntekter og kostnader', 'EXPENSE', '7', 'Finansielle poster'),
('8050', 'Renteinntekter', 'REVENUE', '7', 'Renteinntekter fra bank'),
('8150', 'Rentekostnader', 'EXPENSE', '7', 'Rentekostnader'),
('8160', 'Rentekostnader til kredittinstitusjoner', 'EXPENSE', '7', 'Bankrente'),
('8300', 'Valutatap', 'EXPENSE', '7', 'Tap på valuta'),
('8310', 'Valutagevinst', 'REVENUE', '7', 'Gevinst på valuta');

-- Class 8: TAX (SKATTEKOSTNAD)
INSERT INTO standard_accounts (account_number, account_name, account_type, account_class, description) VALUES
('8960', 'Skattekostnad', 'EXPENSE', '8', 'Beregnet skatt'),
('8980', 'Årsresultat', 'EQUITY', '8', 'Resultat for året');
