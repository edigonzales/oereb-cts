package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSuite {
    final Logger log = LoggerFactory.getLogger(TestSuite.class);
    
    private static final String FOLDER_PREFIX = "oerebcts";

    
    public void run(Map<String,String> parameters) throws IOException {
        if (parameters.get("SERVICE_ENDPOINT") == null) {
            throw new IllegalArgumentException("no service endpoint found");
        }
        
        Path workFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), FOLDER_PREFIX);        

        HttpClient httpClient = createHttpClient();
        
        // Anhand der Parameter (vom Benutzer) werden zuerst die URL
        // konstruiert. Es sind nicht immer alle Requests möglich, 
        // z.B. wenn der Benutzer nur den EGRID übergibt, ist kein
        // getegrid-Request möglich.
        List<String> requestUrls = RequestBuilder.build(parameters);
        for (String requestUrl : requestUrls) {
            
            HttpResponse<Path> response = null;
            try {
                response = makeRequest(httpClient, workFolder, requestUrl);
            } catch (IOException | InterruptedException | URISyntaxException e) {
                e.printStackTrace();
                throw new IOException(e.getMessage());
            }
            
            
            //break;
            
            
        }
        
       
        
    }

    private HttpResponse<Path> makeRequest(HttpClient httpClient, Path workFolder, String requestUrl) throws IOException, HttpTimeoutException, InterruptedException, URISyntaxException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.GET().uri(new URI(requestUrl));

        HttpRequest request = requestBuilder.timeout(Duration.ofMinutes(2L)).build();
        
        String fileName = createFileName(requestUrl);
        Path responseFile = Paths.get(workFolder.toAbsolutePath().toString(), fileName);      

        log.debug("Requesting: {}", request.uri().toString());
        HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(responseFile));
        
        return response;
    }        
    
    private HttpClient createHttpClient() {
        var httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.NEVER)
                .build();
        return httpClient;
    }
    
    private String createFileName(String requestUrl) {
        String fileName = requestUrl
                .replace("https://", "")
                    .replace("http://", "")
                    .replace(".", "-")
                    .replace("&", "_")
                    .replace("?", "")
                    .replace("/", "_")
                    .replace("=", "_")
                    .replace(",", "-")
                    .toLowerCase();
        
        if (requestUrl.contains("/xml")) {
            fileName += ".xml";
        } else if (requestUrl.toString().contains("/pdf")) {
            fileName += ".pdf";
        } else {
            fileName += ".undefined";
        }
        return fileName;
    }

}
