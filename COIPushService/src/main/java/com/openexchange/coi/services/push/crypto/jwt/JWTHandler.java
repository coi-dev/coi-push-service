
package com.openexchange.coi.services.push.crypto.jwt;

import java.security.PublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SingleKeyJWSKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;

/**
 * {@link JWTHandler} provides functionality for creating, parsing and validating JSON Web Tokens (JWT) (RFC 7519)
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public class JWTHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JWTHandler.class);

    private boolean checkTokenExpiry = true;
    private final JWSAlgorithm expectedAlgorithm;

    /**
     * The constant name for the "audience" claim
     */
    public static final String AUD_CLAIM = "aud";

    /**
     * Initializes a new {@link JWTHandler}.
     * 
     * @param algorithm The algorithm expected in the JWT header; other algorithms will be considered as invalid
     */
    public JWTHandler(String expectedAlgorithm) {
        this.expectedAlgorithm = JWSAlgorithm.parse(expectedAlgorithm);
    }

    /**
     * Whether or not the handler should perform an expiration check of the JWT which is by default=true
     * 
     * @param check true to perform a expiration check while validating a token, false to ignore if the token is expired.
     * @return this
     */
    public JWTHandler withExpireCheck(boolean check) {
        this.checkTokenExpiry = check;
        return this;
    }

    /**
     * Parses and validates the given JWT
     * 
     * @param jwtString The JWT to parse and validate
     * @param publicKey The PublicKey used for signature validation
     * @return The {@link JWTValidationResult} representing the result of the validation
     */
    public JWTValidationResult validateJWT(String jwtString, PublicKey publicKey) {
        return validateJWT(jwtString, publicKey, null);
    }

    /**
     * Parses and validates the given JWT
     * 
     * @param jwtString The JWT to parse and validate
     * @param publicKey The PublicKey used for signature validation
     * @param expectedClaims A map of claims which must be included in the JWT in order to be valid, or null to disable the check for required claims.
     * @return The {@link JWTValidationResult} representing the result of the validation
     */
    public JWTValidationResult validateJWT(String jwtString, PublicKey publicKey, Map<String, Object> expectedClaims) {

        //Note: The DefaultJWTProcessor rejects "unsecured" JWTs with alg="none".
        final DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        try {
            jwtProcessor.setJWSKeySelector(new SingleKeyJWSKeySelector<>(expectedAlgorithm, publicKey));

            //Using a verifier for checking the token expiry and other expected claims
            jwtProcessor.setJWTClaimsSetVerifier(new JWTClaimsSetVerifier<SecurityContext>() {

                @Override
                public void verify(JWTClaimsSet claimsSet, SecurityContext context) throws BadJWTException {
                    //Check the expiray of the JWT
                    if (checkTokenExpiry) {
                        /**
                         * "vapid authentication is invalid if [...] the current time is later than the time identified in the "exp" (Expiry) claim
                         * or more than 24 hours before the expiry time" - (RFC 8292 - 4.2. Using Restricted Subscriptions)
                         */
                        final Date expirationTime = claimsSet.getExpirationTime();
                        if (expirationTime != null) {
                            final Date now = new Date();
                            if (now.after(expirationTime)) {
                                throw new BadJWTException("The \"exp\" claim from the the provided JWT is expired.");
                            }
                            final Date startValidTime = new Date(expirationTime.getTime() - Duration.ofHours(24).toMillis());
                            if (now.before(startValidTime)) {
                                throw new BadJWTException("The \"exp\" claim from the the provided JWT is more than 24h in future.");
                            }
                        } else {
                            throw new BadJWTException("The \"exp\" claim is missing in the provided JWT.");
                        }
                    }

                    //Check custom claims
                    if (expectedClaims != null) {
                        Map<String, Object> claims = claimsSet.getClaims();
                        for (Entry<String, Object> claim : expectedClaims.entrySet()) {
                            final String claimName = claim.getKey();
                            Object claimValue = claims.get(claimName);
                            if (claimValue != null) {
                                if (!claimValue.equals(claim.getValue())) {
                                    throw new BadJWTException("The claim \"" + claimName + "\" does not have the expected value of " + claim.getValue().toString() + ". Was: " + claimValue.toString());
                                }
                            } else {
                                throw new BadJWTException("Missing required JWT claim \"" + claimName + "\"");
                            }
                        }
                    }
                }
            });
            jwtProcessor.process(jwtString, null);
        } catch (ParseException | JOSEException e) {
            final JWTValidationResult failureResult = JWTValidationResult.failureResult(String.format("Error while parsing JWT token: %s", e.getMessage()));
            LOG.error(failureResult.getMessage());
            return failureResult;
        } catch (BadJOSEException e) {
            final JWTValidationResult failureResult = JWTValidationResult.failureResult(String.format("The JWT is rejected because the required claims are not satisfied: %s", e.getMessage()));
            LOG.error(failureResult.getMessage());
            return failureResult;
        }
        return JWTValidationResult.successResult();
    }
}
