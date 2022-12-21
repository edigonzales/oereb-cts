package ch.so.agi.oereb.cts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class GetEGRIDProbeTest {
    private MockWebServer mockWebServer;

    @BeforeEach
    public void setup() throws IOException {
      this.mockWebServer = new MockWebServer();
      this.mockWebServer.start();
    }
    
    @Test
    public void getegrid_en_ok() throws IOException {
        // Prepare 
        var xmlResponse = Files.readString(Paths.get("src/test/data/getegrid_en_ok.xml"));
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/xml")
                .setResponseCode(200)
                .setBody(xmlResponse);
                //.throttleBody(16, 5, TimeUnit.SECONDS);

        mockWebServer.enqueue(mockResponse);
        
        var request = "/getegrid/xml/?EN=2600589,1215498";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + "/" + request);
        
        // Run test
        var probe = new GetEGRIDProbe();
        var result = probe.run(requestUrl);
        
//        for (Result res : result.getResults()) {
//            System.out.println(res.toString());
//        } 

        // Validate
        assertTrue(result.isSuccess());
        
//        System.out.println(result.getRequest());
//        System.out.println(result.isSuccess());
//        System.out.println(result.toString());
//        System.out.println(result.getStatusCode());
//        System.out.println(result.getResultFileLocation());
//        System.out.println(result.message);
    }
    
    @Test
    public void getegrid_en_ok_with_extended_content_type() throws IOException {
        // Prepare 
        var xmlResponse = Files.readString(Paths.get("src/test/data/getegrid_en_ok.xml"));
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/xml; charset=UTF-8")
                .setResponseCode(200)
                .setBody(xmlResponse);

        mockWebServer.enqueue(mockResponse);
        
        var request = "/getegrid/xml/?EN=2600589,1215498";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + "/" + request);
        
        // Run test
        var probe = new GetEGRIDProbe();
        var result = probe.run(requestUrl);

        // Validate
        assertTrue(result.isSuccess());
    }

    
    
    @Test
    public void getegrid_en_fail_statuscode() throws IOException {
        // Prepare 
        var xmlResponse = Files.readString(Paths.get("src/test/data/getegrid_en_ok.xml"));
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/xml")
                .setResponseCode(201)
                .setBody(xmlResponse);

        mockWebServer.enqueue(mockResponse);
        
        var request = "/getegrid/xml/?EN=2600589,1215498";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + "/" + request);
        
        // Run test
        var probe = new GetEGRIDProbe();
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
    public void getegrid_en_fail_responsecontenttype() throws IOException {
        // Prepare 
        var xmlResponse = Files.readString(Paths.get("src/test/data/getegrid_en_ok.xml"));
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "text/xml")
                .setResponseCode(200)
                .setBody(xmlResponse);

        mockWebServer.enqueue(mockResponse);
        
        var request = "/getegrid/xml/?EN=2600589,1215498";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + "/" + request);
        
        // Run test
        var probe = new GetEGRIDProbe();
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

    @Test
    public void getegrid_en_fail_missing_geometry() throws IOException {
        // Prepare 
        var xmlResponse = Files.readString(Paths.get("src/test/data/getegrid_en_ok.xml"));
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/xml")
                .setResponseCode(200)
                .setBody(xmlResponse);

        mockWebServer.enqueue(mockResponse);
        
        var request = "/getegrid/xml/?EN=2600589,1215498&GEOMETRY=true";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + "/" + request);
        
        // Run test
        var probe = new GetEGRIDProbe();
        var result = probe.run(requestUrl);
        
        // Validate
        assertFalse(result.isSuccess());
        for (Result res : result.getResults()) {
            if (res.getClassName().equalsIgnoreCase("ch.so.agi.oereb.cts.GeometryNodeExistenceCheck")) {
                assertFalse(res.isSuccess());
                break;
            }
        } 
    }

    @Test
    public void getegrid_en_fail_superflous_geometry() throws IOException {
        // Prepare 
        var xmlResponse = Files.readString(Paths.get("src/test/data/getegrid_en_geometry_ok.xml"));
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/xml")
                .setResponseCode(200)
                .setBody(xmlResponse);

        mockWebServer.enqueue(mockResponse);
        
        var request = "/getegrid/xml/?EN=2600589,1215498";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + "/" + request);
        
        // Run test
        var probe = new GetEGRIDProbe();
        var result = probe.run(requestUrl);
        
        // Validate
        assertFalse(result.isSuccess());
        for (Result res : result.getResults()) {
            if (res.getClassName().equalsIgnoreCase("ch.so.agi.oereb.cts.GeometryNodeExistenceCheck")) {
                assertFalse(res.isSuccess());
                break;
            }
        } 
    }

    @Test
    public void getegrid_en_fail_notvalid_schema() throws IOException {
        // Prepare 
        var xmlResponse = Files.readString(Paths.get("src/test/data/getegrid_en_notvalid_schema.xml"));
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/xml")
                .setResponseCode(200)
                .setBody(xmlResponse);

        mockWebServer.enqueue(mockResponse);
        
        var request = "/getegrid/xml/?EN=2600589,1215498";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + "/" + request);
        
        // Run test
        var probe = new GetEGRIDProbe();
        var result = probe.run(requestUrl);
        
        // Validate
        assertFalse(result.isSuccess());
        for (Result res : result.getResults()) {
            if (res.getClassName().equalsIgnoreCase("ch.so.agi.oereb.cts.SchemaCheck")) {
                assertFalse(res.isSuccess());
                break;
            }
        } 
    }
    
    @Test
    public void getegrid_en_fail_not_lv95() throws IOException {
        // Prepare 
        var xmlResponse = Files.readString(Paths.get("src/test/data/getegrid_en_wrong_coordsystem.xml"));
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/xml")
                .setResponseCode(200)
                .setBody(xmlResponse);

        mockWebServer.enqueue(mockResponse);
        
        var request = "/getegrid/xml/?EN=2694124,1180546&GEOMETRY=true";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + "/" + request);
        
        // Run test
        var probe = new GetEGRIDProbe();
        var result = probe.run(requestUrl);
        
        // Validate
        assertFalse(result.isSuccess());
        for (Result res : result.getResults()) {
            if (res.getClassName().equalsIgnoreCase("ch.so.agi.oereb.cts.CoordSystemCheck")) {
                assertFalse(res.isSuccess());
                break;
            }
        } 
    }

}
