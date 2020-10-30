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

import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Before;
import com.openexchange.coi.services.testing.httpclient.invoker.ApiClient;
import com.openexchange.coi.services.testing.httpclient.models.PushResource;

/**
 * {@link AbstractTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class AbstractTest {

    protected ApiClient client;
    protected VAPIDUtil vapid;
    private static final Pattern AUDIENCE = Pattern.compile("http(s?):\\/\\/[\\w\\.\\_\\-]*");

    // Disable hostname verification for localhost testing
    static {
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

            @Override
            public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                if (hostname.equals("localhost")) {
                    return true;
                }
                return false;
            }
        });
    }

    @Before
    public void setup() throws Exception {
        vapid = new VAPIDUtil();
        client = new ApiClient();
        String host = System.getProperty("com.openexchange.test.host");
        if (host != null) {
            String basePath = "http://" + host;
            String port = System.getProperty("com.openexchange.test.port");
            if (port != null) {
                basePath += ":" + port;
            }
            client.setBasePath(basePath);
        }
    }

    /**
     * Adds a VAPID header for the given pushResource's audience
     * 
     * @param pushResource The PushResource containing the "audience claim" which will be included in the VAPID header
     */
    protected void setVapidHeader(PushResource pushResource) {
        String endpoint = pushResource.getEndpoint();
        Matcher matcher = AUDIENCE.matcher(endpoint);
        assertTrue("The given pushResource must contain a valid host pattern.", matcher.find());
        String audience = matcher.group(); //entire pattern
        setVapidHeader(audience);
    }

    /**
     * Adds a VAPID header for the given audience
     * 
     * @param audience The audience to add to include in the VAPID header
     */
    protected void setVapidHeader(String audience) {
        client.addDefaultHeader("Authorization", vapid.createVAPIDHeader(audience, new Date(new Date().getTime() + Duration.ofHours(24).toMillis()), "bla"));
    }
}
