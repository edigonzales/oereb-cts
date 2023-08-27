package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SchemaCheck extends Check implements ICheck {

    public SchemaCheck() {
        super.name = this.getClass().getCanonicalName();
        super.description = "Validates the returned xml against the corresponding xml schema.";
    }

    @Override
    public Result run(HttpResponse<Path> response) {        
        Result result = new Result();
        result.setClassName(this.name);
        result.setDescription(this.description);
        result.start();

        String xsd = "oereb_v2_0/Extract.xsd";
        String requestUrl = response.request().uri().toString();
        if (requestUrl.toLowerCase().contains("/versions/")) {
            xsd = "oereb_v2_0/Versioning.xsd";
        } 
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        try {
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(response.body().toFile());

            Schema schema = schemaFactory.newSchema(getClass().getClassLoader().getResource(xsd));
            
            Validator validator = schema.newValidator();
                        
            final List<SAXParseException> exceptions = new LinkedList<SAXParseException>();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    exceptions.add(exception);
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    exceptions.add(exception);
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    exceptions.add(exception);
                }
            });

            validator.validate(new DOMSource(doc));
            
            if (exceptions.size() == 0) {
                result.setSuccess(true);
                result.stop();
                return result;
            }

            String errorMessage = new String();
            for(SAXParseException exception : exceptions) {
                errorMessage += exception.getMessage() + "\n";
            }
            result.setSuccess(false);
            result.setMessage(errorMessage);
            
        } catch (SAXException | ParserConfigurationException | IOException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        result.stop();
        return result;    
    }
}
