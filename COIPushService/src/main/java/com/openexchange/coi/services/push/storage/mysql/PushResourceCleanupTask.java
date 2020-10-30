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

package com.openexchange.coi.services.push.storage.mysql;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.openexchange.coi.services.Profiles;

/**
 * {@link PushResourceCleanupTask} is a cleanup task for expired push resources
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@Component
@Profile({ Profiles.CLEANUP + " & " + Profiles.PUSH + " & " + Profiles.MYSQL, Profiles.CLEANUP_ONLY })
public class PushResourceCleanupTask {

    private static final Logger LOG = LoggerFactory.getLogger(PushResourceCleanupTask.class);

    @Autowired
    private MysqlPushResourceStorage storage;

    @Scheduled(fixedRateString = "${com.openexchange.coi.services.push.database.cleanup:3600000}")
    @Transactional
    public void cleanInvitations() {
        long start = System.currentTimeMillis();
        int numOfCLeanedUpResources = storage.cleanup(new Date());
        LOG.info("Push resource cleanup task deleted {} push resources in {} milliseconds.", numOfCLeanedUpResources, System.currentTimeMillis() - start);
    }

}
