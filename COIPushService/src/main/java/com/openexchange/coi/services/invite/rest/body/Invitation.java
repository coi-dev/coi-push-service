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

package com.openexchange.coi.services.invite.rest.body;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.openexchange.coi.services.invite.rest.validator.SizeByProprerty;
import com.openexchange.coi.services.invite.storage.InvitationEntity;
import com.openexchange.coi.services.invite.storage.mysql.entities.InvitationEntityImpl;
import lombok.Getter;
import lombok.Setter;

/**
 * {@link Invitation}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class Invitation {

    @NotNull(message = "Sender must not be null")
    @Valid
    @Getter
    @Setter
    private Sender sender;

    @SizeByProprerty(name = "getMaxMessageSize", message = "The message must not be longer than %1$s characters.")
    @Getter
    @Setter
    private String message;

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String endpoint;

    /**
     * Creates an {@link Invitation} from an {@link InvitationEntityImpl}
     *
     * @param entity The {@link InvitationEntityImpl}
     * @return The {@link Invitation}
     */
    public static Invitation create(InvitationEntity entity, String image, String endpoint) {
        Invitation invitation = new Invitation();
        invitation.setMessage(entity.getMessage());
        invitation.setId(entity.getId());
        Sender sender = invitation.new Sender();
        sender.setEmail(entity.getEmail());
        if (entity.getImage() != null) {
            sender.setImage(new String(entity.getImage()));
        }
        if (entity.getPublicKey() != null) {
            sender.setPublicKey(new String(entity.getPublicKey()));
        }
        if (image != null) {
            sender.setImage(image);
        }
        sender.setName(entity.getName());
        invitation.setSender(sender);
        if (endpoint != null) {
            invitation.setEndpoint(endpoint);
        }
        return invitation;
    }

    /**
     * {@link Sender} defines the sender of an invitation
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v1.0.0
     */
    public class Sender {

        @NotBlank(message = "name must not be null or empty")
        @Size(max = 256, message = "The name must not be longer than 256 characters.")
        @Getter
        @Setter
        private String name;

        @NotBlank(message = "email must not be null or empty")
        @Size(max = 256, message = "The email address must not be longer than 256 characters.")
        @Getter
        @Setter
        private String email;

        @SizeByProprerty(name = "getMaxImageSize", message = "The image must not exceed %1$s bytes.", bytes = true)
        @Pattern(regexp = "^[^\"]+$", message = "Image contains invalid characters")
        @Getter
        @Setter
        private String image;

        @Getter
        @Setter
        private String publicKey;

    }
}


