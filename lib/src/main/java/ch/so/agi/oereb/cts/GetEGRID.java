package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



// TODO:
// falls nicht alle Parameter geliefert werden, wird diese Variante nicht ausgeführt.
// Z.B. nicht alle negativen Tests testen, d.h. Koordinate ausserhalb für 204er

public class GetEGRID /* extends? implements? */ {
    final Logger log = LoggerFactory.getLogger(GetEGRID.class);

    private List<String> requestTemplates = List.of(
            "/getegrid/xml/?EN=${EN}",
            "/getegrid/xml/?IDENTDN=${IDENTDN}&NUMBER=${NUMBER}" 
            );
    
    private List<String> queryParameters = List.of(
            "",
            "&GEOMETRY=true",
            "&GEOMETRY=false"
            );
    
    private String result;
    
    // Result Rückgabewert?
    public void run(String serviceEndpoint, Map<String,String> parameters) {
        

        
        
        for (var requestTemplate : requestTemplates) {
            var subsitutor = new StringSubstitutor(parameters);
            String resolvedRequestTemplate = null;
            try {
                resolvedRequestTemplate = subsitutor.replace(requestTemplate);
            } catch (IllegalArgumentException e) {
                log.info("FUUUUUUUUUUUUUUUUUUUUUUUUUUUUU");
                continue;
            }
            subsitutor.setEnableUndefinedVariableException(true);
            log.info(resolvedRequestTemplate);
            
            try {
                var requestUrl = URLDecoder.decode(serviceEndpoint + "/" + resolvedRequestTemplate, StandardCharsets.UTF_8.name());
                requestUrl = fixUrl(requestUrl);
                
                for (var queryParameter : queryParameters) {
                    requestUrl += queryParameter;
                    log.info(requestUrl);
                    

                    var requestBuilder = HttpRequest.newBuilder();
                    requestBuilder.GET().uri(URI.create(requestUrl));

                    var request = requestBuilder.build();
                    var httpClient = this.createHttpClient();
                    var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

                    log.info("status code:" + response.statusCode());
                    
                    
                    
                }

            } catch (UnsupportedEncodingException e) {
                // TODO result...
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        
        // Loop 1: Varianten
        
            // Loop 2: Query parameter
                
                // 1. make request
                
                
                // 2. validate response code
                
                
                // 3. validate schema
                
                
                // 4. custom validations 
        
        
    }
    
    // mutterklasse
    private HttpClient createHttpClient() {
        var httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.ALWAYS)
                .build();
        return httpClient;
    }
    
    // mutterklasse?
    private void makeRequest() {
        
    }
    
    // mutterklasse?
    private void validateSchema(String schema, String result) {
        
    }
    
    // mutterklasse?
    private void validateResponseCode(String responseCode, String result) {
        
    }
    
    // Entfernt doppelte Slashes
    private String fixUrl(String url) {
        return url.replaceAll("(?<=[^:\\s])(\\/+\\/)", "/");
    }    
}
