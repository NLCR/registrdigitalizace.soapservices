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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Digitization state (PREDLOHA.STAVREC).
 * 
 * @author Jan Pokorsky
 */
public enum DigitizationState {

    FINISHED("finished", "finished", "archived"),
    IN_PROGRESS("progress", "active", "pripravenoProMf", "predanoZpracovateli", "progress"),
    SCHEDULED("planovane", "planovane"),
    UNDEFINED(null, (String) null);

    private final Set<String> dbValues;
    private final String toDbValue;

    private DigitizationState(String toDbValue, String... fromDbValues) {
        if (fromDbValues == null || fromDbValues.length == 0) {
            throw new IllegalArgumentException();
        }
        this.dbValues = new HashSet<String>(Arrays.asList(fromDbValues));
        this.toDbValue = toDbValue;
    }

    /**
     * Gets value used to persist enum constant in Relief DB.
     * @return value
     */
    public String getDbValue() {
        return toDbValue;
    }

    /**
     * Maps Relief DB states (PREDLOHA.STAVREC) to enum constants.
     */
    public static DigitizationState resolve(String dbValue) {
        for (DigitizationState state : DigitizationState.values()) {
            if (state.dbValues.contains(dbValue)) {
                return state;
            }
        }
        return DigitizationState.UNDEFINED;
    }


}
