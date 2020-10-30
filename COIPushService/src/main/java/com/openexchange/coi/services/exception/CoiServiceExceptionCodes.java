/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.coi.services.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import org.springframework.http.HttpStatus;
import com.openexchange.coi.services.exception.CoiServiceException.Type;

/**
 * {@link CoiServiceExceptionCodes} describes known exception codes of the push service
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public enum CoiServiceExceptionCodes {
    /**
     * The push transport %1$s is unknown
     */
    INVALID_TRANSPORT(Type.USER_INPUT, "The push transport %1$s is unknown.", "1", BAD_REQUEST),
    /**
     * The configuration is invalid. Please check the logs for further informations
     */
    INVALID_CONFIGURATION(Type.CONFIGURATION, "The configuration is invalid. Please check the logs for further informations", "2"),
    /**
     * The push token is invalid!
     */
    INVALID_PUSH_TOKEN("The push token is invalid!", "3"),
    /**
     * The property %1$s is missing!
     */
    MISSING_CONFIGURATION(Type.CONFIGURATION, "The property %1$s is missing!", "4"),
    /**
     * Can't send push message because of an exceeded quota. Please try again after %1$s seconds!
     */
    QUOTA_EXCEEDED(Type.TRY_AGAIN, "Can't send push message because of an exceeded quota. Please try again after %1$s seconds!", "5"),
    /**
     * Error while parsing request body.
     */
    INVALID_REQUEST_BODY(Type.USER_INPUT, "Missing or invalid request body.", "6", BAD_REQUEST),
    /**
     * A push resource with the id %1$s doesn't exist.
     */
    MISSING_PUSH_RESOURCE(Type.USER_INPUT, "A push resource with the id %1$s doesn't exist.", "7", HttpStatus.NOT_FOUND),
    /**
     * The field %1$s is missing.
     */
    MISSING_FIELD(Type.USER_INPUT, "The field %1$s is missing.", "8", BAD_REQUEST),
    /**
     * The VAPID header is missing
     */
    MISSING_VAPID_HEADER(Type.USER_INPUT, "The VAPID authorization header is missing.", "9", UNAUTHORIZED),
    /**
     * The VAPID authorization header does not contain a token.
     */
    MISSING_VAPID_JWT_TOKEN(Type.USER_INPUT, "The VAPID authorization header does not contain a token.", "10", UNAUTHORIZED),
    /**
     * The key is missing in the VAPID header
     */
    MISSING_VAPID_KEY(Type.USER_INPUT, "The VAPID authorization header does not contain a public key.", "11", UNAUTHORIZED),
    /**
     * The public key provided in the valid header does not match the registered VAPID key.
     */
    VAPID_KEY_MISMATCH(Type.USER_INPUT, "The provided public VAPID key does not match the registered public key.", "12", FORBIDDEN),
    /**
     * The VAPID signature could not be verified 
     */
    VAPID_SIGNATURE_NOT_VALID(Type.USER_INPUT, "The provided VAPID signature is not valid: %1$s", "13", FORBIDDEN),
    /**
     * Missing key or error while parsing the provided key
     */
    INVALID_PUBLIC_KEY(Type.USER_INPUT, "Missing or invalid public key", "14", BAD_REQUEST),
    /**
     * The push message exceeds the maximum allowed push size of %1$s bytes.
     */
    MAX_PUSH_SIZE_EXCEEDED(Type.USER_INPUT, "The push message exceeds the maximum allowed size of %1$s bytes.", "15", BAD_REQUEST),
    /**
     * An unexpected error happened: %1$s
     */
    UNEXPECTED_ERROR("An unexpected error happened: %1$s", "16", INTERNAL_SERVER_ERROR),
    /**
     * The method is not supported
     */
    METHOD_NOT_SUPPORTED("The method is not supported", "17", METHOD_NOT_ALLOWED),
    ;

    private String msg;
    private String code;
    private HttpStatus status;
    private Type type = null;

    /**
     * Initializes a new {@link CoiServiceExceptionCodes}.
     */
    private CoiServiceExceptionCodes(String msg, String code, HttpStatus status) {
        this.msg = msg;
        this.code = code;
        this.status = status;
    }

    /**
     * Initializes a new {@link CoiServiceExceptionCodes}.
     */
    private CoiServiceExceptionCodes(Type type, String msg, String code, HttpStatus status) {
        this.msg = msg;
        this.code = code;
        this.status = status;
        this.type = type;
    }

    /**
     * 
     * Initializes a new {@link CoiServiceExceptionCodes}.
     * 
     * @param msg The message
     * @param code The exception code
     */
    private CoiServiceExceptionCodes(String msg, String code) {
        this(msg, code, null);
    }

    /**
     * 
     * Initializes a new {@link CoiServiceExceptionCodes}.
     * 
     * @param msg The message
     * @param code The exception code
     */
    private CoiServiceExceptionCodes(Type type, String msg, String code) {
        this(type, msg, code, null);
    }

    /**
     * Creates a {@link CoiServiceException}
     *
     * @param args The arguments
     * @return The {@link CoiServiceException}
     */
    public CoiServiceException create(Object... args) {
        return status == null ? new CoiServiceException(type, code, msg, args) : new ResponseCodeAwareCoiServiceException(type, code, status, msg, args);
    }

    /**
     * Whether the given exception equals this {@link CoiServiceExceptionCodes}
     *
     * @param e The exception to check
     * @return <code>true</code> if it equals this {@link CoiServiceExceptionCodes}, <code>false</code> otherwise
     */
    public boolean equals(CoiServiceException e) {
        return e.getCode().equals(code);
    }
}
