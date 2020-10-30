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

package com.openexchange.coi.services.ratelimit;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.local.LocalBucket;

/**
 * {@link RateLimitInterceptor} is a implementation of {@link HandlerInterceptorAdapter} which does rate limiting
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@Component
public class RateLimitInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Autowired
    RateLimitConfig config;

    private Map<String, Optional<LocalBucket>> limiters = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (config == null || !config.isEnabled()) {
            return true;
        }

        String clientIp = getClientIP(request);
        Optional<LocalBucket> rateLimiter = getRateLimiter(clientIp);
        boolean allowRequest = rateLimiter.isPresent() && rateLimiter.get().tryConsume(1);

        if (!allowRequest) {
            LOG.debug("Client with ip {} exceeded the ratelimit of {} requests per {} seconds", clientIp, config.getLimit(), config.getPeriod());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        }
        response.addHeader("X-RateLimit-Limit", String.valueOf(config.getLimit()));
        return allowRequest;
    }

    /**
     * Gets the ip of the client
     *
     * @param req The {@link HttpServletRequest}
     * @return The ip
     */
    private String getClientIP(HttpServletRequest req) {
        String header = req.getHeader("X-Forwarded-For");
        return Strings.isNotBlank(header) ? header : req.getRemoteAddr();
    }

    /**
     * Gets the {@link LocalBucket} for the given ip
     *
     * @param clientIp The client ip
     * @return The {@link LocalBucket}
     */
    private Optional<LocalBucket> getRateLimiter(String clientIp) {
        return limiters.computeIfAbsent(clientIp, id -> {
            return Optional.of(createRateLimiter(id));
        });
    }

    /**
     * Create a new {@link LocalBucket} for the given ip
     *
     * @param clientIp The client ip
     * @return The {@link LocalBucket}
     */
    private LocalBucket createRateLimiter(String clientIp) {
        LOG.debug("Creating rate limiter for ip={}", clientIp);
        return Bucket4j.builder().addLimit(Bandwidth.simple(config.getLimit(), Duration.ofSeconds(config.getPeriod()))).build();
    }

}
