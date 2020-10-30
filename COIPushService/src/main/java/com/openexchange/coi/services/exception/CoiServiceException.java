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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;

/**
 * {@link CoiServiceException}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class CoiServiceException extends Exception {

    private static final long serialVersionUID = -2619431493129268398L;

    private String code;
    private List<Object> args = new ArrayList<>();
    private Type type = Type.ERROR;

    /**
     * Initializes a new {@link CoiServiceException}.
     */
    public CoiServiceException(String code, String msg, Object... args) {
        super(args != null && args.length != 0 ? String.format(msg, args) : msg);
        this.code = code;
        this.args.addAll(Arrays.asList(args));
    }

    /**
     * Initializes a new {@link CoiServiceException}.
     */
    public CoiServiceException(Type type, String code, String msg, Object... args) {
        super(args != null && args.length != 0 ? String.format(msg, args) : msg);
        this.type = type == null ? Type.ERROR : type;
        this.code = code;
        this.args.addAll(Arrays.asList(args));
    }

    /**
     * Gets the exception {@link Type}
     *
     * @return The {@link Type}
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the code
     *
     * @return The code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Gets the args
     *
     * @return The args
     */
    public List<Object> getArgs() {
        return args;
    }

    /**
     * Logs this exception to the given logger.
     *
     * @param logger The logger to log to
     */
    public void log(Logger logger) {
        switch (getType().getLogLevel()) {
            case DEBUG:
                logger.debug(getMessage(), this);
                break;
            case ERROR:
                logger.error(getMessage(), this);
                break;
            case INFO:
                logger.info(getMessage(), this);
                break;
            case TRACE:
                logger.trace(getMessage(), this);
                break;
            case WARNING:
                logger.warn(getMessage(), this);
                break;
            default:
                logger.error(getMessage(), this);
                break;
        }
    }

    /**
     * {@link Type} is the exception type
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v1.0.0
     */
    public enum Type {
        /**
         * The default error type.
         */
        ERROR(LogLevel.ERROR),
        /**
         * The try again error type
         */
        TRY_AGAIN(LogLevel.DEBUG),
        /**
         * The user-input type
         */
        USER_INPUT(LogLevel.DEBUG),
        /**
         * The configuration type
         */
        CONFIGURATION(LogLevel.ERROR),
        /**
         * The connectivity type.
         */
        CONNECTIVITY(LogLevel.ERROR),
        /**
         * The service-down type.
         */
        SERVICE_DOWN(LogLevel.ERROR);

        private LogLevel loglevel;

        /**
         * Initializes a new {@link CoiServiceException.Type}.
         */
        private Type(LogLevel loglevel) {
            this.loglevel = loglevel;
        }

        /**
         * Gets the log level of this exception type
         *
         * @return The {@link LogLevel}
         */
        public LogLevel getLogLevel() {
            return loglevel;
        }

    }

    /**
     * 
     * {@link LogLevel}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v1.0.0
     */
    public enum LogLevel {
        /**
         * Trace log level.
         */
        TRACE,
        /**
         * Debug log level.
         */
        DEBUG,
        /**
         * Info log level.
         */
        INFO,
        /**
         * Warn log level.
         */
        WARNING,
        /**
         * Error log level.
         */
        ERROR;
    }

}
