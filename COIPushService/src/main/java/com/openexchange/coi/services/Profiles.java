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

package com.openexchange.coi.services;

/**
 * {@link Profiles} is a list of available spring profile names
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public final class Profiles {

    /**
     * The profile for all cleanup tasks
     */
    public static final String CLEANUP = "cleanup";

    /**
     * The profile for a cleanup only server
     */
    public static final String CLEANUP_ONLY = "cleanup_only";

    /**
     * The profile for the mysql storage
     */
    public static final String MYSQL = "mysql";

    /**
     * The profile for the push service
     */
    public static final String PUSH = "push";

    /**
     * The profile for the invite service
     */
    public static final String INVITE = "invite";

    /**
     * The profile for documentation rest endpoints
     */
    public static final String DOCU = "docu";

    /**
     * The profile for testing classes
     */
    public static final String TEST = "test";

    /**
     * The profile for vault kubernetes authentication
     */
    public static final String VAULT = "vault";

    /**
     * The profile for additional health extensions
     */
    public static final String HEALTH = "health";

    /**
     * An array of default profiles
     */
    private static final String[] DEFAULT_PROFILES = new String[] { "default", PUSH, INVITE, MYSQL, CLEANUP, HEALTH };

    /**
     * Gets an array of default profiles
     *
     * @return The array of default profiles
     */
    public static String[] getDefaultProfiles() {
        return DEFAULT_PROFILES;
    }

    /**
     * Prevent initialization
     */
    private Profiles() {
        super();
    }
}
