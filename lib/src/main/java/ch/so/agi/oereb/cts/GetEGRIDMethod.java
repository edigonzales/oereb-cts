package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetEGRIDMethod {
    final Logger log = LoggerFactory.getLogger(GetEGRIDMethod.class);

    private List<String> requestTemplates = List.of(
            "/getegrid/xml/?EN=${EN}",
            "/getegrid/xml/?IDENTDN=${IDENTDN}&NUMBER=${NUMBER}"
            );
    
    private List<String> queryParameters = List.of(
            "",
            "&GEOMETRY=true",
            "&GEOMETRY=false"
            );
        
    public List<Result> run(String serviceEndpoint, Map<String,String> parameters) {
        List<Result> resultList = new ArrayList<Result>();

        for (String requestTemplate : requestTemplates) {
            StringSubstitutor subsitutor = new StringSubstitutor(parameters);
            subsitutor.setEnableUndefinedVariableException(true);
            String resolvedRequestTemplate = null;
            try {
                resolvedRequestTemplate = subsitutor.replace(requestTemplate);
            } catch (IllegalArgumentException e) {
                // Es wurden keine passenden Parameter gefunden, um die Platzhalter zu ersetzen.
                // In diesem Fall soll/kann die Prüfung nicht durchgeführt werden.
                continue;
            }
            
            try {
                String requestUrlString = URLDecoder.decode(serviceEndpoint + "/" + resolvedRequestTemplate, StandardCharsets.UTF_8.name());
                requestUrlString = Utils.fixUrl(requestUrlString);
                
                for (String queryParameter : queryParameters) { 
                    URI requestUrl = URI.create(requestUrlString + queryParameter);

                    log.debug("request url: {}", requestUrl);

                    GetEGRIDProbe probe = new GetEGRIDProbe();
                    Result probeResult = probe.run(requestUrl);
                    probeResult.setIdentifier(parameters.get("identifier"));
                    
                    resultList.add(probeResult);
                }
            } catch (UnsupportedEncodingException e) {
                // TODO result...
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
        return resultList;
    }    
}
