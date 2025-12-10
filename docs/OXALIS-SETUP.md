# Oxalis PEPPOL Integration Setup Guide

## Overview

This guide explains how to set up Oxalis for sending EHF invoices via the PEPPOL network in the Snabel accounting system.

**Current Status:** Infrastructure ready, awaiting PEPPOL certificates and configuration.

## What is Oxalis?

Oxalis is an open-source PEPPOL Access Point that enables sending and receiving business documents (like invoices) via the PEPPOL network. It implements the AS4 protocol and handles all the complexity of PEPPOL messaging.

**License:** LGPL-3.0 (free for commercial use)

## Prerequisites Checklist

Before you can send invoices via PEPPOL, you need:

- [ ] PEPPOL certificates (Access Point certificate)
- [ ] PEPPOL participant ID registration
- [ ] Norwegian organization number (9 digits)
- [ ] Oxalis home directory configured
- [ ] Java 11+ (already satisfied by this project using Java 21)

## Architecture

```
┌──────────────────────────────────────────────────┐
│  Snabel Backend (Quarkus)                        │
│                                                   │
│  ┌────────────────────────────────────────────┐ │
│  │  PeppolAccessPointService                  │ │
│  │  - Uses Oxalis API                         │ │
│  │  - Sends EHF XML via PEPPOL                │ │
│  └────────────────────────────────────────────┘ │
│                │                                  │
│                ↓ Oxalis API (in-process)          │
│  ┌────────────────────────────────────────────┐ │
│  │  Oxalis Outbound Component                 │ │
│  │  - AS4 Protocol                            │ │
│  │  - SMP Lookup                              │ │
│  │  - Certificate handling                    │ │
│  └────────────────────────────────────────────┘ │
└───────────────────┼──────────────────────────────┘
                    │
                    ↓ HTTPS (AS4)
             ┌──────────────────┐
             │  PEPPOL Network  │
             │  - SMP Lookup    │
             │  - Access Points │
             └──────────────────┘
                    │
                    ↓
          ┌──────────────────────┐
          │  Recipient's         │
          │  Access Point        │
          └──────────────────────┘
```

## Implementation Status

### ✅ Completed
- [x] Oxalis dependencies added to `pom.xml`
- [x] `PeppolAccessPointService` created with full API integration
- [x] Configuration templates created
- [x] Application properties configured
- [x] Documentation written

### ⏳ Pending (Your Action Required)
- [ ] Obtain PEPPOL certificates
- [ ] Register as PEPPOL participant
- [ ] Create Oxalis home directory
- [ ] Import certificates to keystore
- [ ] Configure `oxalis.conf`
- [ ] Test on PEPPOL test network
- [ ] Enable in production

## Step-by-Step Setup

### Step 1: Create Oxalis Home Directory

```bash
# Create directory structure
sudo mkdir -p /opt/oxalis-home/{conf,inbound,outbound,evidence,logs}

# Set ownership (replace 'youruser' with actual user)
sudo chown -R youruser:youruser /opt/oxalis-home

# Set permissions
chmod 755 /opt/oxalis-home
chmod 755 /opt/oxalis-home/conf
chmod 755 /opt/oxalis-home/{inbound,outbound,evidence,logs}
```

### Step 2: Copy Configuration Template

```bash
# Copy template from project
cp src/main/resources/oxalis/oxalis.conf.example /opt/oxalis-home/conf/oxalis.conf

# Edit configuration
nano /opt/oxalis-home/conf/oxalis.conf
```

**Update these values:**
- `oxalis.participant.identifier` → Your org number (e.g., `0192:123456789`)
- `oxalis.keystore.password` → Your keystore password (use env var)

### Step 3: Obtain PEPPOL Certificates

#### Test Environment (Development)

Contact **Difi/Digdir** for test certificates:
- Website: https://www.digdir.no/
- Email: servicedesk@digdir.no
- Purpose: Testing PEPPOL integration
- Cost: Free for test network
- Delivery time: 1-2 weeks

#### Production Environment

For production certificates:
1. **Apply for PEPPOL membership**
   - Contact: Digdir (Norwegian PEPPOL Authority)
   - Form: https://www.digdir.no/peppol/bli-peppol-leverandor/1567

2. **Certificate cost**: ~€100-500 per year

3. **What you'll receive**:
   - Access Point certificate (P12 format)
   - Certificate password
   - Participant ID

4. **Timeline**: 2-4 weeks

### Step 4: Import PEPPOL Certificates

Once you receive your PEPPOL certificate (`.p12` file):

```bash
# Convert P12 to JKS format
keytool -importkeystore \
  -srckeystore your-peppol-cert.p12 \
  -srcstoretype PKCS12 \
  -srcstorepass YOUR_CERT_PASSWORD \
  -destkeystore /opt/oxalis-home/conf/oxalis-keystore.jks \
  -deststoretype JKS \
  -deststorepass YOUR_KEYSTORE_PASSWORD \
  -alias "peppol-ap"

# Verify import
keytool -list -keystore /opt/oxalis-home/conf/oxalis-keystore.jks \
  -storepass YOUR_KEYSTORE_PASSWORD

# Secure the keystore
chmod 600 /opt/oxalis-home/conf/oxalis-keystore.jks
```

### Step 5: Configure Environment Variables

Add to your environment (`.bashrc`, systemd service, or Docker env):

```bash
# Oxalis configuration
export OXALIS_HOME=/opt/oxalis-home
export OXALIS_KEYSTORE_PASSWORD=your_secure_password
export OXALIS_PARTICIPANT_ID=0192:YOUR_ORG_NUMBER
export OXALIS_ENABLED=true
```

**For systemd service** (`/etc/systemd/system/snabel-backend.service`):
```ini
[Service]
Environment="OXALIS_HOME=/opt/oxalis-home"
Environment="OXALIS_KEYSTORE_PASSWORD=your_secure_password"
Environment="OXALIS_PARTICIPANT_ID=0192:YOUR_ORG_NUMBER"
Environment="OXALIS_ENABLED=true"
```

**For Docker** (`docker-compose.yml`):
```yaml
services:
  snabel-backend:
    environment:
      - OXALIS_HOME=/opt/oxalis-home
      - OXALIS_KEYSTORE_PASSWORD=${OXALIS_KEYSTORE_PASSWORD}
      - OXALIS_PARTICIPANT_ID=0192:${ORG_NUMBER}
      - OXALIS_ENABLED=true
    volumes:
      - /opt/oxalis-home:/opt/oxalis-home
```

### Step 6: Enable Oxalis in Application

Update `application.properties`:

```properties
# For production
oxalis.enabled=true
```

Or set via environment variable:
```bash
export OXALIS_ENABLED=true
```

### Step 7: Test the Setup

#### Test 1: Verify Configuration

```bash
# Start the application
./mvnw quarkus:dev

# Check logs for:
# "Oxalis PEPPOL Access Point initialized (home: /opt/oxalis-home)"
```

#### Test 2: Send Test Invoice

You'll need to implement a test endpoint or use the existing send functionality once it's connected to `PeppolAccessPointService`.

```bash
# Example test (once API is wired up)
curl -X POST http://localhost:8080/api/invoices/123/send \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryMethod": "EHF"
  }'
```

#### Test 3: Verify on PEPPOL Test Network

Before production, test with PEPPOL test network:
1. Use test certificates
2. Update `oxalis.conf`:
   ```properties
   oxalis.smp.url=http://test-smp.difi.no
   ```
3. Send test invoice to a test recipient
4. Verify transmission in evidence directory

## Integration with Invoice Sending

The `PeppolAccessPointService` is ready to use. You need to wire it up in your invoice sending flow:

### Current Implementation

File: `src/main/java/no/snabel/service/PeppolAccessPointService.java`

**Key methods:**
- `sendInvoice(Invoice invoice)` - Send via PEPPOL
- `isReady()` - Check if Oxalis is configured

### Next Steps for Integration

1. **Create `InvoiceSendingService`** (orchestrator)
2. **Wire up the send endpoint** in `InvoiceResource`
3. **Add delivery tracking** (optional, recommended)

Example integration:

```java
@ApplicationScoped
public class InvoiceSendingService {

    @Inject
    PeppolAccessPointService peppolService;

    public Uni<SendResult> sendInvoice(Invoice invoice, SendMethod method) {
        if (method == SendMethod.EHF && peppolService.isReady()) {
            return peppolService.sendInvoice(invoice)
                .map(result -> mapToSendResult(result));
        }
        // ... other methods (email, eFaktura B2C)
    }
}
```

## Troubleshooting

### Issue: "Oxalis PEPPOL Access Point is disabled"

**Solution:**
- Set `oxalis.enabled=true` in `application.properties`
- Or set environment variable: `OXALIS_ENABLED=true`

### Issue: "Failed to initialize Oxalis"

**Possible causes:**
1. Keystore not found or wrong password
2. Oxalis home directory doesn't exist
3. Missing oxalis.conf file
4. Certificate issues

**Check:**
```bash
# Verify keystore exists
ls -la /opt/oxalis-home/conf/oxalis-keystore.jks

# Test keystore password
keytool -list -keystore /opt/oxalis-home/conf/oxalis-keystore.jks

# Check Oxalis home permissions
ls -la /opt/oxalis-home
```

### Issue: "Participant not found" when sending

**Solution:**
- Verify recipient's PEPPOL ID is correct
- Check recipient is registered in SMP
- Test SMP lookup: `http://test-smp.difi.no/iso6523-actorid-upis::0192:RECIPIENT_ORG_NUMBER`

### Issue: Certificate expired

**Solution:**
```bash
# Check certificate validity
keytool -list -v -keystore /opt/oxalis-home/conf/oxalis-keystore.jks \
  | grep "Valid from"

# Renew certificate (contact Difi/Digdir)
# Import new certificate (repeat Step 4)
```

## Cost Breakdown

| Item | Cost (NOK/year) | One-time |
|------|-----------------|----------|
| **Oxalis software** | Free | - |
| **PEPPOL test certificates** | Free | - |
| **PEPPOL prod certificates** | 1000-5000 | - |
| **PEPPOL membership (if required)** | 0-10000 | Yes |
| **Infrastructure (server)** | 0 | - |
| **Total (first year)** | ~1000-15000 | - |
| **Total (recurring)** | ~1000-5000 | - |

**Note:** Costs vary by provider and country. Norway has reasonable costs compared to other countries.

## Migrating to Oxalis-NG

When Oxalis-NG v1.2.0 becomes available on Maven Central:

### 1. Update pom.xml

```xml
<properties>
    <!-- Change from 6.3.0 to oxalis-ng version -->
    <oxalis.version>1.2.0</oxalis.version>
</properties>

<dependencies>
    <!-- Change groupId from network.oxalis to network.oxalis.ng -->
    <dependency>
        <groupId>network.oxalis.ng</groupId>
        <artifactId>oxalis-ng-api</artifactId>
        <version>${oxalis.version}</version>
    </dependency>
    <!-- ... repeat for other artifacts -->
</dependencies>
```

### 2. Update Java Imports

```java
// Change from:
import network.oxalis.api.*
import network.oxalis.outbound.*

// To:
import network.oxalis.ng.api.*
import network.oxalis.ng.outbound.*
```

### 3. Test Thoroughly

- Oxalis-NG has the same API structure
- Configuration files remain compatible
- Test sending on test network before production

## Resources

### Documentation
- **Oxalis GitHub:** https://github.com/OxalisCommunity/oxalis
- **Oxalis-NG:** https://github.com/OxalisCommunity/oxalis-ng
- **Oxalis Wiki:** https://github.com/OxalisCommunity/oxalis/wiki
- **PEPPOL:** https://peppol.org/

### Norwegian Resources
- **Digdir (PEPPOL Authority):** https://www.digdir.no/
- **EHF Specifications:** https://anskaffelser.dev/
- **Norwegian PEPPOL:** https://www.digdir.no/peppol/

### Support
- **Oxalis Community:** https://www.oxalis.network/
- **Slack:** NorStella Oxalis Slack channel
- **Email:** oxalis@norstella.no
- **GitHub Issues:** https://github.com/OxalisCommunity/oxalis-ng/issues

## Checklist

Use this checklist to track your setup progress:

- [ ] Created `/opt/oxalis-home` directory structure
- [ ] Copied `oxalis.conf.example` to `/opt/oxalis-home/conf/oxalis.conf`
- [ ] Edited `oxalis.conf` with correct organization number
- [ ] Applied for PEPPOL test certificates
- [ ] Received test certificates (`.p12` file)
- [ ] Imported certificates to keystore
- [ ] Set environment variables (`OXALIS_HOME`, `OXALIS_KEYSTORE_PASSWORD`)
- [ ] Enabled Oxalis (`oxalis.enabled=true`)
- [ ] Started application successfully
- [ ] Verified in logs: "Oxalis PEPPOL Access Point initialized"
- [ ] Sent test invoice on test network
- [ ] Verified transmission evidence
- [ ] Applied for production certificates (when ready)
- [ ] Switched to production SMP
- [ ] Tested production sending
- [ ] Monitored for issues

## Next Steps

After completing this setup:

1. **Implement InvoiceSendingService** - Orchestrate different sending methods
2. **Add delivery tracking** - Track PEPPOL transmission status
3. **Implement email fallback** - For recipients not on PEPPOL
4. **Add monitoring** - Alert on failed transmissions
5. **Document for users** - How to use PEPPOL in your system

---

**Last Updated:** December 2024
**Oxalis Version:** 6.3.0 (ready for oxalis-ng 1.2.0 migration)
**Status:** Infrastructure ready, awaiting certificates
