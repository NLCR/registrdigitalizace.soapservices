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
import java.util.Date;
import java.util.List;

/**
 *
 * @author Jan Pokorsky
 */
public final class DigitizationRegistryDao {

    public final RegistryDataSource ds = new RegistryDataSource();

    public List<DigitizationRecord> findRecords(String ccnb) throws DataSourceException {
        GetRecordsQuery query = new GetRecordsQuery(ccnb);
        ds.runQuery(query);
        return query.getRecords();
    }

    public DigitizationState getRecordState(String ccnb, String barcode) throws DataSourceException {
        GetRecordStateQuery query = new GetRecordStateQuery(ccnb, barcode);
        ds.runQuery(query);
        return query.getState();
    }

    public boolean updateRecordState(String ccnb, String barcode,
            DigitizationState newState, DigitizationState oldState,
            String user, Date date) throws DataSourceException {
        
        UpdateScanningStateQuery query = new UpdateScanningStateQuery(ccnb, barcode, newState, oldState, user, date);
        ds.runQuery(query);
        return query.isUpdated();
    }
}
