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

package com.openexchange.coi.services.push.rest.util;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.coi.services.exception.CoiServiceExceptionCodes;

/**
 * {@link SizeLimitInputStream} limits the size of an push message.
 * 
 * When the size is exceeded an {@link IOException} is thrown which contains a {@link CoiServiceExceptionCodes#MAX_PUSH_SIZE_EXCEEDED} exception.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class SizeLimitInputStream extends InputStream {

    private final InputStream original;
    private final long maxSize;
    private long total;

    public SizeLimitInputStream(InputStream original, long maxSize) {
        this.original = original;
        this.maxSize = maxSize;
    }

    @Override
    public int read() throws IOException {
        int i = original.read();
        if (i >= 0)
            incrementCounter(1);
        return i;
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int i = original.read(b, off, len);
        if (i >= 0) {
            incrementCounter(i);
        }
        return i;
    }

    /**
     * 
     * Increments the internal counter and throws an exception if this counter exceeds the maximum allowed byte size
     *
     * @param size The size of bytes read
     * @throws IOException In case the maximum allowed size is exceeded. Contains a {@link CoiServiceExceptionCodes#MAX_PUSH_SIZE_EXCEEDED}.
     */
    private void incrementCounter(int size) throws IOException {
        total += size;
        if (total > maxSize) {
            throw new IOException("InputStream exceeded maximum size in bytes.", CoiServiceExceptionCodes.MAX_PUSH_SIZE_EXCEEDED.create(maxSize));
        }
    }

}
