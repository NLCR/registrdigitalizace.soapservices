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
 * Updates scanning state.
 *
 * @author Jan Pokorsky
 */
public class UpdateScanningStateQuery implements PreparedQuery<Integer> {
    private static final Logger LOGGER = Logger.getLogger(UpdateScanningStateQuery.class.getName());

    private final String ccnb;
    private final String barcode;
    private final DigitizationState newState;
    /** old scanning state used to make optimistic synchronization */
    private final DigitizationState oldState;
    /** scanner operator */
    private final String scanUser;
    /** date of scanning */
    private Date scanDate;
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

    public UpdateScanningStateQuery(String ccnb, String barcode,
            DigitizationState newState, DigitizationState oldState,
            String user, java.util.Date date) {
        this.ccnb = ccnb;
        this.barcode = barcode;
        this.newState = newState;
        this.oldState = oldState;
        this.scanUser = user;
        this.editDate = new Date(System.currentTimeMillis());
        this.scanDate = (date == null && newState == DigitizationState.FINISHED)
                ? this.editDate : new Date(date.getTime());
    }

    public PreparedStatement prepareStatement(Connection conn) throws SQLException {
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
                "update predloha set skenstav=?, skendate=?, skenprac=?, edidate=?, ediuser=?"
                + " where ccnb=? and carkod=? and "
                + (oldState != DigitizationState.UNDEFINED ? "skenstav=?" : "skenstav is null");

        PreparedStatement pstmt = conn.prepareStatement(query);
        int col = 1;
        String newStateTxt = RegistryDataSource.resolveState(newState);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("column: %s, val: %s\n", col, newStateTxt));
        pstmt.setString(col++, newStateTxt);
        sb.append(String.format("column: %s, val: %s\n", col, scanDate));
        pstmt.setDate(col++, scanDate);
        sb.append(String.format("column: %s, val: %s\n", col, scanUser));
        pstmt.setString(col++, scanUser);
        sb.append(String.format("column: %s, val: %s\n", col, editDate));
        pstmt.setDate(col++, editDate);
        sb.append(String.format("column: %s, val: %s\n", col, editUser));
        pstmt.setString(col++, editUser);
        sb.append(String.format("column: %s, val: %s\n", col, ccnb));
        pstmt.setString(col++, ccnb);
        sb.append(String.format("column: %s, val: %s\n", col, barcode));
        pstmt.setString(col++, barcode);
        if (oldState != DigitizationState.UNDEFINED) {
            String oldStateTxt = RegistryDataSource.resolveState(oldState);
            sb.append(String.format("column: %s, val: %s\n", col, oldStateTxt));
            pstmt.setString(col++, oldStateTxt);
        }

        sb.append(query);
        LOGGER.fine(sb.toString());
        return pstmt;
    }

    private PreparedStatement preparePlainChangeStatement(Connection conn) throws SQLException {
        final String query = "update predloha set skenstav=?, edidate=?, ediuser=?"
                + " where ccnb=? and carkod=? and "
                + (oldState != DigitizationState.UNDEFINED ? "skenstav=?" : "skenstav is null");
        PreparedStatement pstmt = conn.prepareStatement(
                query);
        int col = 1;
        String newStateTxt = RegistryDataSource.resolveState(newState);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("column: %s, val: %s\n", col, newStateTxt));
        pstmt.setString(col++, newStateTxt);
        sb.append(String.format("column: %s, val: %s\n", col, editDate));
        pstmt.setDate(col++, editDate);
        sb.append(String.format("column: %s, val: %s\n", col, editUser));
        pstmt.setString(col++, editUser);
        sb.append(String.format("column: %s, val: %s\n", col, ccnb));
        pstmt.setString(col++, ccnb);
        sb.append(String.format("column: %s, val: %s\n", col, barcode));
        pstmt.setString(col++, barcode);
        if (oldState != DigitizationState.UNDEFINED) {
            String oldStateTxt = RegistryDataSource.resolveState(oldState);
            sb.append(String.format("column: %s, val: %s\n", col, oldStateTxt));
            pstmt.setString(col++, oldStateTxt);
        }
        return pstmt;
    }

    public void consumeQuery(Integer result) throws SQLException {
        updated = result > 0;
        LOGGER.fine(String.format(
                "update rowCount: %s, cCNB:%s, barcode:%s, state:%s",
                result, ccnb, barcode, newState));
    }

    public Class<Integer> getQueryType() {
        return Integer.class;
    }

}
