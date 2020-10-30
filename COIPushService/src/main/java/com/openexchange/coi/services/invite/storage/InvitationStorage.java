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

import java.util.List;
import java.util.Optional;
import com.openexchange.coi.services.exception.CoiServiceException;

/**
 * {@link InvitationStorage} defines a storage for invitations
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public interface InvitationStorage {

    /**
     * Saves the given invitation
     *
     * @param invitation The invitation to store
     * @return The stored invitation
     * @throws CoiServiceException in case of errors
     */
    InvitationEntity save(InvitationEntity invitation) throws CoiServiceException;

    /**
     * Deletes the given invitation
     *
     * @param invitation The invitation to delete
     * @throws CoiServiceException in case of errors
     */
    void delete(InvitationEntity invitation) throws CoiServiceException;

    /**
     * Finds the invitation with the given id
     *
     * @param id The id to get
     * @return An optional invitation
     * @throws CoiServiceException in case of errors
     */
    Optional<InvitationEntity> getById(String id) throws CoiServiceException;

    /**
     * Gets a list of stored invitations. NOTE: This is for testing purposes only
     *
     * @param from The lower limit
     * @param to The upper limit
     * @return A list of all stored invitations
     */
    List<InvitationEntity> list(Long from, Long to);

}
