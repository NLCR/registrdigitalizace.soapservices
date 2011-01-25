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

package cz.registrdigitalizace.soapservices.transform;

import cz.registrdigitalizace.soapservices.model.RecordFormat;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import junit.framework.TestCase;

/**
 *
 * @author Jan Pokorsky
 */
public class MarcTransformerTest extends TestCase {
    
    public MarcTransformerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testTransform2Dc() throws Exception {
        MarcTransformer instance = new MarcTransformer();
        StreamSource inputSource = new StreamSource(
                MarcTransformerTest.class.getResourceAsStream("marc_example.xml"));
        Source transformedSource = instance.transform(inputSource, RecordFormat.DC_RDF);
        assertNotNull(transformedSource);
    }

    public void testTransform2Mods33() throws Exception {
        MarcTransformer instance = new MarcTransformer();
        StreamSource inputSource = new StreamSource(
                MarcTransformerTest.class.getResourceAsStream("marc_example.xml"));
        Source transformedSource = instance.transform(inputSource, RecordFormat.MODS_33);
        assertNotNull(transformedSource);
    }

    public void testTransform2Mods34() throws Exception {
        MarcTransformer instance = new MarcTransformer();
        StreamSource inputSource = new StreamSource(
                MarcTransformerTest.class.getResourceAsStream("marc_example.xml"));
        Source transformedSource = instance.transform(inputSource, RecordFormat.MODS_34);
        assertNotNull(transformedSource);
    }

    public void testGetDcXsl() throws Exception {
        testGetXsltImpl(RecordFormat.DC_RDF);
    }

    public void testGetMods33Xsl() throws Exception {
        testGetXsltImpl(RecordFormat.MODS_33);
    }

    public void testGetMods34Xsl() throws Exception {
        testGetXsltImpl(RecordFormat.MODS_34);
    }

    private void testGetXsltImpl(RecordFormat format) throws TransformerException {
        Source result = MarcTransformer.getXsl(format, new MarcTransformer.SimpleResolver());
        assertNotNull(result);
    }

}
