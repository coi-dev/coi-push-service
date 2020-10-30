
package com.openexchange.coi.services.push.crypto.jwt.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.junit.Test;
import com.openexchange.coi.services.push.crypto.jwt.JWTHandler;
import com.openexchange.coi.services.push.crypto.jwt.JWTValidationResult;
import com.openexchange.coi.services.push.crypto.keys.parsing.KeyConverter;

/**
 * {@link JWTHandlerTest} - Contains test for {@link JWTHandler}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public class JWTHandlerTest {

    private static final String ALGORITHM_ES265 = "ES256";

    private interface TestData {

        PublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException;

        String getJWT();
    }

    private static List<TestData> validTestData;
    private static List<TestData> invalidTestData;
    static {
        validTestData = Arrays.asList(

            //X9.62 - Example from RFC 8292
            new TestData() {

                @Override
                public PublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
                    return createPublicKeyFromDER(KeyConverter.X962ToDER(Base64.getUrlDecoder().decode("BA1Hxzyi1RUM1b5wjxsn7nGxAszw2u61m164i3MrAIxHF6YK5h4SDYic-dRuU_RCPCfA5aq9ojSwk5Y2EmClBPs")));
                }

                @Override
                public String getJWT() {
                    return "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJodHRwczovL3B1c2guZXhhbXBsZS5uZXQiLCJleHAiOjE0NTM1MjM3NjgsInN1YiI6Im1haWx0bzpwdXNoQGV4YW1wbGUuY29tIn0.i3CYb7t4xfxCDquptFOepC9GAu_HLGkMlMuCGSK2rpiUfnK9ojFwDXb1JrErtmysazNjjvW2L9OkSSHzvoD1oA";
                }
            },

            //PEM 
            new TestData() {

                @Override
                public PublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
                    return createPublicKeyFromDER(Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEVs/o5+uQbTjL3chynL4wXgUg2R9q9UU8I5mEovUf86QZ7kOBIjJwqnzD1omageEHWwHdBO6B+dFabmdT9POxg=="));
                }

                @Override
                public String getJWT() {
                    return "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.tyh-VfuzIxCyGYDlkBA7DfyjrqmSHu6pQ2hoZuFqUSLPNY2N0mpHb3nk5K17HWP_3cYHBw7AhHale5wky6-sVA";
                }
            },

            new TestData() {

                @Override
                public PublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
                    return createPublicKeyFromDER(Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEVs/o5+uQbTjL3chynL4wXgUg2R9q9UU8I5mEovUf86QZ7kOBIjJwqnzD1omageEHWwHdBO6B+dFabmdT9POxg=="));
                }

                @Override
                public String getJWT() {
                    return "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiLjg5XnjpZeW-mypuKwu-mZqeSNgiIsImF1ZCI6Imh0dHBzOi8vMTAuNTAuMC40NyIsImV4cCI6MTU2MzQzNDMyOH0.ytAN6LtX0Kaoo1RhshZj_o61NF98xXoAUTZnHal6_xkRklnO8s4Kxl_x93W7TEcVc4saEJPsJow6WdPt0uFQcA";
                }
            });

        invalidTestData = Arrays.asList(
            //X9.62 - Example from RFC 8292 - This fails because the "exp" claim is expired
            new TestData() {

                @Override
                public PublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
                    return createPublicKeyFromDER(KeyConverter.X962ToDER(Base64.getUrlDecoder().decode("BA1Hxzyi1RUM1b5wjxsn7nGxAszw2u61m164i3MrAIxHF6YK5h4SDYic-dRuU_RCPCfA5aq9ojSwk5Y2EmClBPs")));
                }

                @Override
                public String getJWT() {
                    //The token contains a "exp" claim which is too far in the past
                    return "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJodHRwczovL3B1c2guZXhhbXBsZS5uZXQiLCJleHAiOjE0NTM1MjM3NjgsInN1YiI6Im1haWx0bzpwdXNoQGV4YW1wbGUuY29tIn0.i3CYb7t4xfxCDquptFOepC9GAu_HLGkMlMuCGSK2rpiUfnK9ojFwDXb1JrErtmysazNjjvW2L9OkSSHzvoD1oA";
                }
            },

            //PEM - Signature is invalid
            new TestData() {

                @Override
                public PublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
                    return createPublicKeyFromDER(Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEVs/o5+uQbTjL3chynL4wXgUg2R9q9UU8I5mEovUf86QZ7kOBIjJwqnzD1omageEHWwHdBO6B+dFabmdT9POxg=="));
                }

                @Override
                public String getJWT() {
                    //Signature is missing the last character
                    return "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.tyh-VfuzIxCyGYDlkBA7DfyjrqmSHu6pQ2hoZuFqUSLPNY2N0mpHb3nk5K17HWP_3cYHBw7AhHale5wky6-sV";
                }
            },

            //PEM - wrong public key
            new TestData() {

                @Override
                public PublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
                    //This is a wrong public key
                    return createPublicKeyFromDER(KeyConverter.X962ToDER(Base64.getUrlDecoder().decode("BED41xsyO5BuB8DSEC5GX3TtXNY3SDFUN4P_i1KYYnruzOxfX4fAolWuB32xn-T-hhfilvFIqev6JhYpni392XA")));
                }

                @Override
                public String getJWT() {
                    return "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJodHRwczovL3B1c2guZXhhbXBsZS5uZXQiLCJleHAiOjE0NTM1MjM3NjgsInN1YiI6Im1haWx0bzpwdXNoQGV4YW1wbGUuY29tIn0.i3CYb7t4xfxCDquptFOepC9GAu_HLGkMlMuCGSK2rpiUfnK9ojFwDXb1JrErtmysazNjjvW2L9OkSSHzvoD1oA";
                }
            });
    }

    private static PublicKey createPublicKeyFromDER(byte[] keyMaterial) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyMaterial);
        KeyFactory factory = KeyFactory.getInstance("EC");
        PublicKey privateKey = factory.generatePublic(spec);
        return privateKey;
    }

    /**
     * Tests a bunch of tokens which should be considered as valid
     */
    @Test
    public void testJWTData() throws Exception {
        //Since some tokens can contain a expire claim, we need to disable the expire check
        JWTHandler jwtHandler = new JWTHandler(ALGORITHM_ES265).withExpireCheck(false);
        for (TestData d : validTestData) {
            JWTValidationResult validationResult = jwtHandler.validateJWT(d.getJWT(), d.getKey());
            assertThat("The JWT should be valid", validationResult.isSuccess(), is(true));
            assertThat("The JWT should be valid", validationResult.isFailure(), is(false));
        }
    }

    /**
     * Tests a bunch of tokens which should not be considered as valid
     * 
     * @throws Exception
     */
    @Test
    public void testinvalidJWTShouldFail() throws Exception {
        JWTHandler jwtHandler = new JWTHandler(ALGORITHM_ES265);
        for (TestData d : invalidTestData) {
            JWTValidationResult validationResult = jwtHandler.validateJWT(d.getJWT(), d.getKey());
            assertThat("The JWT should NOT be valid", validationResult.isSuccess(), is(false));
            assertThat("The JWT should NOT be valid", validationResult.isFailure(), is(true));
        }
    }
}
