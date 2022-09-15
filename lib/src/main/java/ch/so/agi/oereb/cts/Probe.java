package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.StreamSupport;

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

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

public abstract class Probe {
    protected void validateGeometryNodesCount(HttpResponse<Path> response, String expression, String queryParameter, Result probeResult) throws SaxonApiException {
        int count = countGeometryNodes(response, expression);

        Result result = new Result();
        result.setClassName("GeometryExistence");
        result.setDescription("Checks if the returned xml document has geometry elements(s) or is not allowed to have geometry elements based on the request parameters.");

        if (queryParameter.contains("GEOMETRY=true")) {
            if (count == 0) {
                result.setSuccess(false);
                result.setMessage("Response misses geometry element(s)");
                probeResult.addResult(result);
            }
        } else if (queryParameter.contains("GEOMETRY=false") || !queryParameter.contains("GEOMETRY")) {
            if (count > 0) {
                result.setSuccess(false);
                result.setMessage("Response has superfluos geometry element(s).");
                probeResult.addResult(result);
            }
        }
    }
    
    private int countGeometryNodes(HttpResponse<Path> response, String expression) throws SaxonApiException {
        Processor proc = new Processor(false);
        XPathCompiler xpath = proc.newXPathCompiler();
        xpath.declareNamespace("geom", "http://www.interlis.ch/geometry/1.0");

        DocumentBuilder builder = proc.newDocumentBuilder();
        builder.setLineNumbering(true);
        builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
        XdmNode responseDoc = builder.build(response.body().toFile());

        XPathSelector selector = xpath.compile(expression).load();
        selector.setContextItem(responseDoc);
        
        return  Long.valueOf(StreamSupport.stream(selector.spliterator(), false).count()).intValue();
    }

    private String createFileName(String fileName) {
        return fileName
                .replace("https://", "")
                    .replace("http://", "")
                    .replace(".", "-")
                    .replace("&", "_")
                    .replace("?", "")
                    .replace("/", "_")
                    .replace("=", "_")
                    .replace(",", "-")
                    .toLowerCase() + ".xml";
    }
    
    private HttpClient createHttpClient() {
        var httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.ALWAYS)
                .build();
        return httpClient;
    }

    protected HttpResponse<Path> makeRequest(File workFolder, String requestUrl) throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder();
        requestBuilder.GET().uri(URI.create(requestUrl));

        var request = requestBuilder.build();
        var httpClient = this.createHttpClient();
        
        var fileName = createFileName(requestUrl);
        var responseFile = Paths.get(workFolder.getAbsolutePath(), fileName);
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(responseFile));

        return response;
    }    
    
    protected void validateSchema(HttpResponse<Path> response, String xsd, Result probeResult) {
        var result = new Result();
        result.setClassName("SchemaValidation");
        result.setDescription("Validates the returned xml against xml schema.");

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
                probeResult.addResult(result);
                return;
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
        probeResult.addResult(result);
    }    

    // Anhand des Requests (d.h. ohne User-Input) kann nicht abschliessend geprüft werden, ob der Status-Code
    // korrekt ist. Aus diesem Grund wird er "bloss" dokumentiert. Er kann nur drei Werte 
    // annehmen. Falls der Status-Code nicht einem dieser Werte entspricht, wird der Check
    // als nicht erfüllt dokumentiert.
    protected void validateStatusCode(HttpResponse<Path> response, Result probeResult) {
        int statusCode = response.statusCode();
        probeResult.setStatusCode(statusCode);

        var result = new Result();
        result.setClassName("StatusCode");
        result.setDescription("Checks the returned http status code.");
        result.setStatusCode(statusCode);

        if (statusCode != 200 && statusCode != 204 && statusCode != 500) {
            result.setSuccess(false);
            result.setMessage("Returned status code does not match expected status code (200, 204, 500)");
        } 
        probeResult.addResult(result);
    }
    
    // Entfernt doppelte Slashes
    protected String fixUrl(String url) {
        return url.replaceAll("(?<=[^:\\s])(\\/+\\/)", "/");
    }    
}
