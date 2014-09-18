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

/**
 * The simple query to select particular digitized records. At least one parameter must be specified.
 * In case of multiple parameters 'and' operator is used to build the query expression.
 *
 * @author Jan Pokorsky
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class PlainQuery {

    /** PREDLOHA.CARKOD */
    private String barcode;
    /** PREDLOHA.CCNB */
    private String ccnb;
    /** PREDLOHA.ISBN */
    private String isbn;
    /** PREDLOHA.ISSN */
    private String issn;
    /** PREDLOHA.ROKVYD */
    private String issueDate;
    /**
     * PREDLOHA.SIGNATURA
     * @since 1.1
     */
    private String signature;
    /** PREDLOHA.NAZEV */
    private String title;
    /** PREDLOHA.ROCNIKPER */
    private String volume;



    private String pole001;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getCcnb() {
        return ccnb;
    }

    public void setCcnb(String ccnb) {
        this.ccnb = ccnb;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    /**
     * @since 1.1
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @since 1.1
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    /**
     * @since 1.2
     */
    public String getPole001() {return pole001;}

    /**
     * @since 1.2
     */
    public void setPole001(String pole001) {this.pole001 = pole001;}

}
