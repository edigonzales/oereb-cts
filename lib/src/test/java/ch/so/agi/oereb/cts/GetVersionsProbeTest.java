package ch.so.agi.oereb.cts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

public class GetVersionsProbeTest {
    private MockWebServer mockWebServer;

    @BeforeEach
    public void setup() throws IOException {
      this.mockWebServer = new MockWebServer();
      this.mockWebServer.start();
    }

    @Test
    public void versions_ok() throws IOException {
        // Prepare 
        var xmlResponse = Files.readString(Paths.get("src/test/data/versions_ok.xml"));
       
        var mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/xml")
                .setResponseCode(200)
                .setBody(xmlResponse);

        mockWebServer.enqueue(mockResponse);
        
        var request = "/versions/xml/";
        mockWebServer.url(request);
        
        var serviceEndpoint = URI.create(mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        var requestUrl = URI.create("http://" + serviceEndpoint + "/" + request);
        
        // Run test
        var probe = new GetVersionsProbe();
        var result = probe.run(requestUrl);
        
        //System.out.println(result);

        // Validate
        assertTrue(result.isSuccess()); 
    }
}
