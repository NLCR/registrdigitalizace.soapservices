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
import cz.registrdigitalizace.soapservices.model.PlainQuery;
import cz.registrdigitalizace.soapservices.model.RecordFormat;
import cz.registrdigitalizace.soapservices.transform.MarcTransformer;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.ws.WebServiceContext;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SOAP web service makes available <a href="http://registrdigitalizace.cz">
 * Digitization Registry CZ</a> content.
 *
 * @author Jan Pokorsky
 */
@WebService(targetNamespace="http://registrdigitalizace.cz/soapservices")
public class DigitizationRegistry {

    @Resource
    private WebServiceContext wsContext;

    /**
     * Returns list of digitization records described in required format.
     *
     * @param query query to select particular records
     * @param format format of record descriptor. It may be {@code null}
     *              for default {@link RecordFormat#MARC_XML Marc XML}.
     * @param maxResults limits number of returned records (since 1.1)
     * @return list of existing digitization records.
     * @throws DigitizationRegistryException in case of illegal parameters or some internal error.
     */
    @WebMethod(operationName = "findRecords")
    public List<DigitizationRecord> findRecords(
            @WebParam(name = "query")
            PlainQuery query,
            @WebParam(name = "format")
            RecordFormat format,
            @WebParam(name = "maxResults")
            Integer maxResults
            ) throws DigitizationRegistryException {

        StringBuilder failureMsg = new StringBuilder();
        checkNotNullParam("query", query, null);
        if (format == null) {
            format = RecordFormat.MARC_XML;
        }
        if (query != null) {
            String barcode = query.getBarcode();
            String ccnb = query.getCcnb();
            String isbn = query.getIsbn();
            String issn = query.getIssn();
            String name = query.getTitle();
            String signature = query.getSignature();
            String pole001 = query.getPole001();
            boolean anyValid = barcode != null && barcode.length() > 0
                    || ccnb != null && ccnb.length() > 0
                    || isbn != null && isbn.length() > 0
                    || issn != null && issn.length() > 0
                    || name != null && name.length() > 0
                    || signature != null && signature.length() > 0
                    || pole001 != null && pole001.length() > 0;
            if (!anyValid) {
                buildFailureMsg(failureMsg, "Invalid query. Any non-empty parameter required.");
            }
        }

        if (failureMsg.length() > 0) {
            throw new DigitizationRegistryException(failureMsg.toString());
        }

        int limit = 1000;
        if (maxResults != null && maxResults > 0 && maxResults < limit) {
            limit = maxResults;
        }

        try {
            DigitizationRegistryDao dao = new DigitizationRegistryDao();
            List<DigitizationRecord> records = dao.findRecords(query, limit);

            MarcTransformer transormer = new MarcTransformer();
            for (DigitizationRecord record : records) {
                Source source = record.getDescriptor();
                if (source != null) {
                    source = transormer.transform(source, format);
                    record.setDescriptor(source);
                }
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
     * Gets scanning state for a given record.
     *
     * @param recordId ID of required record
     * @return the scanning state or {@code null} iff no such record exists.
     * @throws DigitizationRegistryException in case of illegal parameters or some internal error.
     */
    @WebMethod(operationName = "getRecordState")
    public DigitizationState getRecordState(
            @WebParam(name = "recordId")
            int recordId) throws DigitizationRegistryException {

        StringBuilder failureMsg = new StringBuilder();
        checkRecordIdParam(recordId, failureMsg);
        if (failureMsg.length() > 0) {
            throw new DigitizationRegistryException(failureMsg.toString());
        }

        try {
            DigitizationRegistryDao dao = new DigitizationRegistryDao();
            return dao.getRecordState(recordId);
        } catch (DataSourceException ex) {
            Logger.getLogger(DigitizationRegistry.class.getName()).log(Level.SEVERE, null, ex);
            throw DigitizationRegistryException.internalServiceError();
        }
    }

    /**
     * Updates digitization state for a given record.
     *
     * @param recordId ID of required record
     * @param newState new digitization state
     * @param oldState old digitization state. Use
     *          {@link #getRecordState(int) getRecordState}
     *          or {@link #findRecords(cz.registrdigitalizace.soapservices.model.PlainQuery, cz.registrdigitalizace.soapservices.model.RecordFormat) findRecords}
     * @param user scanner operator
     * @param date scanning date. In case it is {@code null} the present date is used.
     * @return {@code true} when the update passes or {@code false} if there is no such
     *          record or the oldState has been modified by someone else in the meantime.
     * @throws DigitizationRegistryException in case of illegal parameters or some internal error.
     */
    @WebMethod(operationName = "setRecordState")
    public boolean setRecordState(
            @WebParam(name = "recordId")
            int recordId,
            @WebParam(name = "newState")
            DigitizationState newState,
            @WebParam(name = "oldState")
            DigitizationState oldState,
            @WebParam(name = "user")
            String user,
            @WebParam(name = "date")
            Date date) throws DigitizationRegistryException {

        checkWritePermission();
        StringBuilder failureMsg = new StringBuilder();
        checkRecordIdParam(recordId, failureMsg);
        checkStateParam(newState, failureMsg);
        if (newState == DigitizationState.FINISHED) {
            checkNotNullNotEmptyParam("user", user, failureMsg);
        }
        if (failureMsg.length() > 0) {
            throw new DigitizationRegistryException(failureMsg.toString());
        }

        try {
            DigitizationRegistryDao dao = new DigitizationRegistryDao();
            return dao.updateRecordState(recordId, newState, oldState, user, date);
        } catch (DataSourceException ex) {
            Logger.getLogger(DigitizationRegistry.class.getName()).log(Level.SEVERE, null, ex);
            throw DigitizationRegistryException.internalServiceError();
        }
    }

    /**
     * Adds list of URN:NBN items for given digitization record.
     * If any item matches existing one it is excluded from insert.
     *
     * @param recordId ID of required record
     * @param date date of allocation or {@code null} for present date
     * @param urnNbns list of URN:NBN. Valid list size is 1-1000.
     * @return {@code true} when the update passes or {@code false} if there is no such record
     * @throws DigitizationRegistryException in case of illegal parameters or some internal error.
     */
    @WebMethod(operationName = "addRecordUrnNbn")
    public boolean addRecordUrnNbn(
            @WebParam(name = "recordId") int recordId,
            @WebParam(name = "date") Date date,
            @WebParam(name = "urnNbn") List<String> urnNbns
            ) throws DigitizationRegistryException {

        checkWritePermission();
        StringBuilder failureMsg = new StringBuilder();
        checkRecordIdParam(recordId, failureMsg);
        checkCollectionParam("urnNbnList", urnNbns, 1, 1000, failureMsg);
        if (failureMsg.length() > 0) {
            throw new DigitizationRegistryException(failureMsg.toString());
        }
        LinkedHashSet<String> uniqueUrnNbns = new LinkedHashSet<String>(urnNbns.size());
        for (String urnNbn : urnNbns) {
            if (urnNbn != null) {
                urnNbn = urnNbn.trim();
                if (!urnNbn.isEmpty()) {
                    uniqueUrnNbns.add(urnNbn);
                }
            }
        }
        if (uniqueUrnNbns.isEmpty()) {
            throw new DigitizationRegistryException("'urnNbnList' parameter contains no valid item!");
        }

        try {
            DigitizationRegistryDao dao = new DigitizationRegistryDao();
            return dao.addRecordUrnNbn(recordId, uniqueUrnNbns, date);
        } catch (DataSourceException ex) {
            Logger.getLogger(DigitizationRegistry.class.getName()).log(Level.SEVERE, null, ex);
            throw DigitizationRegistryException.internalServiceError();
        }
    }

    /**
     * Sets list of URN:NBN items for given digitization record. It removes all
     * existing URN:NBN.
     *
     * @param recordId ID of required record
     * @param date date of allocation or {@code null} for present date
     * @param urnNbns list of URN:NBN. Valid list size is 0-1000.
     * @return {@code true} when the update passes or {@code false} if there is no such record
     * @throws DigitizationRegistryException in case of illegal parameters or some internal error.
     */
    @WebMethod(operationName = "setRecordUrnNbn")
    public boolean setRecordUrnNbn(
            @WebParam(name = "recordId") int recordId,
            @WebParam(name = "date") Date date,
            @WebParam(name = "urnNbn") List<String> urnNbns
            ) throws DigitizationRegistryException {

        checkWritePermission();
        StringBuilder failureMsg = new StringBuilder();
        checkRecordIdParam(recordId, failureMsg);
        // urnNbnList can be empty for set => remove
        checkCollectionParam("urnNbnList", urnNbns, 0, 1000, failureMsg);
        if (failureMsg.length() > 0) {
            throw new DigitizationRegistryException(failureMsg.toString());
        }
        LinkedHashSet<String> uniqueUrnNbns = new LinkedHashSet<String>(urnNbns.size());
        for (String urnNbn : urnNbns) {
            if (urnNbn != null) {
                urnNbn = urnNbn.trim();
                if (!urnNbn.isEmpty()) {
                    uniqueUrnNbns.add(urnNbn);
                }
            }
        }
        
        try {
            DigitizationRegistryDao dao = new DigitizationRegistryDao();
            return dao.setRecordUrnNbn(recordId, uniqueUrnNbns, date);
        } catch (DataSourceException ex) {
            Logger.getLogger(DigitizationRegistry.class.getName()).log(Level.SEVERE, null, ex);
            throw DigitizationRegistryException.internalServiceError();
        }
    }

    private void checkWritePermission() throws DigitizationRegistryException {
        if (!wsContext.isUserInRole("registry-ws")) {
            throw new DigitizationRegistryException(
                    "Access forbidden. You don't have permission to run the operation.");
        }
    }

    private static void checkNotNullParam(String param, Object value, StringBuilder failureMsg) {
        if (value == null) {
            buildFailureMsg(failureMsg, "Missing '%s' parameter.", param);
        }
    }

    private static void checkCollectionParam(String param, Collection<?> value, Integer min, Integer max, StringBuilder failureMsg) {
        if (min != null) {
            checkNotNullParam(param, value, failureMsg);
            if (value != null && value.size() < min) {
                buildFailureMsg(failureMsg, "'%s' parameter requires at least %s item(s).", param, min);
            }
        }
        if (max != null) {
            if (value != null && value.size() > max) {
                buildFailureMsg(failureMsg, "'%s' parameter accepts no more than %s item(s).", param, max);
            }
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

    private static void checkRecordIdParam(int recordId, StringBuilder failureMsg) {
        if (recordId < 0) {
            buildFailureMsg(failureMsg, "Illegal 'recordId' parameter value '%s'.", recordId);
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
