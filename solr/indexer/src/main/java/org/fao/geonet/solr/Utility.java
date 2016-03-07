package org.fao.geonet.solr;

import net.sf.saxon.Configuration;
import net.sf.saxon.FeatureKeys;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.commons.codec.digest.DigestUtils;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by francois on 10/12/14.
 */
public class Utility {
    /**
     * Encrypt a string using sha256Hex
     *
     * @param stringToEncrypt
     * @return
     */
    public String encrypt(@Header("stringToEncrypt") String stringToEncrypt) {
        return DigestUtils.sha256Hex(stringToEncrypt);
    }


    /**
     * Run XSLT transformation on the body of the Exchange
     * and set the output body to the results of the transformation.
     */
    public StreamResult transform(Exchange exchange, String xslt) throws TransformerConfigurationException {
        String xml = exchange.getIn().getBody(String.class);

        exchange.getOut().setHeaders(exchange.getIn().getHeaders());


        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        try {
            builder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = null;
        try {
            document = builder.parse(
                    new InputSource(
                            new StringReader(xml)
                    )
            );
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DOMSource source = new DOMSource(document);

        // TODO: Should depends on javax/xml.transform.TransformerFactory file
        TransformerFactoryFactory.init("net.sf.saxon.TransformerFactoryImpl");
        TransformerFactory transFact = TransformerFactoryFactory.getTransformerFactory();

        InputStream streamSource = this.getClass().getResourceAsStream(xslt);
        URL url = this.getClass().getResource(xslt);

        Source stylesheet = new StreamSource(streamSource);
        // http://stackoverflow.com/questions/3699860/resolving-relative-paths-when-loading-xslt-files
        stylesheet.setSystemId(url.toExternalForm());

        StringWriter sw = new StringWriter();

        StreamResult result = new StreamResult(sw);
        try {
            transFact.setAttribute(FeatureKeys.VERSION_WARNING,false);
            transFact.setAttribute(FeatureKeys.LINE_NUMBERING,true);
            transFact.setAttribute(FeatureKeys.PRE_EVALUATE_DOC_FUNCTION,true);
            transFact.setAttribute(FeatureKeys.RECOVERY_POLICY, Configuration.RECOVER_SILENTLY);
            // Add the following to get timing info on xslt transformations
            transFact.setAttribute(FeatureKeys.TIMING,true);
        } catch (IllegalArgumentException e) {
            System.out.println("WARNING: transformerfactory doesnt like saxon attributes!");
            //e.printStackTrace();
        } finally {
            Transformer t = null;
            try {
                t = transFact.newTransformer(stylesheet);
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            }
            Map<String, Object> headers = exchange.getIn().getHeaders();
            Iterator<String> iterator = headers.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object value = headers.get(key);
                if (value != null) {
                    t.setParameter(key, value);
                }
            }
            try {
                t.transform(source, result);

                exchange.getOut().setBody(result.getWriter().toString());
            } catch (TransformerException e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}
