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

import cz.registrdigitalizace.soapservices.model.DigitizationRecord;
import cz.registrdigitalizace.soapservices.model.DigitizationState;
import cz.registrdigitalizace.soapservices.model.PlainQuery;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Jan Pokorsky
 */
public final class DigitizationRegistryDao {

    public final RegistryDataSource ds = new RegistryDataSource();

    public List<DigitizationRecord> findRecords(PlainQuery pquery, int maxResults) throws DataSourceException {
        GetRecordsQuery query = new GetRecordsQuery(pquery, maxResults);
        ds.runQuery(query);
        return query.getRecords();
    }

    public DigitizationState getRecordState(int recordId) throws DataSourceException {
        GetRecordStateQuery query = new GetRecordStateQuery(recordId);
        ds.runQuery(query);
        return query.getState();
    }

    public boolean updateRecordState(int recordId,
            DigitizationState newState, DigitizationState oldState,
            String user, Date date) throws DataSourceException {
        
        UpdateScanningStateQuery query = new UpdateScanningStateQuery(
                recordId, newState, oldState, user, date);
        ds.runQuery(query);
        return query.isUpdated();
    }

    public boolean setRecordUrnNbn(int recordId, Set<String> urnNbns, Date date) throws DataSourceException {
        UpdateUrnNbnQuery query = new UpdateUrnNbnQuery(recordId, urnNbns, date, false);
        ds.runQuery(query);
        return query.isUpdated();
    }

    public boolean addRecordUrnNbn(int recordId, Set<String> urnNbns, Date date) throws DataSourceException {
        UpdateUrnNbnQuery query = new UpdateUrnNbnQuery(recordId, urnNbns, date, true);
        ds.runQuery(query);
        return query.isUpdated();
    }

    public void init() throws DataSourceException {
        PlaantIdQuery query = new PlaantIdQuery();
        ds.runQuery(query);
    }
}
