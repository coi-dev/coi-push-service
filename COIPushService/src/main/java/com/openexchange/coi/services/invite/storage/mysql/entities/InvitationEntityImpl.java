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

package com.openexchange.coi.services.invite.storage.mysql.entities;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import com.openexchange.coi.services.invite.storage.InvitationEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * {@link InvitationEntityImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@Entity
@Table(name = "invitations")
public class InvitationEntityImpl implements InvitationEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", length = 191, updatable = false, nullable = false)
    private String id;


    @Lob
    @Column(name = "publickey", nullable = true)
    @Getter
    @Setter
    private byte[] publicKey;

    @Getter
    @Setter
    @Column(name = "expiredate", nullable = false)
    private Date expireDate;

    @Setter
    @Getter
    @Column(name = "name", length = 256, nullable = false)
    private String name;

    @Setter
    @Getter
    @Column(name = "email", length = 256, nullable = false)
    private String email;

    @Setter
    @Getter
    @Lob
    @Column(name = "image", nullable = true)
    private byte[] image;

    @Setter
    @Getter
    @Column(name = "message", length = 1024, nullable = false)
    private String message;

    /**
     * Initializes a new {@link InvitationEntityImpl}.
     */
    public InvitationEntityImpl() {
        super();
    }

    /**
     * Initializes a new {@link InvitationEntityImpl}.
     * 
     * @param entity
     */
    public InvitationEntityImpl(InvitationEntity entity) {
        super();
        this.setId(entity.getId());
        this.setEmail(entity.getEmail());
        this.setExpireDate(entity.getExpireDate());
        this.setImage(entity.getImage());
        this.setMessage(entity.getMessage());
        this.setName(entity.getName());
        this.setPublicKey(entity.getPublicKey());
    }

    /**
     * Gets the id
     *
     * @return The id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the id
     *
     * @param id The id to set
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

}
