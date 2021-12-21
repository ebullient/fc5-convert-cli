package dev.ebullient.fc5;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlStreamUtils {
    public static StreamSource getSourceFor(String jarResource) throws IOException {
        URL source = ClassLoader.getSystemResource(jarResource);
        return new StreamSource(source.openStream(), source.toString());
    }

    public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setEntityResolver(new DummyEntityResolver());
        return db;
    }

    static class DummyEntityResolver implements EntityResolver {
        public InputSource resolveEntity(String publicID, String systemID)
                throws SAXException {
            System.out.println("Resolve entity: " + publicID + ", " + systemID);
            return new InputSource(new StringReader(""));
        }
    }

}
