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

package com.openexchange.coi.services.push.storage.mysql.mapper;

import java.util.EnumMap;
import com.openexchange.coi.services.push.storage.PushResource;
import com.openexchange.coi.services.push.storage.PushResourceField;
import com.openexchange.coi.services.push.storage.mysql.entities.PushResourceImpl;
import com.openexchange.coi.services.util.mapper.DbMapping;
import com.openexchange.coi.services.util.mapper.DefaultDbMapper;
import com.openexchange.coi.services.util.mapper.mapping.BinaryMapping;
import com.openexchange.coi.services.util.mapper.mapping.BooleanMapping;
import com.openexchange.coi.services.util.mapper.mapping.DateMapping;
import com.openexchange.coi.services.util.mapper.mapping.VarCharMapping;

/**
 * 
 * {@link PushResourceMapper}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class PushResourceMapper extends DefaultDbMapper<PushResource, PushResourceField> {

    private static PushResourceMapper INSTANCE = new PushResourceMapper();

    /**
     * Gets the {@link PushResourceMapper} instance
     * 
     * @return The {@link PushResourceMapper}
     */
    public static PushResourceMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link PushResourceMapper}.
     */
    private PushResourceMapper() {
        super();
    }

    @Override
    protected EnumMap<PushResourceField, ? extends DbMapping<? extends Object, PushResource>> createMappings() {
        EnumMap<PushResourceField, DbMapping<? extends Object, PushResource>> mappings = new EnumMap<PushResourceField, DbMapping<? extends Object, PushResource>>(PushResourceField.class);

        // @formatter:off
        mappings.put(PushResourceField.id, new VarCharMapping<PushResource>("id", 
                                                                            "ID", 
                                                                            (res) -> res.getId() != null, 
                                                                            (res) -> res.getId(), 
                                                                            (res, value) -> res.setId(value)));
        
        mappings.put(PushResourceField.appid, new VarCharMapping<PushResource>("appid", 
                                                                               "App ID", 
                                                                               (res) -> res.getAppId() != null, 
                                                                               (res) -> res.getAppId(), 
                                                                               (res, value) -> res.setAppId(value)));
        
        mappings.put(PushResourceField.pushtoken, new VarCharMapping<PushResource>( "pushtoken", 
                                                                                    "Pushtoken", 
                                                                                    (res) -> res.getPushToken() != null, 
                                                                                    (res) -> res.getPushToken(), 
                                                                                    (res, value) -> res.setPushToken(value)));
        
        mappings.put(PushResourceField.transport, new VarCharMapping<PushResource>( "transport", 
                                                                                    "Transport", 
                                                                                    (res) -> res.getTransport() != null, 
                                                                                    (res) -> res.getTransport(), 
                                                                                    (res, value) -> res.setTransport(value)));
        
        mappings.put(PushResourceField.expireDate, new DateMapping<PushResource>( "expiredate", 
                                                                                  "Expire Date", 
                                                                                  (res) -> res.getExpireDate() != null, 
                                                                                  (res) -> res.getExpireDate(), 
                                                                                  (res, value) -> res.setExpireDate(value))); 
        
        mappings.put(PushResourceField.lastModified, new DateMapping<PushResource>( "lastModified", 
                                                                                    "Last modified date", 
                                                                                    (res) -> res.getLastModified() != null, 
                                                                                    (res) -> res.getLastModified(), 
                                                                                    (res, value) -> res.setLastModified(value))); 
        
        mappings.put(PushResourceField.validated, new BooleanMapping<PushResource>(  "validated", 
                                                                                     "Validated", 
                                                                                     (res) -> true, 
                                                                                     (res) -> res.isValid(), 
                                                                                     (res, value) -> res.setValid(value)));
        mappings.put(PushResourceField.publicKey, new BinaryMapping<PushResource>(  "publickey", 
                                                                                    "Public key", 
                                                                                    (res) -> true, 
                                                                                    (res) -> res.getPublicKey(), 
                                                                                    (res, value) -> res.setPublicKey(value)));
        
        // @formatter:on
        return mappings;
    }

    @Override
    protected PushResource newInstance() {
        return new PushResourceImpl();
    }

    @Override
    protected PushResourceField[] newArray(int size) {
        return new PushResourceField[size];
    }


}
