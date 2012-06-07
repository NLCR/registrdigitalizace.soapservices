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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Allows to run SQL queries {@link PreparedQuery} for given data source.
 * For now there is the only one hard coded data source.
 *
 * @author Jan Pokorsky
 */
public final class RegistryDataSource {
    private static final Logger LOGGER = Logger.getLogger(RegistryDataSource.class.getName());

    public <T> void runQuery(PreparedQuery<T> query) throws DataSourceException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            connection = initConnection();
            connection.setAutoCommit(false);
            stmt = query.prepareStatement(connection);
            if (stmt == null) {
                connection.rollback();
                return ;
            }
            T result;
            if (query.getQueryType() == ResultSet.class) {
                resultSet = stmt.executeQuery();
                result = (T) resultSet;
            } else if (query.getQueryType() == int[].class) {
                result = (T) stmt.executeBatch();
            } else {
                result = (T) Integer.valueOf(stmt.executeUpdate());
            }
            query.consumeQuery(result);
            connection.commit();
        } catch (Throwable t) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
            throw new DataSourceException(t);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private Connection initConnection() throws NamingException, SQLException {
        SQLException sex = null;
        for (int i = 1; i <= 10; i++) {
            try {
                return initConnectionImpl();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "initConnection failed: {0}. attempt.", i);
                sex = ex;
            }
        }
        throw sex;
    }
    private Connection initConnectionImpl() throws NamingException, SQLException {
        DataSource dataSource = InitialContext.doLookup("java:/comp/env/jdbc/registrydb");
        Connection connection = dataSource.getConnection();
        return connection;
    }

    private static final String COLLECTION_TAG = "<collection";
    private static final String MARCXML_NAMESPACE_FIX =
            " xmlns='http://www.loc.gov/MARC21/slim'"
            + " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"
            + " xsi:schemaLocation='http://www.loc.gov/MARC21/slim"
            + " http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd'";

    /**
     * Tries to fix Marc XML document which may be without proper name space declaration.
     * @param xml document
     * @return fixed document or {@code null} in case the passed document is null
     */
    static String fixMarcXml(String xml) {
        int collectionIdx = xml.indexOf(COLLECTION_TAG);
        int namespaceIdx = collectionIdx + COLLECTION_TAG.length();
        if (collectionIdx < 0 || namespaceIdx > xml.length()) {
            return xml;
        }

        if (xml.charAt(namespaceIdx) == '>') {
            StringBuilder sb = new StringBuilder(xml.length() + MARCXML_NAMESPACE_FIX.length());
            sb.append(xml.substring(0, namespaceIdx));
            sb.append(MARCXML_NAMESPACE_FIX);
            sb.append(xml.substring(namespaceIdx));
            xml = sb.toString();
        }
        return xml;
    }

}
