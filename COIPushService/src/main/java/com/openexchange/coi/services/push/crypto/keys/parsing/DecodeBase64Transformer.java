
package com.openexchange.coi.services.push.crypto.keys.parsing;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.regex.Pattern;

/**
 * {@link DecodeBase64Transformer} decodes BASE64 or BASE64-URL encoded key material.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public class DecodeBase64Transformer implements KeyMaterialTransformer {

    private static final Pattern URL_SAFE_BASE64 = Pattern.compile("[-,_]", Pattern.DOTALL);

    @Override
    public byte[] transform(byte[] keyMaterial) {
        String keyMaterialString = new String(keyMaterial, StandardCharsets.UTF_8);
        Decoder base64Decoder = URL_SAFE_BASE64.matcher(keyMaterialString).find() ? Base64.getUrlDecoder() : Base64.getDecoder();
        return base64Decoder.decode(keyMaterialString.getBytes(StandardCharsets.UTF_8));
    }

}
