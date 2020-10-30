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

package com.openexchange.coi.services.invite.storage;

import java.util.Date;

/**
 * {@link InvitationEntity}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public interface InvitationEntity {

    /**
     * Gets the invitation id
     *
     * @return The invitation id
     */
    public String getId();

    /**
     * Gets the public key of the sender
     *
     * @return The public key of the sender
     */
    public byte[] getPublicKey();

    /**
     * Gets the expire date of this invitation
     *
     * @return The expire date
     */
    public Date getExpireDate();

    /**
     * Gets the name of the sender
     *
     * @return The senders name
     */
    public String getName();

    /**
     * Gets the email of the sender
     *
     * @return The email address of the sender
     */
    public String getEmail();

    /**
     * Gets the image of the sender
     *
     * @return The image of the sender
     */
    public byte[] getImage();

    /**
     * Gets the senders message
     *
     * @return The message of the sender
     */
    public String getMessage();

    /**
     * Sets the id of the invitation
     *
     * @param id The id to set
     */
    public void setId(String id);

    /**
     * Sets the public key of the sender
     *
     * @param publicKey The public key to set
     */
    public void setPublicKey(byte[] publicKey);

    /**
     * Sets the expire date of the invitation
     *
     * @param expireDate The expire date
     */
    public void setExpireDate(Date expireDate);

    /**
     * Sets the name of the sender
     *
     * @param name The name to set
     */
    public void setName(String name);

    /**
     * Sets the mail address of the sender
     *
     * @param email The mail address of the sender
     */
    public void setEmail(String email);

    /**
     * Sets the image of the sender
     *
     * @param image The image of the sender
     */
    public void setImage(byte[] image);

    /**
     * Sets the senders message
     *
     * @param message The message to set
     */
    public void setMessage(String message);

}