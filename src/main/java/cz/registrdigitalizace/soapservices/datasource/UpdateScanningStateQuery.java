/*
 * Copyright (C) 2011 Jan Pokorsky
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

import cz.registrdigitalizace.soapservices.model.DigitizationState;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Updates digitization state.
 *
 * @author Jan Pokorsky
 */
public class UpdateScanningStateQuery implements PreparedQuery<Integer> {
    private static final Logger LOGGER = Logger.getLogger(UpdateScanningStateQuery.class.getName());

    private final int recordId;
    private final DigitizationState newState;
    /** old scanning state used to make optimistic synchronization */
    private final DigitizationState oldState;
    private DigitizationState currentState;
    /** digitization operator */
    private final String finishUser;
    /** date of finished digitization */
    private Date finishDate;
    /** hard coded user making the update */
    private final String editUser = "webservice";
    /** update date */
    private Date editDate;
    /** update result */
    private boolean updated = false;

    /**
     * Gets the query result.
     * @return {@code true} means successfully updated.
     */
    public boolean isUpdated() {
        return updated;
    }

    public UpdateScanningStateQuery(int recordId,
            DigitizationState newState, DigitizationState oldState,
            String user, java.util.Date date) {
        this.recordId = recordId;
        this.newState = newState;
        this.oldState = oldState;
        this.finishUser = user;
        this.editDate = new Date(System.currentTimeMillis());
        if (newState == DigitizationState.FINISHED) {
            this.finishDate = (date == null) ? this.editDate : new Date(date.getTime());
        } else {
            this.finishDate = null;
        }
    }

    public PreparedStatement prepareStatement(Connection conn) throws SQLException {
        // get current digitization state
        GetRecordStateQuery stateQuery = new GetRecordStateQuery(recordId);
        PreparedStatement stateStmt = stateQuery.prepareStatement(conn);
        try {
            stateQuery.consumeQuery(stateStmt.executeQuery());
            this.currentState = stateQuery.getState();
        } finally {
            stateStmt.close();
        }

        PreparedStatement pstmt;
        if (newState == DigitizationState.FINISHED) {
            pstmt = prepareFinishedStateStatement(conn);
        } else {
            pstmt = preparePlainChangeStatement(conn);
        }
        LOGGER.info(pstmt.toString());
        return pstmt;
    }

    private PreparedStatement prepareFinishedStateStatement(Connection conn) throws SQLException {
        final String query =
                "update predloha set stavrec=?, findate=?, finuser=?, edidate=?, ediuser=?"
                + " where id=?"
                // hack: concurrent modification => update nothing => stavrec='xxx'
                + (oldState != currentState ? " and stavrec='xxx'" : "");

        PreparedStatement pstmt = conn.prepareStatement(query);
        int col = 1;
        String newStateTxt = newState.getDbValue();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("column: %s, val: %s\n", col, newStateTxt));
        pstmt.setString(col++, newStateTxt);
        sb.append(String.format("column: %s, val: %s\n", col, finishDate));
        pstmt.setDate(col++, finishDate);
        sb.append(String.format("column: %s, val: %s\n", col, finishUser));
        pstmt.setString(col++, finishUser);
        sb.append(String.format("column: %s, val: %s\n", col, editDate));
        pstmt.setDate(col++, editDate);
        sb.append(String.format("column: %s, val: %s\n", col, editUser));
        pstmt.setString(col++, editUser);
        sb.append(String.format("column: %s, val: %s\n", col, recordId));
        pstmt.setInt(col++, recordId);

        sb.append(query);
        LOGGER.fine(sb.toString());
        return pstmt;
    }

    private PreparedStatement preparePlainChangeStatement(Connection conn) throws SQLException {
        final String query = "update predloha set stavrec=?, edidate=?, ediuser=?"
                + " where id=?"
                // hack: concurrent modification => update nothing => stavrec='xxx'
                + (oldState != currentState ? " and stavrec='xxx'" : "");
        
        PreparedStatement pstmt = conn.prepareStatement(
                query);
        int col = 1;
        String newStateTxt = newState.getDbValue();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("column: %s, val: %s\n", col, newStateTxt));
        pstmt.setString(col++, newStateTxt);
        sb.append(String.format("column: %s, val: %s\n", col, editDate));
        pstmt.setDate(col++, editDate);
        sb.append(String.format("column: %s, val: %s\n", col, editUser));
        pstmt.setString(col++, editUser);
        sb.append(String.format("column: %s, val: %s\n", col, recordId));
        pstmt.setInt(col++, recordId);

        sb.append(query);
        LOGGER.fine(sb.toString());
        return pstmt;
    }

    public void consumeQuery(Integer result) throws SQLException {
        updated = result > 0;
        LOGGER.fine(String.format(
                "update rowCount: %s, recordId:%s, state:%s",
                result, recordId, newState));
    }

    public Class<Integer> getQueryType() {
        return Integer.class;
    }

}
