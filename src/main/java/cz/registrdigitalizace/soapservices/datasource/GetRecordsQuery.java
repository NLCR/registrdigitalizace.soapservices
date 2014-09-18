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

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GetRecordsQuery implements PreparedQuery<ResultSet> {
    private static final Logger LOGGER = Logger.getLogger(GetRecordsQuery.class.getName());

    private final PlainQuery pquery;
    private final List<DigitizationRecord> records = new ArrayList<DigitizationRecord>();
    private final int maxResults;

    public GetRecordsQuery(PlainQuery pquery, int maxResults) {
        this.pquery = pquery;
        this.maxResults = maxResults;
    }

    public List<DigitizationRecord> getRecords() {
        return records;
    }

    private void addRecord(int recordId, String scanStateStr, String marcXmlStr) {
        int marcXmlStrLength = (marcXmlStr != null) ? marcXmlStr.length() : -1;
        LOGGER.fine(String.format("addRecord recordId: %s, scanState: %s, xml.length: %s\n",
                recordId, scanStateStr, marcXmlStrLength));
        LOGGER.finest(marcXmlStr);
        DigitizationRecord record = new DigitizationRecord();
        record.setRecordId(recordId);
        DigitizationState state = DigitizationState.resolve(scanStateStr);
        record.setState(state);
        Source source = resolveSource(marcXmlStr);
        record.setDescriptor(source);

        records.add(record);
    }

    private static Source resolveSource(String marcXmlStr) {
        if (marcXmlStr != null && marcXmlStr.length() > 0) {
            marcXmlStr = RegistryDataSource.fixMarcXml(marcXmlStr);
            return new StreamSource(new StringReader(marcXmlStr));
        }
        return null;
    }

    public void consumeQuery(ResultSet resultSet) throws SQLException {
        for (int i = 0; resultSet.next() && i < maxResults; i++) {
            String xml = resultSet.getString("xml");
            int id = resultSet.getInt("id");
            String stavrec = resultSet.getString("stavrec");

            addRecord(id, stavrec, xml);
        }
    }

    public PreparedStatement prepareStatement(Connection conn) throws SQLException {
        StringBuilder whereBuilder = new StringBuilder();
        addWhereStringExp(whereBuilder, "CARKOD", pquery.getBarcode());
        addWhereStringExp(whereBuilder, "CCNB", pquery.getCcnb());
        addWhereStringExp(whereBuilder, "ISBN", pquery.getIsbn());
        addWhereStringExp(whereBuilder, "ISSN", pquery.getIssn());
        addWhereStringExp(whereBuilder, "ROKVYD", pquery.getIssueDate());
        addWhereStringExp(whereBuilder, "SIGNATURA", pquery.getSignature());
        addWhereStringExp(whereBuilder, "NAZEV", pquery.getTitle());
        addWhereStringExp(whereBuilder, "ROCNIKPER", pquery.getVolume());
        addWhereStringExp(whereBuilder, "POLE001", pquery.getPole001());

        String query = "select id, stavrec, xml from predloha where " + whereBuilder.toString();
        LOGGER.fine(query);

        PreparedStatement pstmt = conn.prepareStatement(query);

        int column = 1;
        column = setStringParam(pstmt, column, pquery.getBarcode());
        column = setStringParam(pstmt, column, pquery.getCcnb());
        column = setStringParam(pstmt, column, pquery.getIsbn());
        column = setStringParam(pstmt, column, pquery.getIssn());
        column = setStringParam(pstmt, column, pquery.getIssueDate());
        column = setStringParam(pstmt, column, pquery.getSignature());
        column = setStringParam(pstmt, column, pquery.getTitle());
        column = setStringParam(pstmt, column, pquery.getVolume());
        column = setStringParam(pstmt, column, pquery.getPole001());
        return pstmt;
    }

    public Class<ResultSet> getQueryType() {
        return ResultSet.class;
    }

    private static void addWhereStringExp(StringBuilder sb, String name, String value) {
        if (value != null && value.length() > 0) {
            addWhereExp(sb, name + "=?");
        }
    }

    private static void addWhereExp(StringBuilder sb, String exp) {
        if (sb.length() > 0) {
            sb.append(" and ");
        }
        sb.append(exp);
    }

    private static int setStringParam(PreparedStatement ps, int col, String value) throws SQLException {
        if (value != null && value.length() > 0) {
            LOGGER.log(Level.FINE, "PreparedStatement.setString({0}, {1})", new Object[] {col, value});
            ps.setString(col++, value);
        }
        return col;
    }
}
