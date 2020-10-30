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

package com.openexchange.coi.services.push;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openexchange.coi.services.Profiles;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.exception.CoiServiceExceptionCodes;
import com.openexchange.coi.services.push.storage.PushResource;
import com.openexchange.coi.services.push.storage.PushResourceStorage;
import com.openexchange.coi.services.push.storage.mysql.entities.PushResourceImpl;
import com.openexchange.coi.services.push.transport.PushTransport;
import com.openexchange.coi.services.push.transport.PushTransportRegistry;
import com.openexchange.coi.services.util.ErrorAwareBiConsumer;
import com.openexchange.coi.services.util.caching.CacheConfig;
import com.openexchange.coi.services.util.caching.CacheProvider;
import com.openexchange.coi.services.util.caching.DeactivatableCache;
import lombok.Getter;
import lombok.Setter;

/**
 * {@link PushService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@Service
@Profile(Profiles.PUSH)
@Configuration
@ConfigurationProperties(prefix = "com.openexchange.coi.services.cache")
public class PushService implements CacheProvider, CacheConfig {

    private static final String CACHE_KEY = "resource";

    @Getter
    @Setter
    private boolean enabled = false;

    @Getter
    @Setter
    private long max = 100000;

    @Getter
    @Setter
    private long expire = 60;

    private static final Logger LOG = LoggerFactory.getLogger(PushService.class);

    @Autowired
    private Environment env;

    @Autowired
    private PushTransportRegistry registry;

    @Autowired
    private PushResourceStorage resourceStorage;

    @Autowired(required = false)
    private List<PushServiceCallback> callbacks;

    /**
     * Sends the push message to the device
     *
     * @param resource The {@link PushResourceImpl}
     * @param message The message received from the COI server
     * @throws CoiServiceException In case of errors 
     */
    public void sendPushToDevice(PushResource resource, byte[] message) throws CoiServiceException {
        Optional<PushTransport> opt = registry.getTransportForName(resource.getTransport());
        if (opt.isPresent() == false) {
            throw CoiServiceExceptionCodes.INVALID_TRANSPORT.create(resource.getTransport());
        }
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending push message for {} resource with id {}", resource.isValid() ? "validated" : "un-validated", resource.getId());
            }
            opt.get().transport(resource.getPushToken(), resource.isValid(), message);
        } catch (CoiServiceException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to send push message for resource {} because {}", resource.getId(), e.getMessage());
            }
            if (CoiServiceExceptionCodes.INVALID_PUSH_TOKEN.equals(e)) {
                // The push token is invalid. Push resource must be removed and the client needs to re-register.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Push token for resource {} is invalid and is going to be removed.", resource.getId());
                }
                delete(resource);
            }
            throw e;
        }
    }

    /**
     * Finds a push resource by its id
     *
     * @param id The id of the {@link PushResourceImpl}
     * @return The {@link PushResourceImpl}
     * @throws CoiServiceException in case no {@link PushResourceImpl} with the given id exists
     */
    @Cacheable(value = CACHE_KEY, key = "#id")
    public PushResource findById(String id) throws CoiServiceException {
        Optional<PushResource> result = resourceStorage.getById(id);
        return result.orElseThrow(() -> CoiServiceExceptionCodes.MISSING_PUSH_RESOURCE.create(id));
    }

    /**
     * Saves the given push resource
     *
     * @param res The push resource to save
     * @return The saved push resource
     * @throws CoiServiceException
     */
    @CachePut(value = CACHE_KEY, key = "#result.getId()", unless = "#result == null")
    public PushResource save(PushResource res) throws CoiServiceException {
        runCallback(res, (calback, resource) -> calback.beforeSave(Optional.empty(), resource));
        return resourceStorage.save(res);
    }

    /**
     * Updates the given push resource
     *
     * @param res The push resource to save
     * @return The saved push resource
     * @throws CoiServiceException
     */
    @CachePut(value = CACHE_KEY, key = "#result.getId()", unless = "#result == null")
    public PushResource update(PushResource old, PushResource res) throws CoiServiceException {
        runCallback(res, (calback, resource) -> calback.beforeSave(Optional.of(old), resource));
        return resourceStorage.save(res);
    }

    /**
     * Runs callback functions
     *
     * @param res The {@link PushResource}
     * @param fn The {@link ErrorAwareBiConsumer} to run on all {@link PushServiceCallback}s
     * @throws CoiServiceException in case of errors
     */
    private void runCallback(PushResource res, ErrorAwareBiConsumer<PushServiceCallback, PushResource> fn) throws CoiServiceException {
        if (callbacks != null) {
            for (PushServiceCallback callback : callbacks) {
                if (callback.isAppplicable(res)) {
                    fn.accept(callback, res);
                }
            }
        }
    }

    /**
     * Deletes the given push resource
     *
     * @param res The push resource to delete
     * @throws CoiServiceException in case of errors
     */
    @CacheEvict(value = CACHE_KEY, key = "#res.getId()")
    public void delete(PushResource res) throws CoiServiceException {
        runCallback(res, (calback, resource) -> calback.beforeDelete(resource));
        resourceStorage.delete(res);
    }

    /**
     * Checks if a transport with the given name exists and if it is enabled.
     *
     * @param transport The name of the push transport
     * @return <code>true</code> if the push transport with the given name exists and if it is enabled, <code>false</code> otherwise
     */
    public boolean isAvailable(String transport) {
        Optional<PushTransport> opt = registry.getTransportForName(transport);
        return opt.isPresent() ? opt.get().isEnabled() : false;
    }

    /**
     * Returns a list of all {@link PushResource}s
     *
     * @return a list of all {@link PushResource}s
     * @throws CoiServiceException In case the method is not allowed
     */
    public List<PushResource> list(long from, long to) throws CoiServiceException {
        if (env.acceptsProfiles(org.springframework.core.env.Profiles.of(Profiles.TEST)) == false) {
            // Test profile is deactivated
            throw CoiServiceExceptionCodes.METHOD_NOT_SUPPORTED.create();
        }
        return resourceStorage.list(from, to);
    }

    @Override
    public CacheManager getCacheManager() {
        SimpleCacheManager result = new SimpleCacheManager();
        Cache<Object, Object> cache = Caffeine.newBuilder().maximumSize(max).expireAfterWrite(expire, TimeUnit.MINUTES).build();
        CaffeineCache caffeineCache = new CaffeineCache(CACHE_KEY, cache, false);
        result.setCaches(Collections.singleton(new DeactivatableCache(this, caffeineCache)));
        result.afterPropertiesSet();
        return result;
    }

}
