
package com.openexchange.coi.services.push.crypto.keys.parsing;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link RemovePEMHeaderTransformer} Removes the PEM header and footer from the key material if present.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public class RemovePEMHeaderTransformer implements KeyMaterialTransformer {

    private static final Pattern PEM = Pattern.compile("-----.*-----(.*)-----.*-----", Pattern.DOTALL);

    @Override
    public byte[] transform(byte[] keyMaterial) {
        //Remove PEM wrapper if present
        Matcher matcher = PEM.matcher(new String(keyMaterial, StandardCharsets.UTF_8));
        if (matcher.find()) {
            keyMaterial = matcher.group(1).getBytes(StandardCharsets.UTF_8);
        }
        return keyMaterial;
    }

}
