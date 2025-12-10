# Oxalis Configuration Setup

This directory contains template configuration files for Oxalis PEPPOL Access Point integration.

## Directory Structure

When setting up Oxalis, you need to create the following directory structure:

```
/opt/oxalis-home/
├── conf/
│   ├── oxalis.conf           # Main configuration file
│   └── oxalis-keystore.jks   # PEPPOL certificates
├── inbound/                   # Received PEPPOL messages
├── outbound/                  # Messages to be sent
├── evidence/                  # Transmission receipts (MDN)
└── logs/                      # Oxalis logs
```

## Quick Setup

### 1. Create Oxalis Home Directory

```bash
sudo mkdir -p /opt/oxalis-home/{conf,inbound,outbound,evidence,logs}
sudo chown -R $USER:$USER /opt/oxalis-home
```

### 2. Copy Configuration Template

```bash
cp src/main/resources/oxalis/oxalis.conf.example /opt/oxalis-home/conf/oxalis.conf
```

### 3. Edit Configuration

Edit `/opt/oxalis-home/conf/oxalis.conf`:
- Replace `YOUR_ORG_NUMBER` with your Norwegian organization number
- Set `OXALIS_KEYSTORE_PASSWORD` environment variable or update the config

### 4. Import PEPPOL Certificates

```bash
# Convert your PEPPOL certificate from P12 to JKS format
keytool -importkeystore \
  -srckeystore your-peppol-cert.p12 \
  -srcstoretype PKCS12 \
  -srcstorepass YOUR_CERT_PASSWORD \
  -destkeystore /opt/oxalis-home/conf/oxalis-keystore.jks \
  -deststoretype JKS \
  -deststorepass YOUR_KEYSTORE_PASSWORD
```

### 5. Set Environment Variables

Add to your shell profile or systemd service:

```bash
export OXALIS_HOME=/opt/oxalis-home
export OXALIS_KEYSTORE_PASSWORD=your_secure_password
```

### 6. Configure Application

Update `src/main/resources/application.properties`:

```properties
# Oxalis home directory
oxalis.home=/opt/oxalis-home
oxalis.enabled=true
```

## PEPPOL Certificate Acquisition

### Test Environment

For testing, you can get test certificates from:
- **Norway:** Contact Difi/Digdir at https://www.digdir.no/
- **Test network:** https://peppol.org/get-involved/join-openpeppol/peppol-test-network/

### Production Environment

For production PEPPOL certificates:
1. Contact a PEPPOL Authority in your country
2. In Norway: Digdir (https://www.digdir.no/)
3. Cost: Typically €100-500 per year
4. Process: 1-2 weeks

You'll receive:
- Access Point certificate (for sending/receiving)
- SMP certificate (for service metadata)

## Testing Oxalis Setup

### Test Certificate Import

```bash
keytool -list -keystore /opt/oxalis-home/conf/oxalis-keystore.jks
```

### Test Configuration

```bash
# Set Java system property to point to Oxalis home
java -Doxalis.home=/opt/oxalis-home -jar your-app.jar
```

## Integration Modes

### Mode 1: Standalone Service (Recommended for Development)

Run Oxalis as a separate service:

```bash
# Download Oxalis standalone
wget https://github.com/OxalisCommunity/oxalis/releases/download/v6.3.0/oxalis-standalone.jar

# Run Oxalis
java -Doxalis.home=/opt/oxalis-home -jar oxalis-standalone.jar
```

### Mode 2: API Integration (Production)

Embed Oxalis in your application (already configured in pom.xml):
- Dependencies added to pom.xml
- PeppolAccessPointService created in Java code
- Configuration via application.properties

## Troubleshooting

### Certificate Issues

```bash
# Check certificate validity
keytool -list -v -keystore /opt/oxalis-home/conf/oxalis-keystore.jks
```

### Connection Issues

- Verify SMP URL is correct
- Check firewall allows outbound HTTPS (port 443)
- Test SMP lookup manually: https://test-smp.difi.no/iso6523-actorid-upis%3A%3A0192%3AORG_NUMBER

### Directory Permissions

```bash
# Fix permissions
chmod 755 /opt/oxalis-home
chmod 755 /opt/oxalis-home/conf
chmod 600 /opt/oxalis-home/conf/oxalis-keystore.jks
```

## Resources

- **Oxalis Documentation:** https://github.com/OxalisCommunity/oxalis/wiki
- **Oxalis-NG:** https://github.com/OxalisCommunity/oxalis-ng
- **PEPPOL Network:** https://peppol.org/
- **Norwegian EHF:** https://anskaffelser.dev/
- **Support:** oxalis@norstella.no or Slack channel

## Migration to Oxalis-NG

When oxalis-ng artifacts become available on Maven Central:

1. Update `pom.xml`:
```xml
<oxalis.version>1.2.0</oxalis.version>
<groupId>network.oxalis.ng</groupId>
```

2. Configuration remains the same (backward compatible)

3. Rebuild and test
