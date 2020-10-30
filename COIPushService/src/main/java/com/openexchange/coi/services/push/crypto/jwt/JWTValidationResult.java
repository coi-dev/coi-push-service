package com.openexchange.coi.services.push.crypto.jwt;

/**
 * Represents a result of a JWT validation
 * 
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v1.0.0
 */
public class JWTValidationResult {

	/**
	 * The Type of the result
	 */
	public enum ResultType {
		SUCCESS, FAILURE;
	}

	private static final JWTValidationResult SUCCESS_RESULT = new JWTValidationResult(ResultType.SUCCESS);

	private ResultType resultType;
	private String message = "success";

	/**
	 * Gets a default Success result
	 * 
	 * @return The success result
	 */
	public static JWTValidationResult successResult() {
		return SUCCESS_RESULT;
	}

	/**
	 * Creates a failure result
	 * 
	 * @param message The failure message
	 * @return The new failure result
	 */
	public static JWTValidationResult failureResult(String message) {
		return new JWTValidationResult(ResultType.FAILURE, message);
	}

	/**
	 * Initializes a new {@link JWTValidationHandler}.
	 * 
	 * @param resultType The type of the result
	 */
	public JWTValidationResult(ResultType resultType) {
		this.resultType = resultType;
	}

	/**
	 * Initializes a new {@link JWTValidationHandler}.
	 * 
	 * @param resultType The type of the result
	 * @param message    An additional message
	 */
	public JWTValidationResult(ResultType resultType, String message) {
		this.resultType = resultType;
		this.message = message;
	}

	/**
	 * Gets the type of the result
	 * 
	 * @return The type of the result
	 */
	public ResultType getResultType() {
		return resultType;
	}

	/**
	 * Whether or not the result represents a successful result
	 * 
	 * @return True if the result represents a successful result, false otherwise
	 */
	public boolean isSuccess() {
		return resultType == ResultType.SUCCESS;
	}

	/**
	 * Whether or not the result represents a failure
	 * 
	 * @return True if the result represents a failure, false otherwise
	 */
	public boolean isFailure() {
		return !isSuccess();
	}

	/**
	 * Gets the result message
	 * 
	 * @return The result message
	 */
	public String getMessage() {
		return message;
	}
}
