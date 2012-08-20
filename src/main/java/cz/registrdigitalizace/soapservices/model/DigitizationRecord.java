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

package cz.registrdigitalizace.soapservices.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Source;

/**
 *
 * @author Jan Pokorsky
 */
@XmlType(propOrder={"recordId", "state", "descriptor"})
@XmlAccessorType(XmlAccessType.FIELD)
public final class DigitizationRecord {

    @XmlElement(required=true)
    private int recordId;
    @XmlElement(required=true)
    private DigitizationState state;
    @XmlMimeType("application/xml")
    private Source descriptor;

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public Source getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(Source descriptor) {
        this.descriptor = descriptor;
    }

    public DigitizationState getState() {
        return state;
    }

    public void setState(DigitizationState state) {
        this.state = state;
    }

}
