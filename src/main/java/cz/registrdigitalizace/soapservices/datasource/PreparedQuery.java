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
import java.sql.SQLException;

public interface PreparedQuery<T> {

    void consumeQuery(T result) throws SQLException;

    /**
     * @return statement or {@code null} to rollback connection and exit
     */
    PreparedStatement prepareStatement(Connection conn) throws SQLException;

    /** accepted types are Integer, int[].class (DML) and ResultSet (selects) */
    Class<T> getQueryType();
}
