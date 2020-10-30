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

package com.openexchange.coi.services.invite;

import static java.lang.System.currentTimeMillis;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.openexchange.coi.services.EndpointService;
import com.openexchange.coi.services.Profiles;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.exception.CoiServiceExceptionCodes;
import com.openexchange.coi.services.invite.rest.body.Invitation;
import com.openexchange.coi.services.invite.storage.DefaultInvitationEntity;
import com.openexchange.coi.services.invite.storage.InvitationEntity;
import com.openexchange.coi.services.invite.storage.InvitationStorage;
import com.openexchange.coi.services.invite.storage.mysql.entities.InvitationEntityImpl;

/**
 * {@link InviteService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@Service
@Profile(Profiles.INVITE)
public class InviteService {

    private static final String INVITE_URI_PATH = "/invite/website/";

    @Autowired
    Environment env;

    @Autowired
    InvitationStorage repo;

    @Autowired
    InvitationConfiguration config;

    @Autowired
    private EndpointService endpointService;

    /**
     * Stores the given invitation
     *
     * @param invite The invitation to store
     * @return The stored invitation
     * @throws CoiServiceException in case of errors
     */
    public Invitation storeInvite(Invitation invite) throws CoiServiceException {

        InvitationEntity entity = new DefaultInvitationEntity();
        entity.setEmail(invite.getSender().getEmail());
        entity.setName(invite.getSender().getName());
        entity.setMessage(invite.getMessage());
        entity.setExpireDate(new Date(currentTimeMillis() + config.getTtl()));
        entity.setImage(invite.getSender().getImage() != null ? invite.getSender().getImage().getBytes(StandardCharsets.UTF_8) : null);
        InvitationEntity result = repo.save(entity);
        invite.setId(result.getId());
        invite.setEndpoint(createEntpoint(result.getId()));
        return invite;
    }

    /**
     * Gets the invitation with the given id
     *
     * @param id The id of the invitation
     * @return The invitation
     * @throws CoiServiceException in case no invitation with this id exist
     */
    public Invitation getInvite(String id) throws CoiServiceException {
        InvitationEntity entity = repo.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No invitation with this id found"));
        byte[] image = entity.getImage();
        return Invitation.create(entity, image != null ? new String(image) : null, createEntpoint(id));
    }

    /**
     * Removes the invitation with the given id
     *
     * @param id The invitation id
     * @throws CoiServiceException in case no invitation with id exist
     */
    public void removeInvitation(String id) throws CoiServiceException {
        InvitationEntity entity = repo.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No invitation with this id found"));
        repo.delete(entity);
    }

    /**
     * Returns all invitations. This is for testing purposes only!!!
     *
     * @return A list of all invitations
     * @throws CoiServiceException
     */
    public List<Invitation> list(Long from, Long to) throws CoiServiceException {
        if (env.acceptsProfiles(org.springframework.core.env.Profiles.of(Profiles.TEST)) == false) {
            // Test profile is deactivated
            throw CoiServiceExceptionCodes.METHOD_NOT_SUPPORTED.create();
        }
        List<InvitationEntity> entities = repo.list(from, to);
        List<Invitation> result = new ArrayList<>(entities.size());
        entities.forEach((invite) -> result.add(entity2Invitation(invite)));
        return result;
    }

    /**
     * Converts an {@link InvitationEntityImpl} to an {@link Invitation}
     *
     * @param entity The {@link InvitationEntityImpl} to convert
     * @return The {@link Invitation}
     */
    private Invitation entity2Invitation(InvitationEntity entity) {
        return Invitation.create(entity, null, createEntpoint(entity.getId()));
    }

    /**
     * Creates an endpoint from the host and the invitation id
     *
     * @param uid The invitation id
     * @return The endpoint
     */
    private String createEntpoint(String uid) {
        return endpointService.getEndpoint(INVITE_URI_PATH + uid).toString();
    }

}
