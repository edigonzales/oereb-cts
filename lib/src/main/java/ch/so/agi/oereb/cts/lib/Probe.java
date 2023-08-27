package ch.so.agi.oereb.cts.lib;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
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
        
    private String createFileNameFromUrl(URI requestUrl) {
        String fileName = requestUrl
                .toString()
                .replace("https://", "")
                    .replace("http://", "")
                    .replace(".", "-")
                    .replace("&", "_")
                    .replace("?", "")
                    .replace("/", "_")
                    .replace("=", "_")
                    .replace(",", "-")
                    .toLowerCase();
        
        if (requestUrl.toString().contains("/xml")) {
            fileName += ".xml";
        } else if (requestUrl.toString().contains("/pdf/")) {
            fileName += ".pdf";
        } else {
            fileName += ".undefined";
        }
        return fileName;
    }
    
    private HttpClient createHttpClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.NEVER)
                .build();
        return httpClient;
    }

    protected HttpResponse<Path> makeRequest(File workFolder, URI requestUrl, Result result) throws IOException, HttpTimeoutException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.GET().uri(requestUrl);

        HttpRequest request = requestBuilder.timeout(Duration.ofMinutes(2L)).build();
        HttpClient httpClient = createHttpClient();
        
        String fileName = createFileNameFromUrl(requestUrl);
        Path responseFile = Paths.get(workFolder.getAbsolutePath(), fileName);      
        
        result.start();
        HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(responseFile));
        result.stop();
        
        return response;
    }        
}
