package com.openexchange.coi.services.testing;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Date;
import io.jsonwebtoken.Jwts;

/**
 * {@link VAPIDUtil} - Helper utility for creating VAPID headers
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public class VAPIDUtil {

	private static final String HEADER_TEMPLATE = "vapid t=TOKEN,k=KEY";
	private static final byte[] DER_HEADER = hexStringToByteArray("3059301306072a8648ce3d020106082a8648ce3d030107034200");
	private final KeyPair keyPair; 

	/**
	 * Initializes a new {@link VAPIDUtil}. Key generation takes place in the constructor.
	 * 
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 */
	public VAPIDUtil() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
		ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
		keyGen.initialize(ecSpec, new SecureRandom());
		keyPair = keyGen.generateKeyPair();
	}

	/**
	 * Helper method to decode hex-encoded byte data.
	 * 
	 * @param s The string to convert
	 * @return The hex decoded byte data
	 */
	private static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Creates a JWT 
	 * 
	 * @param audience The aud claim
	 * @param expires  The exp claim
	 * @param subject  The subject claim
	 * @return The signed JWT 
	 */
	private String createToken(String audience, Date expires, String subject) {
		return Jwts.builder()
			.setAudience(audience)
			.setExpiration(expires)
			.setSubject(subject)
			.signWith(getPrivateKey())
			.compact();
	}
	
	/**
	 * Returns the RAW representation X.962 of the public key (=> DER header
	 * removed)
	 * 
	 * @return The RAW representation of the public key (DER header removed),
	 *         base64-URL encoded
	 */
	private String getRawPublicKey() {
		byte[] encoded = getPublicKey().getEncoded();
		byte[] raw = new byte[encoded.length - DER_HEADER.length];
		System.arraycopy(encoded, DER_HEADER.length, raw, 0, encoded.length - DER_HEADER.length);
		return Base64.getUrlEncoder().encodeToString(raw);
	}

	/**
	 * Returns the base64 encoded public key useful for adding to a push
	 * registration request
	 * 
	 * @return The base64 encoded public key
	 */
	public String getEncodedPublicKey() {
		return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
	}
	
	
	/**
	 * Gets the PublicKey
	 * 
	 * @return The public key
	 */
	public PublicKey getPublicKey() {
		return keyPair.getPublic();
	}
	
	/**
	 * Gets the PrivateKey
	 * 
	 * @return The private key
	 */
	public PrivateKey getPrivateKey() {
		return keyPair.getPrivate();
	}
	
	/**
	 * Creates a new VAPID header
	 * 
	 * @param audience The aud claim to include in the VAPID JWT
	 * @param expires  The exp claim to include in the VAPID JWT
	 * @param subject  The subject claim to include in the VAPID JWT
	 * @return The VAPID header
	 */
	public String createVAPIDHeader(String audience, Date expires, String subject) {
		return HEADER_TEMPLATE
				.replaceAll("TOKEN", createToken(audience, expires, subject))
				.replaceAll("KEY", getRawPublicKey());
	}
}
