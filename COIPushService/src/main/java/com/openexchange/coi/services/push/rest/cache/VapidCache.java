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

package com.openexchange.coi.services.push.rest.cache;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openexchange.coi.services.util.caching.CacheConfig;
import com.openexchange.coi.services.util.caching.CacheProvider;
import com.openexchange.coi.services.util.caching.DeactivatableCache;
import lombok.Getter;
import lombok.Setter;

/**
 * {@link VapidCache}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@Service
@Configuration
@ConfigurationProperties(prefix = "com.openexchange.coi.services.push.vapid.cache")
public class VapidCache implements CacheProvider, CacheConfig {

    @Getter
    @Setter
    private boolean enabled = true;

    @Getter
    @Setter
    private long max = 10000;

    private static final String CACHE_KEY = "vapids";

    @Cacheable(value = CACHE_KEY, unless = "#result == null")
    public String getVapid(String hash) {
        return null;
    }

    @CachePut(value = CACHE_KEY, key = "#hash")
    public String putInCache(String hash, String jwt) {
        return jwt;
    }

    @Override
    public CacheManager getCacheManager() {
        SimpleCacheManager result = new SimpleCacheManager();
        Cache<Object, Object> cache = Caffeine.newBuilder().maximumSize(max).expireAfterWrite(24, TimeUnit.HOURS).build();
        CaffeineCache caffeineCache = new CaffeineCache(CACHE_KEY, cache, false);
        result.setCaches(Collections.singleton(new DeactivatableCache(this, caffeineCache)));
        result.afterPropertiesSet();
        return result;
    }

}
