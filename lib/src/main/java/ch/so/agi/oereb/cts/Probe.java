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
        // 2022-12-17: Ich denke unser Verhalten ist nicht korrekt. Es ist unlogisch einen Accept-
        // Header zu schicken und gleichzeit im Pfad das Outputformat anzugeben.

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
