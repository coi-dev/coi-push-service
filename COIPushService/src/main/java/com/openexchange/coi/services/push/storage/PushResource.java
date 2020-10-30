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

package com.openexchange.coi.services.push.storage;

import java.util.Date;
import com.openexchange.coi.services.push.transport.Transport;

/**
 * {@link PushResource}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public interface PushResource {

    /**
     * Gets the id of the push resource
     *
     * @return The id
     */
    public String getId();

    /**
     * Gets the expire date
     * 
     * @return The expire date
     */
    public Date getExpireDate();

    /**
     * Gets the app id
     *
     * @return The app id
     */
    public String getAppId();

    /**
     * Gets the push token
     *
     * @return The push token
     */
    public String getPushToken();

    /**
     * Gets the name of the transport to use for push transportation. See {@link Transport}.
     *
     * @return The Transport
     */
    public String getTransport();

    /**
     * Gets the last modified date
     *
     * @return The last modified date
     */
    public Date getLastModified();

    /**
     * Gets the public key of the coi server
     *
     * @return The public key
     */
    public byte[] getPublicKey();

    /**
     * Whether the push resource is already validated or not
     *
     * @return <code>true</code> if it is validated, <code>false</code> otherwise
     */
    public boolean isValid();

    /**
     * Sets the id
     *
     * @param id The id
     */
    public void setId(String id);

    /**
     * Sets the expire date
     *
     * @param expireDate The expire date
     */
    public void setExpireDate(Date expireDate);

    /**
     * Sets the app id
     *
     * @param appId The app id
     */
    public void setAppId(String appId);

    /**
     * Sets the push token
     *
     * @param pushToken The push token
     */
    public void setPushToken(String pushToken);

    /**
     * Sets the transport name. See {@link Transport} for valid names.
     *
     * @param transport The transport
     */
    public void setTransport(String transport);

    /**
     * Sets the last modified date
     *
     * @param lastModified The last modified date
     */
    public void setLastModified(Date lastModified);

    /**
     * Sets the public key
     *
     * @param publicKey The public key
     */
    public void setPublicKey(byte[] publicKey);

    /**
     * Sets the valid status
     *
     * @param isValid Whether the {@link PushResource} is validated or not
     */
    public void setValid(boolean isValid);

    /**
     * Clones this {@link PushResource}
     *
     * @return A clone of this push resource
     */
    public PushResource clone();

}
