/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.apps;

// Java
import java.io.File;
import java.net.URL;

// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

//JAXP
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;

/**
 * Manages input if it is an XSL-FO file.
 */
public class FOFileHandler extends InputHandler {
    
    private File fofile = null;
    private URL foURL = null;

    /**
     * Create a FOFileHandler for a file.
     * @param fofile the file to read the FO document.
     */
    public FOFileHandler(File fofile) {
        this.fofile = fofile;
        try {
            baseURL =
                new File(fofile.getAbsolutePath()).getParentFile().toURL().toExternalForm();
        } catch (Exception e) {
            baseURL = "";
        }
    }

    /**
     * Create a FOFileHandler for an URL.
     * @param url the URL to read the FO document.
     */
    public FOFileHandler(URL url) {
        this.foURL = url;
    }

    /**
     * @see org.apache.fop.apps.InputHandler#getInputSource()
     */
    public InputSource getInputSource () {
        if (fofile != null) {
            return super.fileInputSource(fofile);
        }
        return super.urlInputSource(foURL);
    }

    /**
     * @see org.apache.fop.apps.InputHandler#render(Driver)
     */
    public void render(Driver driver) throws FOPException {

        // temporary until baseURL removed from inputHandler objects
        if (driver.getUserAgent().getBaseURL() == null) {
            driver.getUserAgent().setBaseURL(getBaseURL());
        }

        try {
            // Setup JAXP using identity transformer (no stylesheet here)
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            
            // Setup input stream
            Source src = new SAXSource(getInputSource());

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(driver.getDefaultHandler());
            
            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);

        } catch (Exception e) {
            throw new FOPException(e);
        }
    }
    
    /**
     * Creates <code>XMLReader</code> object using default
     * <code>SAXParserFactory</code>
     * @return the created <code>XMLReader</code>
     * @throws FOPException if the parser couldn't be created or configured for proper operation.
     */
    protected static XMLReader createParser() throws FOPException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newSAXParser().getXMLReader();
        } catch (SAXException se) {
            throw new FOPException("Couldn't create XMLReader", se);
        } catch (ParserConfigurationException pce) {
            throw new FOPException("Couldn't create XMLReader", pce);
        }
    }

    /**
     * Returns the fully qualified classname of the standard XML parser for FOP
     * to use.
     * @return the XML parser classname
     */
    public static final String getParserClassName() {
        try {
            return createParser().getClass().getName();
        } catch (FOPException e) {
            return null;
        }
    }
}
