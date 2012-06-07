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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Updates/Inserts items to URNNBN. The update is synchronized on given PREDLOHA.ID.
 *
 * @author Jan Pokorsky
 */
public final class UpdateUrnNbnQuery implements PreparedQuery<int[]> {

    private static final Logger LOG = Logger.getLogger(UpdateUrnNbnQuery.class.getName());
    private final int predlohaId;
    private final Set<String> urnNbns;
    private final Date allocDate;
    /** add ({@code true}) or set ({@code false}) urnNbns */
    private final boolean addItems;
    /** update result */
    private boolean updated;

    public UpdateUrnNbnQuery(int predlohaId, Set<String> urnNbns, java.util.Date allocDate, boolean addItems) {
        this.predlohaId = predlohaId;
        this.urnNbns = urnNbns;
        this.allocDate = (allocDate != null)
                ? new Date(allocDate.getTime()) : new Date(System.currentTimeMillis());
        this.addItems = addItems;

        // remove nulls
        for (Iterator<String> it = urnNbns.iterator(); it.hasNext();) {
            String u = it.next();
            if (u == null) {
                it.remove();
            }
        }
    }

    public boolean isUpdated() {
        return updated;
    }

    public void consumeQuery(int[] results) throws SQLException {
        for (int i = 0; i < results.length; i++) {
            int result = results[i];
            if (result < 1 && result != Statement.SUCCESS_NO_INFO) {
                throw new SQLException("batch item failed: " + i);
            }
        }
        updated = true;
    }

    public PreparedStatement prepareStatement(Connection conn) throws SQLException {
        if (!existRecord(conn, predlohaId)) {
            return null;
        }
        // reliefFlag signals to set RPredloha_URNNBNMF = 1 for exactly one urnNbn per given predlohaId
        int reliefFlag = 1;
        int urnNbnIdSequence = PlaantIdQuery.getId(conn, PlaantIdQuery.PLAANT_URNNBN_ID);
        if (addItems) {
            if (exludeUrnNbns(conn, predlohaId, urnNbns)) {
                reliefFlag = 0;
            }
        } else {
            deleteOldUrnNbns(conn, predlohaId);
        }
        String queryInsertUrnNbn = "INSERT INTO URNNBN (ID, RPredloha_URNNBN, urnnbn, awardDate, RPredloha_URNNBNMF) VALUES (?,?,?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(queryInsertUrnNbn);
        for (String urnNbn : urnNbns) {
            int column = 1;
            int id = ++urnNbnIdSequence;
            pstmt.setInt(column++, id);
            pstmt.setInt(column++, predlohaId);
            pstmt.setString(column++, urnNbn);
            pstmt.setDate(column++, allocDate);
            pstmt.setInt(column++, reliefFlag);
            logQuery(queryInsertUrnNbn, id, predlohaId, urnNbn, allocDate, reliefFlag);
            reliefFlag = 0;
            pstmt.addBatch();
        }
        PlaantIdQuery.updateId(conn, PlaantIdQuery.PLAANT_URNNBN_ID, urnNbnIdSequence);
        return pstmt;
    }

    public Class<int[]> getQueryType() {
        return int[].class;
    }

    static boolean exludeUrnNbns(Connection conn, int predlohaId, Set<String> items) throws SQLException {
        String query = "select urnnbn from URNNBN where RPredloha_URNNBN=?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        try {
            pstmt.setInt(1, predlohaId);
            logQuery(query, predlohaId);
            ResultSet rs = pstmt.executeQuery();
            try {
                boolean anyExclude = false;
                while (rs.next()) {
                    String urnNbn = rs.getString(1);
                    boolean removed = items.remove(urnNbn);
                    LOG.log(Level.FINE, "exclude: {0}, excluded: {1}", new Object[] {urnNbn, removed});
                    anyExclude = true;
                }
                return anyExclude;
            } finally {
                DbUtils.close(rs);
            }
        } finally {
            DbUtils.close(pstmt);
        }
    }

    static void deleteOldUrnNbns(Connection conn, int predlohaId) throws SQLException {
        String query = "DELETE FROM URNNBN WHERE RPredloha_URNNBN=?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        try {
            pstmt.setInt(1, predlohaId);
            logQuery(query, predlohaId);
            pstmt.executeUpdate();
        } finally {
            DbUtils.close(pstmt);
        }
    }

    static boolean existRecord(Connection conn, int recordId) throws SQLException {
        String query = "select id from predloha where id=?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        try {
            pstmt.setInt(1, recordId);
            ResultSet rs = pstmt.executeQuery();
            try {
                return rs.next();
            } finally {
                DbUtils.close(rs);
            }
        } finally {
            DbUtils.close(pstmt);
        }
    }

    static void logQuery(String query, Object... params) {
        LOG.fine(DbUtils.logQuery(query, params));
    }

}
