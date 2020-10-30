/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.coi.services.vault;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Objects;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.KubernetesAuthentication;
import org.springframework.vault.authentication.KubernetesAuthenticationOptions;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.client.RestTemplate;
import com.openexchange.coi.services.Profiles;
import com.openexchange.coi.services.validator.IsUUID;
import lombok.Getter;
import lombok.Setter;

/**
 * {@link Vault}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@Configuration
@Profile(Profiles.VAULT)
@ConfigurationProperties(prefix = "com.openexchange.coi.services.vault")
public class Vault extends AbstractVaultConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(Vault.class);
    private static final String VAULT_DATA = "data";

    private SSLAwareHttpRequestFactory factory;

    @Getter
    @Setter
    String role;

    @Getter
    @Setter
    String token;

    @Getter
    @Setter
    @IsUUID
    String endpoint;

    @Getter
    @Setter
    String path;

    @Getter
    @Setter
    String truststore;

    @Getter
    @Setter
    String truststorePassword;

    @Override
    public ClientAuthentication clientAuthentication() {
        if (getRole() != null) {
            KubernetesAuthenticationOptions options = KubernetesAuthenticationOptions.builder().role(role).build();

            try {
                RestTemplate temp = VaultClients.createRestTemplate(vaultEndpointProvider(), getFactory());
                return new KubernetesAuthentication(options, temp);
            } catch (CertificateException | IOException e) {
                LOG.error("Unable to load certificates for the vault connection.", e);
                throw new RuntimeException("Unable to load certificates for vault", e);
            }
        }
        return new TokenAuthentication(getToken());
    }

    @Override
    public VaultEndpoint vaultEndpoint() {
        try {
            return VaultEndpoint.from(new URI(endpoint));
        } catch (URISyntaxException e) {
            // should never happen
            LOG.error("Invalid vault endpoint. The endpoint is not a valid uri.", e);
            throw new RuntimeException("Invalid vault endpoint", e);
        }
    }

    /**
     * Gets the {@link SSLAwareHttpRequestFactory}
     *
     * @return The {@link SSLAwareHttpRequestFactory}
     * @throws CertificateException in case one of the certificates coulnd't be loaded
     * @throws IOException in case there is an I/O or format problem with the certificate. Or when the password is missing or wrong.
     */
    private SSLAwareHttpRequestFactory getFactory() throws CertificateException, IOException {
        if (factory != null) {
            return factory;
        }
        return factory = new SSLAwareHttpRequestFactory(getTruststore(), getTruststorePassword());
    }

    /**
     * Creates the {@link DataSource} for the db connection with a username and password from the vault.
     *
     * @param url The spring.datasource.url
     * @return The {@link DataSource}
     */
    @Bean
    public DataSource getDataSource(@Value("${spring.datasource.url}") String url) {
        ClientAuthentication auth = clientAuthentication();
        try {
            VaultTemplate vaultTemplate = new VaultTemplate(vaultEndpoint(), getFactory(), () -> auth.login());
            VaultResponse resp = vaultTemplate.read(path);
            if (resp == null) {
                LOG.error("Can't find db secrets under path " + path);
                throw new RuntimeException("Can't find db secrets under path " + path);
            }
            
            Object data = resp.getData().get(VAULT_DATA);
            if (data instanceof Map == false) {
                LOG.error("Can't find db secrets under path " + path);
                throw new RuntimeException("Can't find db secrets under path " + path);
            }
            @SuppressWarnings("unchecked") Map<String, String> secrets = (Map<String, String>) data;

            DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.username(secrets.get("spring.datasource.username").toString());
            dataSourceBuilder.password(secrets.get("spring.datasource.password").toString());
            dataSourceBuilder.url(url);
            return dataSourceBuilder.build();
        } catch (CertificateException | IOException e) {
            LOG.error("Unable to load certificates for the vault:" + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@link SSLAwareHttpRequestFactory} is a {@link ClientHttpRequestFactory} which accepts the configured self signed certificates.
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v1.0.0
     */
    private class SSLAwareHttpRequestFactory extends SimpleClientHttpRequestFactory {

        private static final String TL_SV1_2 = "TLSv1.2";
        private static final String JKS = "JKS";

        private KeyStore ks;
        private SSLSocketFactory sslSocketFactory;

        /**
         * Initializes a new {@link Vault.SSLAwareHttpRequestFactory}.
         * 
         * @throws IOException in case there is an I/O or format problem with the certificate. Or when the password is missing or wrong.
         * @throws CertificateException in case one of the certificates coulnd't be loaded
         */
        public SSLAwareHttpRequestFactory(String pathToTrustStore, String secret) throws CertificateException, IOException {
            super();
            Objects.nonNull(pathToTrustStore);
            Objects.nonNull(secret);
            try {
                ks = KeyStore.getInstance(JKS);
            } catch (KeyStoreException e) {
                // Should never be thrown
                LOG.error("Unknown keystore format: " + JKS);
                throw new RuntimeException("Unknown keystore format: " + JKS, e);
            }
            FileInputStream file = new FileInputStream(pathToTrustStore);
            try {
                ks.load(file, secret.toCharArray());
            } catch (NoSuchAlgorithmException e) {
                // Should never be thrown
                LOG.error("Unable to found algorithm to check trustore integrity: " + e.getMessage());
                throw new RuntimeException(e);
            }
            try {
                sslSocketFactory = createSSLSocketFactory(ks, secret);
            } catch (UnrecoverableKeyException e) {
                LOG.error("Invalid truststore password", e);
                throw new RuntimeException("Invalid trsutstore password", e);
            }
        }

        @Override
        public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
            return super.createRequest(uri, httpMethod);
        }

        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
            super.prepareConnection(connection, httpMethod);
            LOG.debug("Setting up vault connection");
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
                ((HttpsURLConnection) connection).setHostnameVerifier((a, b) -> true);
            }
        }

        /**
         * Creates a {@link SSLSocketFactory} which accepts the configured certificate
         *
         * @return The {@link SSLSocketFactory}
         * @throws UnrecoverableKeyException in case the secret is wrong
         */
        private SSLSocketFactory createSSLSocketFactory(KeyStore trustStore, String secret) throws UnrecoverableKeyException {
            try {
                KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmfactory.init(trustStore, secret.toCharArray());
                KeyManager[] keymanagers = kmfactory.getKeyManagers();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustStore);
                SSLContext sslContext = SSLContext.getInstance(TL_SV1_2);
                try {
                    sslContext.init(keymanagers, tmf.getTrustManagers(), new SecureRandom());
                } catch (KeyManagementException e) {
                    LOG.error("Unable to initialize ssl context: " + e.getMessage(), e);
                    throw new RuntimeException("Unable to initialize ssl context", e);
                }
                return sslContext.getSocketFactory();
            } catch (NoSuchAlgorithmException e) {
                // should never be thrown
                LOG.error("Missing cryptographic algorithm: " + e.getMessage(), e);
                throw new RuntimeException("Missing cryptographic algorithm", e);
            } catch (KeyStoreException e) {
                LOG.error("Unable to initalize keystore: " + e.getMessage(), e);
                throw new RuntimeException("Unable to initalize keystore", e);
            }
        }

    }

}
