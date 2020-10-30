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

package com.openexchange.coi.services.invite.storage.mysql;

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
import com.openexchange.coi.services.invite.storage.InvitationEntity;
import com.openexchange.coi.services.invite.storage.InvitationField;
import com.openexchange.coi.services.invite.storage.InvitationStorage;
import com.openexchange.coi.services.invite.storage.mysql.mapper.InvitationMapper;

/**
 * {@link MysqlInvitationStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v1.0.0
 */
@Service
@Profile(Profiles.INVITE + " & " + Profiles.MYSQL)
public class MysqlInvitationStorage implements InvitationStorage {

    private static final String SELECT = "SELECT * FROM invitations WHERE id=?";
    private static final String LIST = "SELECT * FROM invitations ORDER BY id LIMIT ?,?;";
    private static final String INSERT = "INSERT INTO invitations (";
    private static final String UPDATE = "UPDATE invitations SET ";
    private static final String DELETE = "DELETE FROM invitations WHERE id=?;";
    private static final String CLEAN = "DELETE FROM invitations WHERE expireDate<=?;";

    private static final InvitationMapper MAPPER = InvitationMapper.getInstance();

    @Autowired
    JdbcTemplate template;

    /**
     * Stores or updates the given {@link InvitationEntity}
     *
     * @param invitation The {@link InvitationEntity} to store
     * @return The stored {@link InvitationEntity}
     * @throws CoiServiceException
     */
    public InvitationEntity save(InvitationEntity invitation) throws CoiServiceException {
        InvitationField[] fields = MAPPER.getMappedFields();

        if (invitation.getId() == null) {
            invitation.setId(UUID.randomUUID().toString());

            StringBuilder sql = new StringBuilder(INSERT);
            sql.append(MAPPER.getColumns(fields));
            sql.append(") VALUES (").append(MAPPER.getParameters(fields)).append(");");

            return template.execute(sql.toString(), new PreparedStatementCallback<InvitationEntity>() {

                @Override
                public InvitationEntity doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                    try {
                        MAPPER.setParameters(ps, invitation, fields);
                        ps.execute();
                    } catch (CoiServiceException e) {
                        throw asDataAccessException(e, "Unable to set parameters.");
                    }
                    return invitation;
                }
            });
        } else {
            StringBuilder sql = new StringBuilder(UPDATE);
            sql.append(MAPPER.getAssignments(fields));
            sql.append(" WHERE id=?;");
            return template.execute(sql.toString(), new PreparedStatementCallback<InvitationEntity>() {

                @Override
                public InvitationEntity doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                    try {
                        int index = 1;
                        index = MAPPER.setParameters(ps, index, invitation, fields);
                        ps.setString(index, invitation.getId());
                        ps.execute();
                    } catch (CoiServiceException e) {
                        throw asDataAccessException(e, "Unable to set parameters.");
                    }
                    return invitation;
                }
            });
        }
    }

    /**
     * Deletes the given {@link InvitationEntity}
     *
     * @param invitation The {@link InvitationEntity} to delete
     */
    public void delete(InvitationEntity invitation) {
        template.execute(DELETE, new PreparedStatementCallback<Void>() {
            @Override
            public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setString(1, invitation.getId());
                ps.execute();
                return null;
            }
        });
    }

    /**
     * Gets the {@link InvitationEntity} by its id
     *
     * @param id The id of the {@link InvitationEntity}
     * @return An {@link Optional} {@link InvitationEntity}
     */
    public Optional<InvitationEntity> getById(String id) {
        return template.query(SELECT, (ps) -> ps.setString(1, id), new ResultSetExtractor<Optional<InvitationEntity>>() {

            @Override
            public Optional<InvitationEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
                try {
                    if (rs.next()) {
                        return Optional.of(toInvitationEntity(rs));
                    }
                    return Optional.empty();
                } catch (CoiServiceException e) {
                    throw asDataAccessException(e, "Unable to extract InvitationEntity from resultset");
                }
            }

        });
    }

    /**
     * Converts a {@link ResultSet} into a {@link InvitationEntity}
     *
     * @param rs The {@link ResultSet}
     * @return An {@link InvitationEntity} or null
     * @throws SQLException in case the {@link ResultSet} is already closed or doesn't contain {@link InvitationEntity} data
     * @throws CoiServiceException
     */
    private InvitationEntity toInvitationEntity(ResultSet rs) throws SQLException, CoiServiceException {
        return MAPPER.fromResultSet(rs, MAPPER.getMappedFields());
    }

    /**
     * Wraps the given exception in a {@link DataAccessException}
     *
     * @param e The exception
     * @param msg The message
     * @return The {@link DataAccessException}
     */
    private DataAccessException asDataAccessException(CoiServiceException e, String msg) {
        return new DataAccessException(msg, e) {
            private static final long serialVersionUID = 1L;
        };
    }

    /**
     * Removes all expired {@link InvitationEntity}s
     *
     * @param date The date to check
     * @return The number of deleted {@link InvitationEntity}s
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

    @Override
    public List<InvitationEntity> list(Long from, Long to) {
        return template.query(LIST, (ps) -> {
            ps.setLong(1, from);
            ps.setLong(2, to);
        }, new ResultSetExtractor<List<InvitationEntity>>() {

            @Override
            public List<InvitationEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<InvitationEntity> result = new ArrayList<>(rs.getFetchSize());
                while (rs.next()) {
                    try {
                        result.add(toInvitationEntity(rs));
                    } catch (CoiServiceException e) {
                        throw asDataAccessException(e, "Unable to extract InvitationEntity from resultset");
                    }
                }
                return result;
            }

        });
    }

}
