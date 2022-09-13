package ch.so.agi.oereb.cts;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
            var resolvedRequestTemplate = subsitutor.replace(requestTemplate);
            log.info(resolvedRequestTemplate);
            
            try {
                var requestUrl = URLDecoder.decode(serviceEndpoint + "/" + resolvedRequestTemplate, StandardCharsets.UTF_8.name());
                requestUrl = fixUrl(requestUrl);
                
                for (var queryParameter : queryParameters) {
                    requestUrl += queryParameter;
                    log.info(requestUrl);
                    
                    
                    
                    
                    
                }
                
                
                
                
                

            } catch (UnsupportedEncodingException e) {
                // TODO result...
            }
 

            
            
            
        }
        
        // Loop 1: Varianten
        
            // Loop 2: Query parameter
                
                // 1. make request
                
                
                // 2. validate response code
                
                
                // 3. validate schema
                
                
                // 4. custom validations 
        
        
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
