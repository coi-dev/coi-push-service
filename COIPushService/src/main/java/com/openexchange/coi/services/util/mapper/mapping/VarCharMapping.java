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

package com.openexchange.coi.services.util.mapper.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.function.BiConsumer;
import java.util.function.Function;
import com.openexchange.coi.services.exception.CoiServiceException;

/**
 * {@link VarCharMapping} - Database mapping for <code>Types.VARCHAR</code>.
 *
 * @param <O> the type of the object *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
public class VarCharMapping<O> extends DefaultDbMapping<String, O> {

    private Function<O, Boolean> isSetFunction;
    private Function<O, String> getFunction;
    private BiConsumer<O, String> setConsumer;

    /**
     * Initializes a new {@link VarCharMapping}.
     * 
     * @param columnName The name of the column
     * @param readableName A readable name
     * @param isSetFunction The function which check whether the property is set
     * @param getFunction The function which gets the property
     * @param setConsumer A consumer which sets the property
     */
    public VarCharMapping(final String columnName, final String readableName, Function<O, Boolean> isSetFunction, Function<O, String> getFunction, BiConsumer<O, String> setConsumer) {
        super(columnName, readableName, Types.VARCHAR);
        this.isSetFunction = isSetFunction;
        this.getFunction = getFunction;
        this.setConsumer = setConsumer;
    }

    @Override
    public String get(final ResultSet resultSet, String columnLabel) throws SQLException {
        return resultSet.getString(columnLabel);
    }

    @Override
    public boolean truncate(O object, int length) throws CoiServiceException {
        final String value = this.get(object);
        if (null != value && length < value.length()) {
            this.set(object, value.substring(0, length));
            return true;
        }
        return false;
    }

    @Override
    public boolean replaceAll(O object, String regex, String replacement) throws CoiServiceException {
        String value = get(object);
        if (null != value) {
            String replacedValue = value.replaceAll(regex, replacement);
            if (false == value.equals(replacedValue)) {
                set(object, replacedValue);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSet(O obj) {
        return isSetFunction.apply(obj);
    }

    @Override
    public void set(O obj, String value) throws CoiServiceException {
        setConsumer.accept(obj, value);
    }

    @Override
    public String get(O obj) {
        return getFunction.apply(obj);
    }

}
