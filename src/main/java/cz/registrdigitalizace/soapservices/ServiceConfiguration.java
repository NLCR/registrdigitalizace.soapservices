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
package cz.registrdigitalizace.soapservices;

import cz.registrdigitalizace.soapservices.datasource.DataSourceException;
import cz.registrdigitalizace.soapservices.datasource.DigitizationRegistryDao;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author Jan Pokorsky
 */
public class ServiceConfiguration implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        try {
            initDb();
        } catch (DataSourceException ex) {
            throw new IllegalStateException();
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }

    private void initDb() throws DataSourceException {
        DigitizationRegistryDao dao = new DigitizationRegistryDao();
        dao.init();
    }

}
