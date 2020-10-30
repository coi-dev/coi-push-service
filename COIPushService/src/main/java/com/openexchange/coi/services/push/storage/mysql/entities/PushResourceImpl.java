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

package com.openexchange.coi.services.push.storage.mysql.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import com.openexchange.coi.services.push.storage.PushResource;
import lombok.Getter;
import lombok.Setter;

/**
 * {@link PushResourceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */

@Entity
@Table(name = "pushresources")
public class PushResourceImpl implements Serializable, PushResource {

    private static final long serialVersionUID = 7852044927759966762L;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", length = 191, updatable = false, nullable = false)
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    @Column(name = "expiredate", nullable = true)
    private Date expireDate;

    @Getter
    @Setter
    @Column(name = "appid", length = 128, nullable = false)
    private String appId;

    @Getter
    @Setter
    @Column(name = "pushtoken", length = 256, nullable = false)
    private String pushToken;

    @Getter
    @Setter
    @Column(name = "lastmodified", nullable = false)
    private Date lastModified;

    @Getter
    @Setter
    @Column(name = "transport", length = 128, nullable = false)
    private String transport;

    @Getter
    @Setter
    @Lob
    @Column(name = "publickey", nullable = false)
    private byte[] publicKey;

    @Getter
    @Setter
    @Column(name = "validated", nullable = false)
    private boolean valid = false;

    /**
     * Initializes a new {@link PushResourceImpl}.
     */
    public PushResourceImpl() {
        super();
    }

    /**
     * Initializes a new {@link PushResourceImpl}.
     * 
     * @param res The {@link PushResource} to initialize this {@link PushResourceImpl} with.
     */
    public PushResourceImpl(PushResource res) {
        this.setId(res.getId());
        this.setAppId(res.getAppId());
        this.setExpireDate(res.getExpireDate());
        this.setLastModified(res.getLastModified());
        this.setPublicKey(res.getPublicKey());
        this.setPushToken(res.getPushToken());
        this.setTransport(res.getTransport());
        this.setValid(res.isValid());
    }

    @Override
    public PushResource clone() {
        PushResourceImpl result = new PushResourceImpl();
        result.setAppId(this.getAppId());
        result.setExpireDate(this.getExpireDate());
        result.setId(this.getId());
        result.setLastModified(this.getLastModified());
        result.setPublicKey(this.getPublicKey());
        result.setPushToken(this.getPushToken());
        result.setTransport(this.getTransport());
        result.setValid(this.isValid());
        return result;
    }

}
