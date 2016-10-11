/**
 * Copyright (C) 2015 Michael Schnell. All rights reserved. 
 * http://www.fuin.org/
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.esmp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Random;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Generates a self-signed certificate for usage with the event store.
 */
@Mojo(name = "certificate", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresProject = false)
public final class EventStoreCertificateMojo extends AbstractMojo {

    private static final Logger LOG = LoggerFactory.getLogger(EventStoreCertificateMojo.class);

    /**
     * Path and name of the certificate file.
     */
    @Parameter(name = "certificate-file")
    private String certificateFile;

    @Override
    public final void execute() throws MojoExecutionException {
        StaticLoggerBinder.getSingleton().setMavenLog(getLog());

        LOG.info("certificateFile={}", certificateFile);

        if (certificateFile == null || certificateFile.trim().length() == 0) {
            LOG.info("Skipped generation: No certificate file given");
            return;
        }

        try {
            Security.addProvider(new BouncyCastleProvider());
            final File file = new File(certificateFile);
            createSelfSignedCertificate("test.com", file);
            LOG.info("Certificate successfully created");
        } catch (final RuntimeException ex) {
            throw new MojoExecutionException(
                    "Error generating a self-signed X509 certificate: " + certificateFile, ex);
        }

    }

    /**
     * Returns the path and name of the certificate file to generate.
     * 
     * @return Path and name of *.p12 file.
     */
    public final String getCertificateFile() {
        return certificateFile;
    }

    /**
     * Sets the path and name of the certificate file to generate.
     * 
     * @param certificateFile
     *            Path and name of *.p12 file.
     */
    public final void setCertificateFile(final String certificateFile) {
        this.certificateFile = certificateFile;
    }

    private static X509Certificate generateCertificate(final String domain, final KeyPair pair) {
        try {
            final X500Name issuerName = new X500Name("CN=" + domain);
            final X500Name subjectName = issuerName;
            final BigInteger serial = BigInteger.valueOf(new Random().nextInt());
            final Date notBefore = Date.from(LocalDateTime.of(2016, 1, 1, 0, 0).toInstant(ZoneOffset.UTC));
            final Date notAfter = Date.from(LocalDateTime.of(2099, 1, 1, 0, 0).toInstant(ZoneOffset.UTC));
            final X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuerName, serial,
                    notBefore, notAfter, subjectName, pair.getPublic());
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
            final ASN1EncodableVector purposes = new ASN1EncodableVector();
            purposes.add(KeyPurposeId.id_kp_serverAuth);
            builder.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));
            return signCertificate(builder, pair.getPrivate());
        } catch (final CertIOException ex) {
            throw new RuntimeException("Couldn't generate certificate", ex);
        }
    }

    private static KeyPair generateKeyPair(final int keySize) {
        try {
            final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA",
                    BouncyCastleProvider.PROVIDER_NAME);
            generator.initialize(keySize, new SecureRandom());
            return generator.generateKeyPair();
        } catch (final NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new RuntimeException("Couldn't generate key pair", ex);
        }
    }

    private static X509Certificate signCertificate(final X509v3CertificateBuilder certificateBuilder,
            final PrivateKey privateKey) {
        try {
            final ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(privateKey);
            return new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .getCertificate(certificateBuilder.build(signer));
        } catch (final OperatorCreationException | CertificateException ex) {
            throw new RuntimeException("Couldn't sign certificate", ex);
        }
    }

    private static void saveCertificateAsP12File(final String domain, final X509Certificate certificate,
            final PrivateKey key, final File file) {
        try {
            final char[] noPw = new char[] {};
            final KeyStore p12Store = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
            p12Store.load(null, null);
            p12Store.setKeyEntry(domain, key, noPw, new X509Certificate[] {certificate});
            final FileOutputStream fos = new FileOutputStream(file);
            try {
                p12Store.store(fos, noPw);
            } finally {
                fos.close();
            }
        } catch (final KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
                | NoSuchProviderException ex) {
            throw new RuntimeException("Couldn't save certificate", ex);
        }
    }

    private static void verify(final X509Certificate cert) {
        try {
            cert.checkValidity(new Date());
            cert.verify(cert.getPublicKey());
        } catch (final InvalidKeyException | SignatureException | CertificateException
                | NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new RuntimeException("Certificate verification failed", ex);
        }
    }

    private static void createSelfSignedCertificate(final String domain, final File file) {
        final KeyPair pair = generateKeyPair(1024);
        final X509Certificate cert = generateCertificate(domain, pair);
        verify(cert);
        saveCertificateAsP12File(domain, cert, pair.getPrivate(), file);
    }

}
