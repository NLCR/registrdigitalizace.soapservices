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
package cz.registrdigitalizace.soapservices;

import cz.registrdigitalizace.soapservices.datasource.DataSourceException;
import cz.registrdigitalizace.soapservices.datasource.DigitizationRegistryDao;
import cz.registrdigitalizace.soapservices.model.DigitizationRecord;
import cz.registrdigitalizace.soapservices.model.DigitizationState;
import cz.registrdigitalizace.soapservices.model.RecordFormat;
import cz.registrdigitalizace.soapservices.transform.MarcTransformer;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

/**
 * SOAP web service makes available <a href="http://registrdigitalizace.cz">
 * Digitization Registry CZ</a> content.
 *
 * @author Jan Pokorsky
 */
@WebService
public class DigitizationRegistry {

    /**
     * Returns list of cCNB records in given format.
     *
     * @param ccnb cCNB
     * @param format format of record descriptor. It may be {@code null}
     *              for default {@link RecordFormat#MARC_XML Marc XML}.
     *
     * @return list of existing digitization records.
     * @throws DigitizationRegistryException in case of illegal parameters or some internal error.
     */
    @WebMethod(operationName = "getRecords")
    public List<DigitizationRecord> getRecords(
            @WebParam(name = "cCNB")
            String ccnb,
            @WebParam(name = "format")
            RecordFormat format) throws DigitizationRegistryException {
        
        StringBuilder failureMsg = new StringBuilder();
        checkNotNullNotEmptyParam("cCNB", ccnb, failureMsg);
        if (format == null) {
            format = RecordFormat.MARC_XML;
        }

        if (failureMsg.length() > 0) {
            throw new DigitizationRegistryException(failureMsg.toString());
        }

        try {
            DigitizationRegistryDao dao = new DigitizationRegistryDao();
            List<DigitizationRecord> records = dao.findRecords(ccnb);

            MarcTransformer transormer = new MarcTransformer();
            for (DigitizationRecord record : records) {
                Source source = record.getDescriptor();
                source = transormer.transform(source, format);
                record.setDescriptor(source);
            }
            return records;
        } catch (DataSourceException ex) {
            Logger.getLogger(DigitizationRegistry.class.getName()).log(Level.SEVERE, null, ex);
            throw DigitizationRegistryException.internalServiceError();
        } catch (TransformerException ex) {
            Logger.getLogger(DigitizationRegistry.class.getName()).log(Level.SEVERE, null, ex);
            throw DigitizationRegistryException.internalServiceError();
        }
    }

    /**
     * Gets scanning state for given cCNB and bar code.
     * @param ccnb cCNB
     * @param barcode bar code
     * @return the scanning state or {@code null} iff no record exists.
     * @throws DigitizationRegistryException in case of illegal parameters or some internal error.
     */
    @WebMethod(operationName = "getRecordState")
    public DigitizationState getRecordState(
            @WebParam(name = "cCNB")
            String ccnb,
            @WebParam(name = "barcode")
            String barcode) throws DigitizationRegistryException {

        StringBuilder failureMsg = new StringBuilder();
        checkNotNullNotEmptyParam("cCNB", ccnb, failureMsg);
        checkNotNullNotEmptyParam("barcode", barcode, failureMsg);
        if (failureMsg.length() > 0) {
            throw new DigitizationRegistryException(failureMsg.toString());
        }

        try {
            DigitizationRegistryDao dao = new DigitizationRegistryDao();
            return dao.getRecordState(ccnb, barcode);
        } catch (DataSourceException ex) {
            Logger.getLogger(DigitizationRegistry.class.getName()).log(Level.SEVERE, null, ex);
            throw DigitizationRegistryException.internalServiceError();
        }
    }

    /**
     * Updates scanning state for given  cCNB and bar code.
     * @param ccnb cCNB
     * @param barcode bar code
     * @param newState new scanning state
     * @param oldState old scanning state. Use
     *          {@link #getRecordState(java.lang.String, java.lang.String) getRecordState}
     *          or {@link #getRecords(java.lang.String, cz.registrdigitalizace.soapservices.model.RecordFormat) getRecords}
     * @param user scanner operator
     * @param date scanning date. In case it is {@code null} the present date is used.
     * @return {@code true} when the update passes or {@code false} if there is no such
     *          record or the oldState has been modified by someone else in the meantime.
     * @throws DigitizationRegistryException in case of illegal parameters or some internal error.
     */
    @WebMethod(operationName = "setRecordState")
    public boolean setRecordState(
            @WebParam(name = "cCNB")
            String ccnb,
            @WebParam(name = "barcode")
            String barcode,
            @WebParam(name = "newState")
            DigitizationState newState,
            @WebParam(name = "oldState")
            DigitizationState oldState,
            @WebParam(name = "user")
            String user,
            @WebParam(name = "date")
            Date date) throws DigitizationRegistryException {

        StringBuilder failureMsg = new StringBuilder();
        checkNotNullNotEmptyParam("cCNB", ccnb, failureMsg);
        checkNotNullNotEmptyParam("barcode", barcode, failureMsg);
        checkStateParam(newState, failureMsg);
        if (newState == DigitizationState.FINISHED) {
            checkNotNullNotEmptyParam("user", barcode, failureMsg);
        }
        if (failureMsg.length() > 0) {
            throw new DigitizationRegistryException(failureMsg.toString());
        }

        try {
            DigitizationRegistryDao dao = new DigitizationRegistryDao();
            return dao.updateRecordState(ccnb, barcode, newState, oldState, user, date);
        } catch (DataSourceException ex) {
            Logger.getLogger(DigitizationRegistry.class.getName()).log(Level.SEVERE, null, ex);
            throw DigitizationRegistryException.internalServiceError();
        }
    }

    private static void checkNotNullParam(String param, Object value, StringBuilder failureMsg) {
        if (value == null) {
            buildFailureMsg(failureMsg, "Missing '%s' parameter.", param);
        }
    }

    private static void checkNotNullNotEmptyParam(String param, String value, StringBuilder failureMsg) {
        checkNotNullParam(param, value, failureMsg);
        if (value != null && value.length() == 0) {
            buildFailureMsg(failureMsg, "'%s' parameter is empty.", param);
        }
    }

    private static void checkStateParam(DigitizationState state, StringBuilder failureMsg) {
        checkNotNullParam("state", state, failureMsg);
        if (state != null && state == DigitizationState.UNDEFINED) {
            buildFailureMsg(failureMsg, "Illegal 'state' parameter value '%s'.", state);
        }
    }

    private static StringBuilder buildFailureMsg(StringBuilder failureMsg, String msg, Object... args) {
        if (failureMsg.length() > 0) {
            failureMsg.append('\n');
        }
        failureMsg.append(String.format(msg, args));
        return failureMsg;
    }


}
