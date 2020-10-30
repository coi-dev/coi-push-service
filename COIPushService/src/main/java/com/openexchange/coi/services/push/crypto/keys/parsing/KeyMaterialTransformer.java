
package com.openexchange.coi.services.push.crypto.keys.parsing;

/**
 * 
 * {@link KeyMaterialTransformer} transforms key materia from one representation to another.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public interface KeyMaterialTransformer {

    /**
     * Transforms the given key material to another representation
     * 
     * @param keyMaterial The key material to transform
     * @return The transformed key material
     */
    byte[] transform(byte[] keyMaterial);
}
