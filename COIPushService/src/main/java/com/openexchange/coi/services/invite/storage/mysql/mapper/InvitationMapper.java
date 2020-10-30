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

package com.openexchange.coi.services.invite.storage.mysql.mapper;

import java.util.EnumMap;
import com.openexchange.coi.services.invite.storage.InvitationEntity;
import com.openexchange.coi.services.invite.storage.InvitationField;
import com.openexchange.coi.services.invite.storage.mysql.entities.InvitationEntityImpl;
import com.openexchange.coi.services.util.mapper.DbMapping;
import com.openexchange.coi.services.util.mapper.DefaultDbMapper;
import com.openexchange.coi.services.util.mapper.mapping.BinaryMapping;
import com.openexchange.coi.services.util.mapper.mapping.DateMapping;
import com.openexchange.coi.services.util.mapper.mapping.VarCharMapping;

/**
 * 
 * {@link InvitationMapper}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class InvitationMapper extends DefaultDbMapper<InvitationEntity, InvitationField> {

    private static InvitationMapper INSTANCE = new InvitationMapper();

    /**
     * Get the {@link InvitationMapper} instacne
     *
     * @return The {@link InvitationMapper}
     */
    public static InvitationMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link InvitationMapper}.
     */
    private InvitationMapper() {
        super();
    }

    @Override
    protected EnumMap<InvitationField, ? extends DbMapping<? extends Object, InvitationEntity>> createMappings() {
        EnumMap<InvitationField, DbMapping<? extends Object, InvitationEntity>> mappings = new EnumMap<InvitationField, DbMapping<? extends Object, InvitationEntity>>(InvitationField.class);

        // @formatter:off
        mappings.put(InvitationField.id, new VarCharMapping<InvitationEntity>(InvitationField.id.getColumnName(), 
                                                                              "ID", 
                                                                              (invitation) -> invitation.getId() != null, 
                                                                              (invitation) -> invitation.getId(), 
                                                                              (invitation, value) -> invitation.setId(value)));

        mappings.put(InvitationField.name, new VarCharMapping<InvitationEntity>(InvitationField.name.getColumnName(), 
                                                                                "Name", 
                                                                                (invitation) -> invitation.getName() != null, 
                                                                                (invitation) -> invitation.getName(), 
                                                                                (invitation, value) -> invitation.setName(value)));
        
        mappings.put(InvitationField.email, new VarCharMapping<InvitationEntity>(InvitationField.email.getColumnName(), 
                                                                                 "EMail", 
                                                                                 (invitation) -> invitation.getEmail() != null, 
                                                                                 (invitation) -> invitation.getEmail(), 
                                                                                 (invitation, value) -> invitation.setEmail(value)));
        
        mappings.put(InvitationField.message, new VarCharMapping<InvitationEntity>(InvitationField.message.getColumnName(), 
                                                                                   "Message", 
                                                                                   (invitation) -> invitation.getMessage() != null, 
                                                                                   (invitation) -> invitation.getMessage(), 
                                                                                   (invitation, value) -> invitation.setMessage(value)));
        
        mappings.put(InvitationField.expireDate, new DateMapping<InvitationEntity>(InvitationField.expireDate.getColumnName(), 
                                                                                   "Expire Date", 
                                                                                   (invitation) -> invitation.getExpireDate() != null, 
                                                                                   (invitation) -> invitation.getExpireDate(), 
                                                                                   (invitation, value) -> invitation.setExpireDate(value))); 
        
        mappings.put(InvitationField.publickey, new BinaryMapping<InvitationEntity>(InvitationField.publickey.getColumnName(), 
                                                                                    "Public key", 
                                                                                    (invitation) -> true, 
                                                                                    (invitation) -> invitation.getPublicKey(), 
                                                                                    (invitation, value) -> invitation.setPublicKey(value)));
        
        mappings.put(InvitationField.image, new BinaryMapping<InvitationEntity>(InvitationField.image.getColumnName(), 
                                                                                "Image", 
                                                                                (invitation) -> true, 
                                                                                (invitation) -> invitation.getImage(), 
                                                                                (invitation, value) -> invitation.setImage(value)));
        
        // @formatter:on
        return mappings;
    }

    @Override
    protected InvitationEntity newInstance() {
        return new InvitationEntityImpl();
    }

    @Override
    protected InvitationField[] newArray(int size) {
        return new InvitationField[size];
    }


}
