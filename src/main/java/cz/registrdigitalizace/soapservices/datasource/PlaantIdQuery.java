/*
 * Copyright (C) 2012 Jan Pokorsky
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.registrdigitalizace.soapservices.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * ID support for PLAANT system.
 *
 * @author Jan Pokorsky
 */
public class PlaantIdQuery implements PreparedQuery<Integer> {

    private static final Logger LOG = Logger.getLogger(PlaantIdQuery.class.getName());

    static final String PLAANT_URNNBN_ID = "cz.incad.rd.URNNBN";

    public PlaantIdQuery() {
    }

    public void consumeQuery(Integer result) throws SQLException {
        if (result == null || result != 1) {
            throw new IllegalStateException(PLAANT_URNNBN_ID);
        }
    }

    public PreparedStatement prepareStatement(Connection conn) throws SQLException {
        Integer existing = getId(conn, PLAANT_URNNBN_ID);
        if (existing != null) {
            return null;
        }
        String query = "insert into PLAANT_IDS (ID, DESKNAME) values (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, 0);
        stmt.setString(2, PLAANT_URNNBN_ID);
        logQuery(query, 0, PLAANT_URNNBN_ID);
        return stmt;
    }

    public Class<Integer> getQueryType() {
        return Integer.class;
    }

    static Integer getId(Connection conn, String deskname) throws SQLException {
        String query = "select ID from PLAANT_IDS where DESKNAME=? for update";
        PreparedStatement stmt = conn.prepareStatement(query);
        try {
            stmt.setString(1, deskname);
            logQuery(query, deskname);
            ResultSet rs = stmt.executeQuery();
            try {
                return rs.next() ? rs.getInt(1) : null;
            } finally {
                DbUtils.close(rs);
            }
        } finally {
            DbUtils.close(stmt);
        }
    }

    static void updateId(Connection conn, String deskname, int id) throws SQLException {
        String query = "update PLAANT_IDS set ID = ? where DESKNAME = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        try {
            stmt.setInt(1, id);
            stmt.setString(2, deskname);
            logQuery(query, deskname, id);
            int result = stmt.executeUpdate();
            if (result != 1) {
                throw new IllegalStateException(DbUtils.logQuery(query, deskname, id));
            }
        } finally {
            DbUtils.close(stmt);
        }
    }

    static void logQuery(String query, Object... params) {
        LOG.fine(DbUtils.logQuery(query, params));
    }

}
