package ch.so.agi.oereb.cts.probe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetEGRID /* extends? implements? */ {
    final Logger log = LoggerFactory.getLogger(GetEGRID.class);

    private List<String> requestTemplate = List.of(
            "/getegrid/xml/?EN=${EN}",
            "/getegrid/xml/?IDENTDN=${IDENTDN}&NUMBER=${NUMBER}" 
            );
    
    private String result;
    
    // Result Rückgabewert?
    public void run(String serviceEndpoint, Map<String,String> parameters) {
        
        
        
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
    
}
