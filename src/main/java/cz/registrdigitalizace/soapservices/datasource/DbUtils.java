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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jan Pokorsky
 */
final class DbUtils {

    private static final Logger LOG = Logger.getLogger(DbUtils.class.getName());

    static String logQuery(String query, Object... params) {
        String msg = String.format(query.replace("?", "%s"), params);
        return msg;
    }
    public static void close(Connection c) {
        close(c, null);
    }

    public static void close(Connection c, String msg) {
        try {
            c.close();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, msg, ex);
        }
    }

    public static void close(ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public static void close(Statement s) {
        try {
            s.close();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public static void rollback(Connection c) {
        rollback(c, null);
    }

    public static void rollback(Connection c, String msg) {
        try {
            c.close();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, msg, ex);
        }
    }

}
