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

package com.openexchange.coi.services.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.rmi.server.UID;
import org.junit.Test;
import com.openexchange.coi.services.testing.httpclient.invoker.ApiException;
import com.openexchange.coi.services.testing.httpclient.models.PushResource;
import com.openexchange.coi.services.testing.httpclient.models.RegistrationBody;
import com.openexchange.coi.services.testing.httpclient.models.UpdateBody;
import com.openexchange.coi.services.testing.httpclient.modules.PushEndpointApi;
import com.openexchange.coi.services.testing.httpclient.modules.PushResourceApi;

/**
 * {@link PushTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class PushTest extends AbstractTest {

    /**
     * The PushResourceApiTest.java.
     */
    private static final String TEST_APP = "TestApp";

    private PushResourceApi api;

    private PushEndpointApi pushApi;

    @Override
    public void setup() throws Exception {
        super.setup();
        api = new PushResourceApi(client);
        pushApi = new PushEndpointApi(client);
    }

    @Test
    public void testRoundtrip() throws ApiException {
        // Create push resource
        RegistrationBody body = createBody();
        PushResource resource = api.registerPushResource(body);
        assertNotNull("Resource should not be null", resource);

        resource = api.getPushResource(resource.getId());
        assertNotNull("Resource should not be null", resource);

        // Extend expiry date
        PushResource updatedResource = api.updatePushResource(resource.getId(), null);
        // Expect no change because the push resource is not validated yet
        assertEquals("Expiry date unexpectingly changed.", updatedResource.getExpireDate(), resource.getExpireDate());

        // Send first push message to validate push resource
        setVapidHeader(resource);
        byte[] content = "A test message".getBytes();
        pushApi.sendPushMessage(resource.getId(), content);

        // Expiry date should be changed now 
        PushResource res = api.getPushResource(resource.getId());
        assertTrue("Expiry date didn't change.", res.getExpireDate() > resource.getExpireDate());

        // Updating the push resource should change the expiry date now
        updatedResource = api.updatePushResource(resource.getId(), null);
        assertTrue("Expiry date didn't change.", updatedResource.getExpireDate() > res.getExpireDate());

        // Change push token
        UpdateBody updateBody = new UpdateBody();
        String token2 = "tometoken";
        updateBody.setPushToken(token2);
        updatedResource = api.updatePushResource(resource.getId(), updateBody);
        assertEquals(token2, updatedResource.getPushToken());

        resource = api.getPushResource(resource.getId());
        assertNotNull("Resource should not be null", resource);
        assertEquals(token2, resource.getPushToken());

        // remove resource
        api.deletePushResource(resource.getId());
        try {
            api.getPushResourceWithHttpInfo(resource.getId());
            fail();
        } catch (ApiException e) {
            assertEquals("The push resource should not to be found", 404, e.getCode());
        }
    }

    private RegistrationBody createBody() {
        RegistrationBody body = new RegistrationBody();
        body.setAppId(TEST_APP);
        String token = new UID().toString();
        body.setPushToken(token);
        body.setTransport("test");
        body.setPublicKey(vapid.getEncodedPublicKey());
        return body;
    }

    @Test
    public void testPushEndpoint() throws ApiException {
        RegistrationBody body = createBody();
        PushResource resource = api.registerPushResource(body);
        assertNotNull("Resource should not be null", resource);
        setVapidHeader(resource);
        byte[] content = "A test message".getBytes();
        pushApi.sendPushMessage(resource.getId(), content);
    }

}
