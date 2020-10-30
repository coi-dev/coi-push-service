
package com.openexchange.coi.services.push.crypto.keys.parsing;

/**
 * {@link DERHeaderTransformer} adds a DER header in front of the key material if not allready present.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public class DERHeaderTransformer implements KeyMaterialTransformer {

    @Override
    public byte[] transform(byte[] keyMaterial) {
        if (!KeyConverter.hasDERHeader(keyMaterial)) {
            // Assume we have a raw X962 key, so we add the DER header in front of it
            keyMaterial = KeyConverter.X962ToDER(keyMaterial);
        }
        return keyMaterial;
    }

}
