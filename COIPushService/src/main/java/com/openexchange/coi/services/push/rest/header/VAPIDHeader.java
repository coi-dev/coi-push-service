package com.openexchange.coi.services.push.rest.header;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link VAPIDHeader} extracts VAPID information from a HTTP Authentication Header
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public class VAPIDHeader {

	private static String FIELD_SEPARATOR = ","; 
	
	private final String header;
	
	//Regex for checking RFC 8292 compliant VAPID headers
	private final Pattern jwtTokenPattern = Pattern.compile("t=(.*)",Pattern.DOTALL);
	private final Pattern jwtKeyPattern = Pattern.compile("k=(.*)",Pattern.DOTALL);
	
	//Fallback Regex if a client only specifies the encoded JWT token in the auth-header
	private final Pattern fallbackPattern = Pattern.compile("Authorization:\\s.*\\s(.*)", Pattern.DOTALL);

	/**
	 * Initializes a new {@link VAPIDHeader} from a HTTP authentication Header
	 * 
	 * @param authenticationHeader The value of the HTTP authentication header
	 */
	public VAPIDHeader(String authenticationHeader) {
		this.header = Objects.requireNonNull(authenticationHeader);
	}
	
	/**
	 * Internal method to validate a parsed JWT
	 * 
	 * @param jwt The JWT
	 * @return The jwt if its valid, or null if not
	 */
	private String validate(String jwt) {
		int dotCount = jwt.length() - jwt.replace(".", "").length();
		return dotCount == 2 ? jwt : null;
	}

	/**
	 * Gets the JSON web token (JWT)
	 * 
	 * @return The JWT, or null if no JWT was found in the authentication header.
	 */
	public String getJWT() {
		
		final String[] fields = header.split(FIELD_SEPARATOR);
		for(String field : fields) {
			field = field.replace("\n", "");
			
			//Search the JWT "t=.."
			Matcher matcher = jwtTokenPattern.matcher(field);
			if(matcher.find()) {
				String jwt = matcher.group(1);
				return validate(jwt);
			}
			
			//As a fallback we assume that the whole header value represents the JWT
			if(fields.length == 1) {
				Matcher fallbackMather = fallbackPattern.matcher(field);
				if(fallbackMather.find()) {
					String jwt = fallbackMather.group(1);
					return validate(jwt);
				}
			}
		}

		return null;
	}
	
	/**
	 * Gets the public key provided in the VAPID header
	 * 
	 * @return The public key
	 */
	public String getKey() {
		final String[] fields = header.split(FIELD_SEPARATOR);
		for(String field : fields) {
			//Search the JWT "t=.."
			Matcher matcher = jwtKeyPattern.matcher(field);
			if(matcher.find()) {
				return matcher.group(1);
			}
		}
		return null;
	}
}