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

package com.openexchange.coi.services.invite.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.openexchange.coi.services.Profiles;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.exception.CoiServiceExceptionCodes;
import com.openexchange.coi.services.invite.InvitationConfiguration;
import lombok.Getter;

/**
 * {@link InvitePageContentController} is a rest controller which provides images for the invite website
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@RestController
@RequestMapping("/images")
@Profile(Profiles.INVITE)
public class InvitePageContentController {

    private static final Logger LOG = LoggerFactory.getLogger(InvitePageContentController.class);

    @Autowired
    private InvitationConfiguration config;

    private Map<String, Image> data;

    /**
     * 
     * Initializes the {@link InvitePageContentController} by loading all images into the cache
     * 
     * @throws CoiServiceException
     */
    @PostConstruct
    public void init() throws CoiServiceException {
        String folderPath = config.getFolder();
        if (folderPath == null) {
            loadDefaultData();
            LOG.info("Missing folder for invite website. Falling back to default template.");
        } else {
            loadData(folderPath);
        }
    }

    /**
     * Loads the default template data
     * 
     * @throws CoiServiceException
     */
    private void loadDefaultData() throws CoiServiceException {
        Map<String, Image> data = new HashMap<>();
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource[] resources;
        try {
            resources = resolver.getResources("classpath*:/tmpl/*");
            for (Resource resource : resources) {
                if (isMediaFile(resource.getFilename()) == false) {
                    continue;
                }
                byte[] array = IOUtils.toByteArray(resource.getInputStream());
                String type = MediaType.IMAGE_PNG_VALUE;
                if (resource.getFilename().endsWith(".svg")) {
                    type = "image/svg+xml";
                }
                data.put(resource.getFilename(), new Image(type, array));
            }
        } catch (IOException e) {
            LOG.error("Unexpected error while loading default templates: " + e.getMessage(), e);
            throw CoiServiceExceptionCodes.UNEXPECTED_ERROR.create("Unexpected error while loading default templates: " + e.getMessage());
        }
        this.data = Collections.unmodifiableMap(data);
    }

    /**
     * Loads the template data in the given folder
     *
     * @param folderPath
     * @throws CoiServiceException
     */
    private void loadData(String folderPath) throws CoiServiceException {
        Map<String, Image> data = new HashMap<>();
        File folder = new File(folderPath);
        if (folder.exists() == false || folder.isDirectory() == false) {
            LOG.error("The provided path for the invite template folder is invalid");
            throw CoiServiceExceptionCodes.INVALID_CONFIGURATION.create();
        }
        try {
            for (File file : folder.listFiles((dir, fileName) -> isMediaFile(fileName))) {
                byte[] array = IOUtils.toByteArray(new FileInputStream(file));
                String type = MediaType.IMAGE_PNG_VALUE;
                if (file.getName().endsWith(".svg")) {
                    type = "image/svg+xml";
                }

                data.put(file.getName(), new Image(type, array));
            }
        } catch (IOException e) {
            LOG.error("Unexpected error while loading default templates: " + e.getMessage(), e);
            throw CoiServiceExceptionCodes.UNEXPECTED_ERROR.create("Unexpected error while loading default templates: " + e.getMessage());
        }
        this.data = Collections.unmodifiableMap(data);
    }

    /**
     * Checks if the file is a media file
     *
     * @param name The name of the file
     * @return <code>true</code> if it is a media file, <code>false</code> otherwise
     */
    private boolean isMediaFile(String name) {
        return name.endsWith(".png") || name.endsWith(".svg") || name.endsWith(".jpg");
    }

    /**
     * Gets the requested image and writes it to the output stream
     *
     * @param response The {@link HttpServletResponse}
     * @param name The name of the image
     * @throws IOException in case of errors
     */
    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public void getImageAsByteArray(HttpServletResponse response, @PathVariable("name") String name) throws IOException {
        Image image = data != null ? data.get(name) : null;
        if (image == null) {
            response.setStatus(404);
            return;
        }
        response.setContentType(image.getType());
        IOUtils.write(image.getData(), response.getOutputStream());
    }

    /**
     * {@link Image} is a wrapper class for image data
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v1.0.0
     */
    private static class Image {

        @Getter
        private String type;
        @Getter
        private byte[] data;

        /**
         * Initializes a new {@link InvitePageContentController.Image}.
         */
        public Image(String type, byte[] data) {
            super();
            this.type = type;
            this.data = data;
        }
    }

}
