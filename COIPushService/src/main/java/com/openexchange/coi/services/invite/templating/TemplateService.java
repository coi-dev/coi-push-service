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

package com.openexchange.coi.services.invite.templating;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import com.openexchange.coi.services.Profiles;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.exception.CoiServiceExceptionCodes;
import com.openexchange.coi.services.invite.InvitationConfiguration;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

/**
 * {@link TemplateService} processes invitations templates into invitation web pages
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@Service
@Profile(Profiles.INVITE)
public class TemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateService.class);

    @Autowired
    InvitationConfiguration config;

    private String templName;
    private Configuration cfg;

    private static final boolean ESCAPE_HTML = true;

    /**
     * Initializes the template engine
     *
     * @throws CoiServiceException
     */
    @PostConstruct
    public void init() throws CoiServiceException {
        cfg = new Configuration(new Version(2, 3, 28));
        String folderStr = config.getFolder();
        if (folderStr != null) {
            initCustomFolder(folderStr);
        } else {
            loadDefaultTemplate();
        }
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    /**
     * Loads the template from the given folder
     *
     * @param folderStr The folder path
     * @throws CoiServiceException in case the given path is invalid or doesn't contain a template
     */
    private void initCustomFolder(String folderStr) throws CoiServiceException {
        File folder = new File(folderStr);
        if (folder.exists() == false || folder.isDirectory() == false) {
            LOG.error("Invite template folder '{}' doesn't exist.", folder.getAbsolutePath());
            throw CoiServiceExceptionCodes.INVALID_CONFIGURATION.create();
        }
        try {
            cfg.setTemplateLoader(new FileTemplateLoader(folder));
        } catch (IOException e) {
            LOG.error("Unexpected error while loading template: " + e.getMessage(), e);
            throw CoiServiceExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        }
        File[] tmpl = folder.listFiles((dir, name) -> name.endsWith(".tmpl"));
        if (tmpl == null || tmpl.length == 0) {
            LOG.error("Unable to find invite template inside template folder.", folder.getAbsolutePath());
            throw CoiServiceExceptionCodes.INVALID_CONFIGURATION.create();
        }
        templName = tmpl[0].getName();
    }

    /**
     * Loads the default template
     */
    private void loadDefaultTemplate() {
        cfg.setClassForTemplateLoading(TemplateService.class, "/tmpl");
        templName = "invite.tmpl";
    }

    /**
     * Applies the given data to the template and returns it
     *
     * @param id The id of the invitations
     * @param name The senders name
     * @param email The senders email address
     * @param message The senders message
     * @param image The senders image
     * @return The invite web-page
     * @throws CoiServiceException In case an error happens during template processing
     */
    public String applyData(String id, String name, String email, String message, String image) throws CoiServiceException {
        Map<String, Object> input = new HashMap<String, Object>();
        put(input, "name", name);
        put(input, "email", email);
        put(input, "message", message);
        put(input, "google", config.getGoogleLink());
        put(input, "apple", config.getAppleLink());
        put(input, "start", config.getConversationPrefix() + id);
        if (image != null) {
            put(input, "image", image);
        } else {
            put(input, "image", "/images/fallback_contact.png");
        }
        try {
            Template template = cfg.getTemplate(this.templName);
            Writer writer = new StringWriter();
            template.process(input, writer);
            return writer.toString();
        } catch (TemplateException | IOException e) {
            LOG.error("Unexpected error: " + e.getMessage(), e);
            throw CoiServiceExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        }
    }

    /**
     * Adds the given value to the given map with the given key and also applies some sanitizing
     *
     * @param map The map
     * @param key The key
     * @param value The value to sanitize
     */
    private void put(Map<String, Object> map, String key, String value) {
        map.put(key, ESCAPE_HTML ? HtmlUtils.htmlEscape(value) : value);
    }


}
