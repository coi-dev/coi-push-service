
package com.openexchange.coi.services.push.crypto.keys.parsing;

import java.util.Arrays;

/**
 * {@link KeyConverter} Helps to convert keys to different formats
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public final class KeyConverter {

    private static final byte[] DER_HEADER = hexStringToByteArray("3059301306072a8648ce3d020106082a8648ce3d030107034200");

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Converts a X.962 key into DER format by just adding a DER header in front.
     * 
     * @param key The X.962 key
     * @return The key with a preceded DER header
     */
    public static byte[] X962ToDER(byte[] key) {
        byte[] ret = new byte[DER_HEADER.length + key.length];
        System.arraycopy(DER_HEADER, 0, ret, 0, DER_HEADER.length);
        System.arraycopy(key, 0, ret, DER_HEADER.length, key.length);
        return ret;
    }

    /**
     * Returns whether or not the given key has a DER header
     * 
     * @param key The key
     * @return true, if the given contains a DER header, false otherwise
     */
    public static boolean hasDERHeader(byte[] key) {
        if (key.length >= DER_HEADER.length) {
            byte[] header = new byte[DER_HEADER.length];
            System.arraycopy(key, 0, header, 0, header.length);
            return Arrays.equals(DER_HEADER, header);
        }
        return false;
    }
}
