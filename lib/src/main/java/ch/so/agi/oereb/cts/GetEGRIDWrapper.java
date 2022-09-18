package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class GetEGRIDWrapper extends Probe /* implements IProbe*/ {
    final Logger log = LoggerFactory.getLogger(GetEGRIDWrapper.class);
    
    private static String FOLDER_PREFIX = "oerebcts";

    private List<String> requestTemplates = List.of(
            "/getegrid/xml/?EN=${EN}",
            "/getegrid/xml/?IDENTDN=${IDENTDN}&NUMBER=${NUMBER}"
            );
    
    private List<String> queryParameters = List.of(
            "",
            "&GEOMETRY=true",
            "&GEOMETRY=false"
            );
        
    public List<Result> run(String serviceEndpoint, Map<String,String> parameters) throws IOException {
        List<Result> resultList = new ArrayList<Result>();

        for (var requestTemplate : requestTemplates) {
            var subsitutor = new StringSubstitutor(parameters);
            subsitutor.setEnableUndefinedVariableException(true);
            String resolvedRequestTemplate = null;
            try {
                resolvedRequestTemplate = subsitutor.replace(requestTemplate);
            } catch (IllegalArgumentException e) {
                // Es wurden keine passenden Parameter gefunden, um die Platzhalter zu ersetzen.
                // In diesem Fall soll/kann die Prüfung nicht durchgeführt werden.
                continue;
            }
            
            try {
                var requestUrlString = URLDecoder.decode(serviceEndpoint + "/" + resolvedRequestTemplate, StandardCharsets.UTF_8.name());
                requestUrlString = fixUrl(requestUrlString);
                
                for (var queryParameter : queryParameters) { 
                    var requestUrl = URI.create(requestUrlString + queryParameter);
                    var serviceEndpointUrl = URI.create(serviceEndpoint);

                    var probe = new GetEGRIDProbe();
                    var probeResult = probe.run(serviceEndpointUrl, requestUrl);
                    
                    resultList.add(probeResult);
                }
            } catch (UnsupportedEncodingException e) {
                // TODO result...
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
        return resultList;
    }    
}
