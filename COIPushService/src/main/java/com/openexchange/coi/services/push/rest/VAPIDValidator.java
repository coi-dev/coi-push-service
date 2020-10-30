
package com.openexchange.coi.services.push.rest;

import java.net.URL;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import com.openexchange.coi.services.EndpointService;
import com.openexchange.coi.services.Profiles;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.exception.CoiServiceExceptionCodes;
import com.openexchange.coi.services.push.PushConfiguration;
import com.openexchange.coi.services.push.crypto.jwt.JWTHandler;
import com.openexchange.coi.services.push.crypto.jwt.JWTValidationResult;
import com.openexchange.coi.services.push.crypto.keys.parsing.PublicKeyParser;
import com.openexchange.coi.services.push.rest.cache.VapidCache;
import com.openexchange.coi.services.push.rest.header.VAPIDHeader;

/**
 * {@link VAPIDValidator} validates VAPID authentication header (RFC 8292)
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
@Service
@Profile(Profiles.PUSH)
public class VAPIDValidator {

    @Autowired
    private VapidCache vapidCache;

    private static Logger LOG = LoggerFactory.getLogger(VAPIDValidator.class);

    private static final String ALGORITHM_ES256 = "ES256";

    private final PushConfiguration config;
    private final EndpointService endpointService;

    /**
     * {@link PublicKeySource} - Defines a source for the public key material used for validating VAPID headers
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v1.0.0
     */
    public interface PublicKeySource {

        /**
         * Gets the public key material used for validating the VAPID header
         * 
         * @return The key material of the public key to use
         * @throws CoiServiceException Due an error while looking up the public key
         */
        byte[] getPublicKey() throws CoiServiceException;
    }

    /**
     * Initializes a new {@link VAPIDValidator}.
     * 
     * @param configuration The {@link PushConfiguration} which defines whether VAPID validation is enabled or not.
     * @param endpointService The {@link EndpointService} used to verify the provided "audience"-claim.
     */
    @Autowired
    public VAPIDValidator(PushConfiguration configuration, EndpointService endpointService) {
        this.config = Objects.requireNonNull(configuration, "configuration must not be null");
        this.endpointService = Objects.requireNonNull(endpointService, "endpointService must not be null");
    }

    /**
     * Validates the VAPID header of the a given {@link HttpServletRequest} against
     * a registered public key.
     * 
     * @param request The request to validate for VAPID
     * @return The key parsed from the vapid header or null, if the vapid validation is disabled
     * @throws CoiServiceException if the VAPID header and the contained signature could not be verified.
     */
    public void validateRequest(HttpServletRequest request, PublicKeySource keySource) throws CoiServiceException {
        if (config.isVapid()) {
            final String authorizationHeader = request.getHeader(HttpHeader.AUTHORIZATION.toString());
            if (authorizationHeader == null) {
                throw CoiServiceExceptionCodes.MISSING_VAPID_HEADER.create();
            }

            //get the JWT
            final VAPIDHeader vapidHeader = new VAPIDHeader(authorizationHeader);
            String jwt = vapidHeader.getJWT();
            if (jwt == null) {
                throw CoiServiceExceptionCodes.MISSING_VAPID_JWT_TOKEN.create();
            }

            String hash = DigestUtils.md5Hex(jwt).toUpperCase();

            String vapid = vapidCache.getVapid(hash);
            if (vapid != null) {
                LOG.debug("Vapid cache hit");
                return;
            } else {
                LOG.debug("Vapid cache miss");
            }

            //Get the provided public key from the request header
            String headerKey = vapidHeader.getKey();
            if (headerKey == null) {
                throw CoiServiceExceptionCodes.MISSING_VAPID_KEY.create();
            }

            //parse provided key
            PublicKey requestPublicKey = new PublicKeyParser().parse(headerKey);

            //The AUD claim is required as described in the VAPID RFC 8292  
            Map<String, Object> expectedClaims = new HashMap<String, Object>();
            final URL endpoint = endpointService.getEndpoint();
            expectedClaims.put(JWTHandler.AUD_CLAIM, Arrays.asList(endpoint.getProtocol() + "://" + endpoint.getHost()));
            //Validate the JWT with the header's key
            final JWTValidationResult validationResult = new JWTHandler(ALGORITHM_ES256).validateJWT(jwt, requestPublicKey, expectedClaims);
            if (validationResult.isFailure()) {
                throw CoiServiceExceptionCodes.VAPID_SIGNATURE_NOT_VALID.create(validationResult.getMessage());
            }

            // Get the key to check against from the key source only after verifying the signature with the request's public key
            //This allows a caller to defer key lookup until we are sure the VAPID information are at least consistent.
            final byte[] publicKeyMaterial = keySource.getPublicKey();
            if (publicKeyMaterial == null) {
                LOG.error("The key provided by the given keySource must not be null");
                throw CoiServiceExceptionCodes.VAPID_KEY_MISMATCH.create();
            }
            //Check if the the keys are the same; i.e the signature is also valid for the _stored_ key!
            if (!Arrays.equals(requestPublicKey.getEncoded(), publicKeyMaterial)) {
                LOG.error("The key provided by the given keySource does not match the one in the request.");
                throw CoiServiceExceptionCodes.VAPID_KEY_MISMATCH.create();
            }

            vapidCache.putInCache(hash, jwt);
        }
    }
}
