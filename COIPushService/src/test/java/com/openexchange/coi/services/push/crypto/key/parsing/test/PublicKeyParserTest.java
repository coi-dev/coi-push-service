
package com.openexchange.coi.services.push.crypto.key.parsing.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.push.crypto.keys.parsing.PublicKeyParser;

/**
 * 
 * {@link PublicKeyParserTest} Contains tests for {@link PublicKeyParser}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public class PublicKeyParserTest {

    /**
     * Test keys can be created with openssl:
     * openssl ecparam -genkey -name prime256v1 > test.key
     * openssl pkey -in test.key -pubout
     */

    //Various test keys in different formats
    //@formatter:off
    private final List<String> validKeyMaterialTestSet = Arrays.asList(

        //PEM
        "-----BEGIN PUBLIC KEY-----" + 
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEVs/o5+uQbTjL3chynL4wXgUg2R9" + 
        "q9UU8I5mEovUf86QZ7kOBIjJwqnzD1omageEHWwHdBO6B+dFabmdT9POxg==" + 
        "-----END PUBLIC KEY-----",

        //DER
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEVs/o5+uQbTjL3chynL4wXgUg2R9q9UU8I5mEovUf86QZ7kOBIjJwqnzD1omageEHWwHdBO6B+dFabmdT9POxg==",

        //RAW X962
        "BA1Hxzyi1RUM1b5wjxsn7nGxAszw2u61m164i3MrAIxHF6YK5h4SDYic-dRuU_RCPCfA5aq9ojSwk5Y2EmClBPs"

        );
    //@formatter:on

    /**
     * Tests if we are able to parse the provided key materials to a {@link PublicKey} instance
     * 
     * @throws CoiServiceException Due a parser error
     */
    @Test
    public void testParseValidKeyMaterial() throws CoiServiceException {
        PublicKeyParser keyParser = new PublicKeyParser();
        for (String m : validKeyMaterialTestSet) {
            PublicKey parsedKey = keyParser.parse(m);
            assertThat("The parsed key must not be null", parsedKey, notNullValue());
        }
    }
}
