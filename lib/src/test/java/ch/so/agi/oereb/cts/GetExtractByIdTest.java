package ch.so.agi.oereb.cts;

import static org.junit.jupiter.api.Assertions.assertFalse;

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

public class GetExtractByIdTest {
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
    
    //@Test
    public void foo() throws IOException {

        {
            //var parameters = Map.of("EN","2600595,1215629","IDENTDN","SO0200002457","NUMBER","168");
            var parameters = Map.of("EGRID","CH807306583219");
            var wrapper = new GetExtractByIdWrapper();
            List<Result> results = wrapper.run("https://geo.so.ch/api/oereb/", parameters);

            for (Result result : results) {
                System.out.println(result);
            }
        }
        
        {
//            var parameters = Map.of("EGRID","CH427890824980");
//            var wrapper = new GetExtractByIdWrapper();
//            List<Result> results = wrapper.run("https://oereb.geo.bl.ch/", parameters);
//
//            for (Result result : results) {
//                //var resultXml = xmlMapper.writeValueAsString(result);
//                System.out.println(result);
//            }
        }
      
//        {
//            //var parameters = Map.of("EN","2694124,1180546","IDENTDN","UR0200001216","NUMBER","112");
//            var parameters = Map.of("EGRID","CH727993074655");
//            var wrapper = new GetExtractByIdWrapper();
//            List<Result> result = wrapper.run("https://prozessor-oereb.ur.ch/oereb/", parameters);
//            
//            String resultXml = xmlMapper.writeValueAsString(result);
//            System.out.println(resultXml);
//        }
        
//        {
//            var parameters = Map.of("EGRID","CH356489796755");
//            var wrapper = new GetExtractByIdWrapper();
//            List<Result> result = wrapper.run("https://api.oereb.bs.ch/", parameters);
//            
//            String resultXml = xmlMapper.writeValueAsString(result);
//            System.out.println(resultXml);
//        }
    }
}
