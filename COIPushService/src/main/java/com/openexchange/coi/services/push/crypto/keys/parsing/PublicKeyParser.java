
package com.openexchange.coi.services.push.crypto.keys.parsing;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.exception.CoiServiceExceptionCodes;

/**
 * {@link PublicKeyParser} transforms key material to a suitable input and parses {@link PublicKey} objects from it.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public class PublicKeyParser {

    private static final Logger LOG = LoggerFactory.getLogger(PublicKeyParser.class);

    //@formatter:off
    private static final List<KeyMaterialTransformer> DEFAULT_KEY_TRANSFORMER = Arrays.asList(
        new RemovePEMHeaderTransformer(), 
        new DecodeBase64Transformer(), 
        new DERHeaderTransformer());
    //@formatter:on

    private final List<KeyMaterialTransformer> keyMaterialHandler;

    /***
     * Initializes a new {@link PublicKeyParser} with a set of internal default {@link KeyMaterialTransformer}.
     */
    public PublicKeyParser() {
        this(DEFAULT_KEY_TRANSFORMER);
    }

    /**
     * Initializes a new {@link PublicKeyParser}.
     * 
     * @param keyMaterialHandler A set of {@link KeyMaterialTransformer} to use
     */
    public PublicKeyParser(List<KeyMaterialTransformer> keyMaterialHandler) {
        this.keyMaterialHandler = keyMaterialHandler;
    }

    private PublicKey createPublicKeyFromDER(byte[] keyMaterial) throws CoiServiceException {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyMaterial);
            KeyFactory factory = KeyFactory.getInstance("EC");
            PublicKey privateKey = factory.generatePublic(spec);
            return privateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw CoiServiceExceptionCodes.INVALID_PUBLIC_KEY.create();
        }
    }

    /**
     * Transforms the given key material with all known {@link KeyMaterialTransformer}
     * 
     * @param keyMaterial The key to transform
     * @return The raw key material
     */
    private byte[] transform(String keyMaterial) {
        byte[] material = keyMaterial.getBytes(StandardCharsets.UTF_8);
        for (KeyMaterialTransformer h : keyMaterialHandler) {
            material = h.transform(material);
        }
        return material;
    }

    /**
     * Applies all known {@link KeyMaterialTransformer}s to the given material and parses a {@link PublicKey} from it.
     * 
     * @param keyMaterial The key to parse
     * @return The parsed {@link PublicKey}
     * @throws CoiServiceException
     */
    public PublicKey parse(String keyMaterial) throws CoiServiceException {
        try {
            Date t0 = new Date();
            PublicKey key = createPublicKeyFromDER(transform(keyMaterial));
            Date t1 = new Date();
            LOG.debug("Took {} ms to parse public key.", t1.getTime() - t0.getTime());
            return key;
        } catch (IllegalArgumentException e) {
            //Invalid Base64
            throw CoiServiceExceptionCodes.INVALID_PUBLIC_KEY.create();
        }
    }
}
