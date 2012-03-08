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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Finds out scanning state for a given record.
 *
 * @author Jan Pokorsky
 */
public final class GetRecordStateQuery implements PreparedQuery<ResultSet> {
    private final int recordId;
    private DigitizationState state;

    public GetRecordStateQuery(int recordId) {
        this.recordId = recordId;
    }

    /**
     * Returns scanning state.
     * @return state or {@code null} if the record not found
     */
    public DigitizationState getState() {
        return state;
    }

    public void consumeQuery(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            String stateTxt = resultSet.getString("stavrec");
            state = DigitizationState.resolve(stateTxt);
        }
    }

    public PreparedStatement prepareStatement(Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "select stavrec from predloha where id=?");
        pstmt.setInt(1, recordId);
        return pstmt;
    }

    public Class<ResultSet> getQueryType() {
        return ResultSet.class;
    }

}
