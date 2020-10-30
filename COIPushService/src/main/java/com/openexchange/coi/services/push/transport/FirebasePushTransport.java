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

package com.openexchange.coi.services.push.transport;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.google.api.client.http.HttpResponseException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidConfig.Priority;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.openexchange.coi.services.Profiles;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.exception.CoiServiceExceptionCodes;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * {@link FirebasePushTransport} - a push transport which uses the firebase push service
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@Service
@Profile(Profiles.PUSH)
public class FirebasePushTransport implements PushTransport {

    private static final Logger LOG = LoggerFactory.getLogger(FirebasePushTransport.class);

    /**
     * The payload overhead in bytes, which effectively reduces the maximum possible size of a payload a coi server is allowed to sent.
     */
    private static final int PAYLOAD_OVERHEAD = 7;

    /**
     * The max payload size in bytes
     */
    private static final int MAX_PAYLOAD_SIZE = 4000; //4kb

    /**
     * The effective max payload size in bytes
     */
    private static final int MAX_EFFECTIVE_PAYLOAD_SIZE = MAX_PAYLOAD_SIZE - PAYLOAD_OVERHEAD;

    @Autowired
    private FirebaseConfiguration config;

    @Autowired
    private MeterRegistry registry;

    private FirebaseApp app = null;
    private Timer timer;

    /**
     * Initializes a new {@link FirebasePushTransport}.
     */
    private FirebasePushTransport() {
        super();
    }

    @Override
    public void transport(String token, boolean resourceValidated, byte[] data) throws CoiServiceException {
        if (app == null) {
            LOG.error("Firebase app not available. Probably a configuration issue. Fix the configuration and restart the server!");
            throw CoiServiceExceptionCodes.INVALID_CONFIGURATION.create();
        }
        long start = System.currentTimeMillis();

        byte[] base64data = Base64.getEncoder().encode(data);
        if (base64data.length > MAX_EFFECTIVE_PAYLOAD_SIZE) {
            throw CoiServiceExceptionCodes.MAX_PUSH_SIZE_EXCEEDED.create(MAX_EFFECTIVE_PAYLOAD_SIZE);
        }

        // @formatter:off
        Message message = Message.builder()
                                 .putData("content", new String(base64data, StandardCharsets.UTF_8))
                                 .setToken(token)
                                 .setAndroidConfig(AndroidConfig.builder()
                                                                .setPriority(Priority.HIGH)
                                                                .build())
                                 .setApnsConfig(ApnsConfig.builder()
                                                          .setAps(Aps.builder()
                                                                     .setSound("default")
                                                                     .setMutableContent(true)
                                                                      //The first (un-validated) push message should be a "Background Update Notification": therefore setting the contenAvailable flag
                                                                     .setContentAvailable(!resourceValidated)
                                                                     .build())
                                                          .putHeader("apns-priority", String.valueOf(10))
                                                          .build())
                                 .build();
        // @formatter:on

        try {
            String response = FirebaseMessaging.getInstance(app).send(message);
            timer.record(Duration.ofMillis(System.currentTimeMillis() - start));
            LOG.debug("Successfully sent message: " + response);
        } catch (FirebaseMessagingException e) {
            handleFirebaseException(e);
        }
    }

    /**
     * Handles errors returned by the firebase endpoint. E.g. by throwing an adequate {@link CoiServiceException}
     *
     * @param e The {@link FirebaseMessagingException}
     * @throws CoiServiceException
     */
    private void handleFirebaseException(FirebaseMessagingException e) throws CoiServiceException {
        // TODO handle yet unknown error
        if (e.getCause() instanceof HttpResponseException) {
            HttpResponseException ex = (HttpResponseException) e.getCause();
            int statusCode = ex.getStatusCode();
            if (statusCode == 400 || statusCode == 404 || statusCode == 403) {
                throw CoiServiceExceptionCodes.INVALID_PUSH_TOKEN.create();
            }
            if (statusCode == 429) {
                // Quota exceeded
                LOG.error("Firebase quota reached. Currenlty unable to send push messages. Please increase the puhs quota.");
                String retryAfter = ex.getHeaders().getRetryAfter();
                throw CoiServiceExceptionCodes.QUOTA_EXCEEDED.create(retryAfter);
            }
            if (statusCode == 401) {
                // APNS error
                return;
            }

            // Other errors like internal server errors
        }
    }

    /**
     * Initializes the firebase app
     */
    @PostConstruct
    public void init() {
        if (!config.isEnabled()) {
            LOG.info("The firebase transport is disabled and cannot be used!");
            return;
        }
        timer = registry.timer("com.openexchange.coi.services.push.transport.firebase.timer");
        FileInputStream serviceAccount;
        try {
            String file = config.getPrivateKey();
            if (file == null) {
                LOG.error("Unable to find credentials for the Firebase app. Please fix the configuration and restart the server!");
                return;
            }
            serviceAccount = new FileInputStream(file);
            FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setDatabaseUrl("https://sound-inn-182512.firebaseio.com").build();
            app = FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException e) {
            LOG.error("Unable to find credentials for the Firebase app. Please fix the configuration and restart the server!", e);
        } catch (IOException e) {
            LOG.error("Unable to find credentials for the Firebase app. Please fix the configuration and restart the server!", e);
        } catch (IllegalStateException e) {
            // Should already be initialized
            app = FirebaseApp.getInstance();
        }
    }

    @Override
    public boolean isEnabled() {
        return config.isEnabled();
    }

    @Override
    public Transport getTransport() {
        return Transport.firebase;
    }

    /**
     * {@link FirebaseHealth} extends the spring heatlth endpoint with an indicator for the firebase transport.
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v1.0.0
     */
    @Component
    @Profile(Profiles.PUSH + " & " + Profiles.HEALTH)
    public class FirebaseHealth extends AbstractHealthIndicator {

        @Override
        protected void doHealthCheck(Builder builder) throws Exception {
            if (config.isEnabled() == false) {
                builder.outOfService();
                return;
            }
            if (app == null) {
                builder.down();
                return;
            }
            builder.up();
        }
    }

}
