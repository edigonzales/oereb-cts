package ch.so.agi.oereb.cts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import okio.Okio;

public class GetExtractByIdProbeTest {
    private MockWebServer mockWebServer;

    @BeforeEach
    public void setup() throws IOException {
      this.mockWebServer = new MockWebServer();
      this.mockWebServer.start();
    }

    @Test
    public void extract_fail_statuscode_url_redirect() throws IOException {
        // Prepare 
        var xmlResponse = Files.readString(Paths.get("src/test/data/getegrid_en_ok.xml"));
       
        var mockResponse = new MockResponse()
                .addHeader("Location", "http://localhost/map/oereb/?egrid=CHCH807306583219")
                .setResponseCode(500);

        mockWebServer.enqueue(mockResponse);
        
        var request = "/extract/url/?EGRID=CH807306583219";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + "/" + request);
        
        // Run test
        var probe = new GetExtractByIdProbe();
        var result = probe.run(requestUrl);

        // Validate
        assertFalse(result.isSuccess());
        for (Result res : result.getResults()) {
            if (res.getClassName().equalsIgnoreCase("ch.so.agi.oereb.cts.StatusCodeCheck")) {
                assertFalse(res.isSuccess());
                break;
            }
        } 
    }
    
    @Test
    public void extract_pdf_format_ok() throws IOException {
        // Prepare 
        var pdfFile = Paths.get("src/test/data/pdfa1a_ok.pdf").toFile();
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/pdf")
                .setResponseCode(200)
                .setBody(fileToBytes(pdfFile));

        mockWebServer.enqueue(mockResponse);
        
        var request = "/extract/pdf/?EGRID=CH955832730623";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + request);
                
        // Run test
        var probe = new GetExtractByIdProbe();
        var result = probe.run(requestUrl);
        
        // Validate
        assertTrue(result.isSuccess());
    }
    
    @Test
    public void extract_pdf_format_fail() throws IOException {
        // Prepare 
        var pdfFile = Paths.get("src/test/data/pdfa1a_fail.pdf").toFile();
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/pdf")
                .setResponseCode(200)
                .setBody(fileToBytes(pdfFile));

        mockWebServer.enqueue(mockResponse);
        
        var request = "/extract/pdf/?EGRID=CH767982496078";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + request);
                
        // Run test
        var probe = new GetExtractByIdProbe();
        var result = probe.run(requestUrl);
                
        // Validate
        assertFalse(result.isSuccess());
        for (Result res : result.getResults()) {
            if (res.getClassName().equalsIgnoreCase("ch.so.agi.oereb.cts.PdfFormatCheck")) {
                assertFalse(res.isSuccess());
                break;
            }
        } 
    }
    
    @Test
    public void extract_pdf_responsecontenttype_fail() throws IOException {
        // Prepare 
        var pdfFile = Paths.get("src/test/data/pdfa1a_ok.pdf").toFile();
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/notpdf")
                .setResponseCode(200)
                .setBody(fileToBytes(pdfFile));

        mockWebServer.enqueue(mockResponse);
        
        var request = "/extract/pdf/?EGRID=CH955832730623";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + request);
                
        // Run test
        var probe = new GetExtractByIdProbe();
        var result = probe.run(requestUrl);
        
        // Validate
        assertFalse(result.isSuccess());
        for (Result res : result.getResults()) {
            if (res.getClassName().equalsIgnoreCase("ch.so.agi.oereb.cts.ResponseContentTypeCheck")) {
                assertFalse(res.isSuccess());
                break;
            }
        } 
    }

    private Buffer fileToBytes(File file) throws IOException {
        Buffer result = new Buffer();
        result.writeAll(Okio.source(file));
        return result;
    }

    
    
    //@Test
    public void foo() throws IOException {

        {
            //var parameters = Map.of("EN","2600595,1215629","IDENTDN","SO0200002457","NUMBER","168");
            var parameters = Map.of("EGRID","CH807306583219");
            var wrapper = new GetExtractByIdMethod();
            List<Result> results = wrapper.run("https://geo.so.ch/api/oereb/", parameters);

            for (Result result : results) {
                System.out.println(result);
            }
        }        
    }
}
