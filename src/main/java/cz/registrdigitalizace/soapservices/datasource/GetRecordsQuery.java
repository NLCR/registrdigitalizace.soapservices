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
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public final class GetRecordsQuery implements PreparedQuery<ResultSet> {
    private static final Logger LOGGER = Logger.getLogger(GetRecordsQuery.class.getName());

    private final String ccnb;
    private final List<DigitizationRecord> records = new ArrayList<DigitizationRecord>();

    public GetRecordsQuery(String ccnb) {
        this.ccnb = ccnb;
    }

    public List<DigitizationRecord> getRecords() {
        return records;
    }

    private void addRecord(String barcode, String scanStateStr, String marcXmlStr) {
        LOGGER.fine(String.format("addRecord cCNB: %s, barcode: %s, scanState: %s, xml.length: %s\n",
                ccnb, barcode, scanStateStr, marcXmlStr.length()));
        LOGGER.finest(marcXmlStr);
        DigitizationRecord record = new DigitizationRecord();
        record.setBarcode(barcode);
        DigitizationState state = RegistryDataSource.resolveState(scanStateStr);
        record.setState(state);
        Source source = resloveSource(marcXmlStr);
        record.setDescriptor(source);

        records.add(record);
    }

    private static Source resloveSource(String marcXmlStr) {
        if (marcXmlStr == null || marcXmlStr.length() > 0) {
            marcXmlStr = RegistryDataSource.fixMarcXml(marcXmlStr);
            return new StreamSource(new StringReader(marcXmlStr));
        }
        return null;
    }

    public void consumeQuery(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String xml = resultSet.getString("xml");
            String carkod = resultSet.getString("carkod");
            String skenstav = resultSet.getString("skenstav");

            addRecord(carkod, skenstav, xml);
        }
    }

    public PreparedStatement prepareStatement(Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "select carkod, skenstav, xml from predloha where ccnb=?");
        pstmt.setString(1, ccnb);
        return pstmt;
    }

    public Class<ResultSet> getQueryType() {
        return ResultSet.class;
    }
}
