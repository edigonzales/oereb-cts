package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
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
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

public abstract class Probe {
    protected static String FOLDER_PREFIX = "oerebcts";

    // Anhand des Requests (d.h. ohne User-Input) kann nicht abschliessend geprüft werden, ob der Status-Code
    // korrekt ist. Aus diesem Grund muss er immer auch vom Menschen überprüft werden. Er kann nur drei Werte 
    // annehmen. Falls der Status-Code nicht einem dieser Werte entspricht, wird der Check
    // als nicht erfüllt dokumentiert.
    protected Result validateStatusCode(HttpResponse<Path> response) {
        int statusCode = response.statusCode();
        
        var result = new Result();
        result.setClassName("StatusCode");
        result.setDescription("Checks the returned http status code.");
        result.setStatusCode(statusCode);
        result.start();

        if (statusCode != 200 && statusCode != 204 && statusCode != 500 && statusCode != 303) {
            result.setSuccess(false);
            result.setMessage("Returned status code does not match expected status code (200, 204, 303, 500).");
        } 
        result.stop();
        return result;
    }

    protected Result validateResponseContentType(HttpResponse<Path> response) {
        Result result = new Result();
        result.setClassName("ResponseContentType");
        result.setDescription("Checks the content type header of the response.");
        result.start();

        String requestUrl = response.request().uri().toString();
        try {
            String contentType = response.headers().firstValue("content-type").orElseThrow(()-> {
                return new Exception("Content-Type header not present.");
            });
                        
            if (requestUrl.contains("/pdf/")) {
                if (contentType.equalsIgnoreCase("application/pdf")) {
                    result.setSuccess(true);
                } else {
                    result.setSuccess(false);
                    result.setMessage("Response Content-Type must be 'application/pdf'. Response Content-Type is '" + contentType + "'.");
                }
            } else if (requestUrl.contains("/xml/")) {
                if (contentType.equalsIgnoreCase("application/xml")) {
                    result.setSuccess(true);
                } else {
                    result.setSuccess(false);
                    result.setMessage("Response Content-Type must be 'application/xml'. Response Content-Type is '" + contentType + "'.");
                }
            } else {
                result.setSuccess(false);
                result.setMessage("Response Content-Type '" + contentType + "' is not allowed.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        result.stop();
        return result;
    }

    protected Result validateSchema(HttpResponse<Path> response, String xsd) {
        var result = new Result();
        result.setClassName("SchemaValidation");
        result.setDescription("Validates the returned xml against the corresponding xml schema.");
        result.start();
        
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
    
    protected Result validateFederalTopicCodesExistence(HttpResponse<Path> response) throws SaxonApiException {
        Result result = new Result();
        result.setClassName("FederalTopicCodesExistence");
        result.setDescription("Checks if the returned xml document has all federal topics in the themes table of contents.");
        result.start();
        
        Processor proc = new Processor(false);
        XPathCompiler xpath = proc.newXPathCompiler();
        xpath.declareNamespace("geom", "http://www.interlis.ch/geometry/1.0");
        xpath.declareNamespace("extract", "http://schemas.geo.admin.ch/V_D/OeREB/2.0/Extract");
        xpath.declareNamespace("data", "http://schemas.geo.admin.ch/V_D/OeREB/2.0/ExtractData");

        DocumentBuilder builder = proc.newDocumentBuilder();
        builder.setLineNumbering(true);
        builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
        XdmNode responseDoc = builder.build(response.body().toFile());

        XPathSelector selector = xpath.compile("//data:Code[parent::data:ConcernedTheme|parent::data:NotConcernedTheme|parent::data:ThemeWithoutData]/text()").load();
        selector.setContextItem(responseDoc);
        
        var themeCodesMap = new HashMap<String,String>();
        for (XdmItem item: selector) {
            XdmNode code = (XdmNode)item;
            themeCodesMap.put(code.getStringValue(), code.getStringValue());
        }
        
        var federalThemeCodesList = List.of("ch.Nutzungsplanung","ch.ProjektierungszonenNationalstrassen","ch.BaulinienNationalstrassen","ch.ProjektierungszonenEisenbahnanlagen",
                "ch.BaulinienEisenbahnanlagen","ch.ProjektierungszonenFlughafenanlagen","ch.BaulinienFlughafenanlagen","ch.Sicherheitszonenplan",
                "ch.BelasteteStandorte","ch.BelasteteStandorteMilitaer","ch.BelasteteStandorteZivileFlugplaetze","ch.BelasteteStandorteOeffentlicherVerkehr",
                "ch.Grundwasserschutzzonen","ch.Grundwasserschutzareale","ch.Laermempfindlichkeitsstufen","ch.StatischeWaldgrenzen",
                "ch.Waldabstandslinien","ch.Planungszonen","ch.Waldreservate","ch.Gewaesserraum","ch.ProjektierungszonenStarkstromanlagen","ch.BaulinienStarkstromanlagen");

        var missingFederalThemeCodes = new ArrayList<String>();
        for (var code : federalThemeCodesList) {
            if (!themeCodesMap.containsKey(code)) {
                missingFederalThemeCodes.add(code);
            }
        }
        
        if (missingFederalThemeCodes.size() > 0) {
            result.setSuccess(false);
            result.setMessage("Missing federal theme codes in table of contents: " + String.join(", ", missingFederalThemeCodes));
        }
        
        result.stop();
        return result;
    }
    
    protected Result validateEmbeddedImages(HttpResponse<Path> response) throws SaxonApiException {
        Result result = new Result();
        result.setClassName("EmbeddedImages");
        result.setDescription("Checks if the returned xml document has embedded images or is not allowed to have embedded images and must have references to images.");
        result.start();

        String requestUrl = response.request().uri().toString();

        var logoList = List.of("LogoPLRCadastre", "FederalLogo", "CantonalLogo", "MunicipalityLogo");
        for (var logo : logoList) {
            {
                int count = countNodes(response, "count(//data:"+logo+"/text())");

                if (requestUrl.contains("WITHIMAGES=true")) {
                    if (count == 0) {
                        result.setSuccess(false);
                        result.setMessage("Response misses "+logo+" element.");
                    }
                } else if (requestUrl.contains("WITHIMAGES=false") || !requestUrl.contains("WITHIMAGES")) {
                    if (count > 0) {
                        result.setSuccess(false);
                        result.setMessage("Response has a superfluos "+logo+" element.");
                    }
                }
            }
            
            {
                int count = countNodes(response, "count(//data:"+logo+"Ref/text())");

                if (requestUrl.contains("WITHIMAGES=true")) {
                    // Glaube diese Bedingung gilt nicht.
//                    if (count > 0) {
//                        result.setSuccess(false);
//                        result.setMessage("Response has a superfluos "+logo+"Ref element.");
//                    }
                } else if (requestUrl.contains("WITHIMAGES=false") || !requestUrl.contains("WITHIMAGES")) {
                    if (count == 0) {
                        result.setSuccess(false);
                        result.setMessage("Response misses "+logo+"Ref element.");
                    }
                }
            }
        }
        
        {
            int count = countNodes(response, "count(//data:RestrictionOnLandownership/data:Map/data:Image/data:LocalisedBlob/data:Blob/text())");
            
            if (requestUrl.contains("WITHIMAGES=true")) {
                if (count == 0) {
                    result.setSuccess(false);
                    result.setMessage("Response misses image blob element.");
                }
            } else if (requestUrl.contains("WITHIMAGES=false") || !requestUrl.contains("WITHIMAGES")) {
                if (count > 0) {
                    result.setSuccess(false);
                    result.setMessage("Response has a superfluos image blob element.");
                }
            }
        }

        result.stop();
        return result;
    }
        
    // TODO:
    // Das reicht im Prinzip schon nicht ganz:
    // Es sollte geprüft werden, ob alle Elemente, die eine Geometrie haben sollten, auch eine Geometrie haben.
    // Name: validateGeometryNodeExistence ?
    protected Result validateGeometryNodesCount(HttpResponse<Path> response, String expression) {
        Result result = new Result();
        result.setClassName("GeometryExistence");
        result.setDescription("Checks if the returned xml document has geometry elements(s) or is not allowed to have geometry elements based on the request parameters.");
        result.start();

        try {
            int count = countNodes(response, expression);
            
            String requestUrl = response.request().uri().toString();
            
            if (requestUrl.contains("GEOMETRY=true")) {
                if (count == 0) {
                    result.setSuccess(false);
                    result.setMessage("Response misses geometry element(s).");
                }
            } else if (requestUrl.contains("GEOMETRY=false") || !requestUrl.contains("GEOMETRY")) {
                if (count > 0) {
                    result.setSuccess(false);
                    result.setMessage("Response has superfluos geometry element(s).");
                }
            }     
        } catch (SaxonApiException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        } finally {
            result.stop();
        }
        return result;
    }
    
    private int countNodes(HttpResponse<Path> response, String expression) throws SaxonApiException {
        Processor proc = new Processor(false);
        XPathCompiler xpath = proc.newXPathCompiler();
        xpath.declareNamespace("geom", "http://www.interlis.ch/geometry/1.0");
        xpath.declareNamespace("extract", "http://schemas.geo.admin.ch/V_D/OeREB/2.0/Extract");
        xpath.declareNamespace("data", "http://schemas.geo.admin.ch/V_D/OeREB/2.0/ExtractData");

        DocumentBuilder builder = proc.newDocumentBuilder();
        builder.setLineNumbering(true);
        builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
        XdmNode responseDoc = builder.build(response.body().toFile());

        XPathSelector selector = xpath.compile(expression).load();
        selector.setContextItem(responseDoc);
        
        int count = Integer.valueOf(selector.evaluateSingle().getStringValue());
        return count;
    }

    private String createFileName(URI fileName) {
        return fileName
                .toString()
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
                .followRedirects(Redirect.NEVER)
                .build();
        return httpClient;
    }

    protected HttpResponse<Path> makeRequest(File workFolder, URI requestUrl, Result result) throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder();
        requestBuilder.GET().header("accept", "application/xml").uri(requestUrl);
        // TODO: Wir haben bei uns ein leicht unterschiedliches Verhalten zwischen GetEGRID und
        // GetExtract. Beim Extract muss man den Accept-Header setzen. Siehe Controller im Code.

        var request = requestBuilder.build();
        var httpClient = createHttpClient();
        
        var fileName = createFileName(requestUrl);
        var responseFile = Paths.get(workFolder.getAbsolutePath(), fileName);      
        
        result.start();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(responseFile));
        result.stop();
        
        return response;
    }    
        
    // Entfernt doppelte Slashes
    protected String fixUrl(String url) {
        return url.replaceAll("(?<=[^:\\s])(\\/+\\/)", "/");
    }    
}
