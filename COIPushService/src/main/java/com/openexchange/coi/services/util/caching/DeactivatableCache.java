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

package com.openexchange.coi.services.util.caching;

import java.util.concurrent.Callable;
import org.springframework.cache.Cache;
import org.springframework.cache.support.NoOpCache;

/**
 * {@link DeactivatableCache} is a cache implementation which can be switched off.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class DeactivatableCache implements Cache {

    private final Cache delegate;
    private final NoOpCache noOpCache;
    private final CacheConfig cacheConfig;

    /**
     * Initializes a new {@link DeactivatableCache}.
     * 
     * @param cacheConfig The {@link CacheConfig}
     * @param delegate The {@link Cache}
     */
    public DeactivatableCache(CacheConfig cacheConfig, Cache delegate) {
        this.delegate = delegate;
        this.cacheConfig = cacheConfig;
        this.noOpCache = new NoOpCache(delegate.getName());
    }

    @Override
    public ValueWrapper get(Object key) {
        if (isCacheDisabled()) {
            return noOpCache.get(key);
        }

        return delegate.get(key);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        if (isCacheDisabled()) {
            return noOpCache.get(key, type);
        }

        return delegate.get(key, type);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        if (isCacheDisabled()) {
            return noOpCache.get(key, valueLoader);
        }

        return delegate.get(key, valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        if (isCacheDisabled()) {
            noOpCache.put(key, value);
            return;
        }

        delegate.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        if (isCacheDisabled()) {
            return noOpCache.putIfAbsent(key, value);
        }

        return delegate.putIfAbsent(key, value);
    }

    @Override
    public void evict(Object key) {
        if (isCacheDisabled()) {
            noOpCache.evict(key);
            return;
        }

        delegate.evict(key);
    }

    @Override
    public void clear() {
        if (isCacheDisabled()) {
            noOpCache.clear();
            return;
        }

        delegate.clear();
    }

    private boolean isCacheDisabled() {
        return cacheConfig.isEnabled() == false;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

}