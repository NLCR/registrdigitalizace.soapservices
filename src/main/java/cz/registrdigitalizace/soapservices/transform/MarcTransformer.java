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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Transforms Marc XML documents to various {@link RecordFormat formats}.
 *
 * @author Jan Pokorsky
 * @see <a href='http://www.loc.gov/standards/marcxml/'>MARC 21 XML Schema</a>
 * @see <a href='http://www.loc.gov/standards/mods/mods-conversions.html'>MODS Conversions</a>
 */
public final class MarcTransformer {

    private static final Map<RecordFormat, String> FORMAT2XSL;
    private static final Map<RecordFormat, Templates> FORMAT2TEMPLATES;
    private static final Logger LOG = Logger.getLogger(MarcTransformer.class.getName());

    private static final String DC_RDF_XSL_PATH = "http://www.loc.gov/standards/marcxml/xslt/MARC21slim2RDFDC.xsl";
    private static final String MODS_33_XSL_PATH = "http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3-3.xsl";
    private static final String MODS_34_XSL_PATH = "http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3-4.xsl";

    static {
        FORMAT2TEMPLATES = new EnumMap<RecordFormat, Templates>(RecordFormat.class);
        FORMAT2XSL = new EnumMap<RecordFormat, String>(RecordFormat.class);
        FORMAT2XSL.put(RecordFormat.DC_RDF, DC_RDF_XSL_PATH);
        FORMAT2XSL.put(RecordFormat.MODS_33, MODS_33_XSL_PATH);
        FORMAT2XSL.put(RecordFormat.MODS_34, MODS_34_XSL_PATH);
        initTemplates();
    }

    public Source transform(Source input, RecordFormat format) throws TransformerException {
        if (format == null || format == RecordFormat.MARC_XML) {
            return input;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Result output = new StreamResult(buffer);
        Transformer t = createTransformer(format);
        t.transform(input, output);
        return new StreamSource(new ByteArrayInputStream(buffer.toByteArray()));
    }

    static Source getXsl(RecordFormat format, URIResolver resolver) throws TransformerException {
        String path = FORMAT2XSL.get(format);
        return resolver.resolve(path, path);
    }

    private static Templates createTemplates(RecordFormat recordFormat) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute("debug", true);
        SimpleResolver resolver = new SimpleResolver();
        factory.setURIResolver(resolver);
        Templates templates = factory.newTemplates(getXsl(recordFormat, resolver));
        return templates;
    }

    private static Transformer createTransformer(RecordFormat recordFormat) throws TransformerConfigurationException {
        Templates templates = FORMAT2TEMPLATES.get(recordFormat);
        if (templates == null) {
            throw new TransformerConfigurationException("Cannot transform " + recordFormat);
        }
        return templates.newTransformer();
    }

    private static void initTemplates() {
        for (Map.Entry<RecordFormat, String> entry : FORMAT2XSL.entrySet()) {
            try {
                Templates templates = createTemplates(entry.getKey());
                FORMAT2TEMPLATES.put(entry.getKey(), templates);
            } catch (TransformerException ex) {
                LOG.log(Level.SEVERE, entry.getValue(), ex);
            }
        }
    }

    /**
     * This allows to run MarcTransformer offline.
     */
    static final class SimpleResolver implements URIResolver {

        /** mapping to offline resources */
        private static final Map<String, String> CATALOG = new HashMap<String, String>();

        static {
            CATALOG.put(MODS_34_XSL_PATH, "/xslts/MARC21slim2MODS3-4.xsl");
            CATALOG.put(MODS_33_XSL_PATH,"/xslts/MARC21slim2MODS3-3.xsl");
            CATALOG.put(DC_RDF_XSL_PATH, "/xslts/MARC21slim2RDFDC.xsl");
            CATALOG.put("http://www.loc.gov/standards/marcxml/xslt/MARC21slimUtils.xsl", "/xslts/MARC21slimUtils.xsl");
        }

        public Source resolve(String href, String base) throws TransformerException {
            String path = CATALOG.get(href);
            if (path == null) {
                path = "/xslts/" + href;
            }
            InputStream stream = SimpleResolver.class.getResourceAsStream(path);
            if (stream != null) {
                return new StreamSource(stream, href);
            }

            // delegates to system resolver
            return null;
        }

    }
}
