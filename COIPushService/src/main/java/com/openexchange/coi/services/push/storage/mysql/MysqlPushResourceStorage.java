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

package com.openexchange.coi.services.push.storage.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import com.openexchange.coi.services.Profiles;
import com.openexchange.coi.services.exception.CoiServiceException;
import com.openexchange.coi.services.push.storage.PushResource;
import com.openexchange.coi.services.push.storage.PushResourceField;
import com.openexchange.coi.services.push.storage.PushResourceStorage;
import com.openexchange.coi.services.push.storage.mysql.mapper.PushResourceMapper;

/**
 * {@link MysqlPushResourceStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@Service
@Profile(Profiles.PUSH + " & " + Profiles.MYSQL)
public class MysqlPushResourceStorage implements PushResourceStorage {

    private static final String SELECT = "SELECT * FROM pushresources WHERE id=?";
    private static final String LIST = "SELECT * FROM pushresources ORDER BY id LIMIT ?,?;";
    private static final String INSERT = "INSERT INTO pushresources (";
    private static final String UPDATE = "UPDATE pushresources SET ";
    private static final String DELETE = "DELETE FROM pushresources WHERE id=?;";
    private static final String CLEAN = "DELETE FROM pushresources WHERE expiredate<=?;";

    private static final PushResourceMapper MAPPER = PushResourceMapper.getInstance();

    @Autowired
    JdbcTemplate template;

    /**
     * Gets the {@link PushResource} by its id
     *
     * @param id The id of the push resource
     * @return The {@link PushResource} or null
     */
    public Optional<PushResource> getById(String id) {
        return Optional.ofNullable(template.query(SELECT, (ps) -> ps.setString(1, id), new ResultSetExtractor<PushResource>() {

            @Override
            public PushResource extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) {
                    try {
                        return toPushResource(rs);
                    } catch (CoiServiceException e) {
                        throw asDataAccessException(e, "Unable to extract PushResource from resultset");
                    }
                }
                return null;
            }
        }));
    }

    /**
     * Converts a {@link ResultSet} into a {@link PushResource}
     *
     * @param rs The {@link ResultSet}
     * @return The {@link PushResource}
     * @throws SQLException in case the {@link ResultSet} is already closed or doesn't contain {@link PushResource} data
     * @throws CoiServiceException In case the data couldn't be mapped
     */
    private PushResource toPushResource(ResultSet rs) throws SQLException, CoiServiceException {
        return MAPPER.fromResultSet(rs, MAPPER.getMappedFields());
    }

    /**
     * Stores or updates {@link PushResource}s
     *
     * @param resource The {@link PushResource} to store
     * @return The stored {@link PushResource}
     * @throws CoiServiceException If the {@link PushResource} couldn't be stored
     */
    public PushResource save(PushResource resource) throws CoiServiceException {
        PushResourceField[] fields = MAPPER.getMappedFields();

        if (resource.getId() == null) {
            resource.setId(UUID.randomUUID().toString());

            StringBuilder sql = new StringBuilder(INSERT);
            sql.append(MAPPER.getColumns(fields));
            sql.append(") VALUES (").append(MAPPER.getParameters(fields)).append(");");

            return template.execute(sql.toString(), new PreparedStatementCallback<PushResource>() {

                @Override
                public PushResource doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                    try {
                        MAPPER.setParameters(ps, resource, fields);
                        ps.execute();
                    } catch (CoiServiceException e) {
                        throw asDataAccessException(e, "Unable to set parameters.");
                    }
                    return resource;
                }
            });
        } else {
            StringBuilder sql = new StringBuilder(UPDATE);
            sql.append(MAPPER.getAssignments(fields));
            sql.append(" WHERE id=?;");
            return template.execute(sql.toString(), new PreparedStatementCallback<PushResource>() {

                @Override
                public PushResource doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                    try {
                        int index = 1;
                        index = MAPPER.setParameters(ps, index, resource, fields);
                        ps.setString(index, resource.getId());
                        ps.execute();
                    } catch (CoiServiceException e) {
                        throw asDataAccessException(e, "Unable to set parameters.");
                    }
                    return resource;
                }
            });
        }
    }

    /**
     * Deletes the given {@link PushResource}
     *
     * @param resource The {@link PushResource} to delete
     */
    public void delete(PushResource resource) {
        template.execute(DELETE, new PreparedStatementCallback<Void>() {

            @Override
            public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setString(1, resource.getId());
                ps.execute();
                return null;
            }
        });
    }

    /**
     * Removes {@link PushResource}s which are expired
     *
     * @param date The date to check
     * @return The number of removed {@link PushResource}s
     */
    public int cleanup(Date date) {
        return template.execute(CLEAN, new PreparedStatementCallback<Integer>() {

            @Override
            public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setTimestamp(1, new Timestamp(date.getTime()));
                return ps.executeUpdate();
            }
        });
    }

    /**
     * Wraps the given exception in an {@link DataAccessException}
     *
     * @param e The exception to wrap
     * @param message The message
     * @return The wrapped exception
     */
    private DataAccessException asDataAccessException(CoiServiceException e, String message) {
        return new DataAccessException(message, e) {
            private static final long serialVersionUID = 1L;
        };
    }

    @Override
    public List<PushResource> list(long from, long to) {
        return template.query(LIST, (ps) -> {
            ps.setLong(1, from);
            ps.setLong(2, to);
        }, new ResultSetExtractor<List<PushResource>>() {

            @Override
            public List<PushResource> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<PushResource> result = new ArrayList<>(rs.getFetchSize());
                while (rs.next()) {
                    try {
                        result.add(toPushResource(rs));
                    } catch (CoiServiceException e) {
                        throw asDataAccessException(e, "Unable to extract PushResource from resultset");
                    }
                }
                return result;
            }

        });
    }

}
