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

public class GetExtractByIdWrapper {
    final Logger log = LoggerFactory.getLogger(GetEGRIDWrapper.class);

    private List<String> requestTemplates = List.of(
            "/extract/xml/?EGRID=${EGRID}",
            "/extract/xml/?IDENTDN=${IDENTDN}&NUMBER=${NUMBER}",
            "/extract/pdf/?EGRID=${EGRID}",
            "/extract/url/?EGRID=${EGRID}"
            );
    
    private List<String> queryParameters = List.of(
            "",
            "&GEOMETRY=true",
            "&GEOMETRY=false",
            "&WITHIMAGES=true",
            "&WITHIMAGES=false"
            );

    public List<Result> run(String serviceEndpoint, Map<String, String> parameters) {
        List<Result> resultList = new ArrayList<Result>();

        for (var requestTemplate : requestTemplates) {
            var subsitutor = new StringSubstitutor(parameters);
            subsitutor.setEnableUndefinedVariableException(true);
            String resolvedRequestTemplate = null;
            try {
                resolvedRequestTemplate = subsitutor.replace(requestTemplate);
            } catch (IllegalArgumentException e) {
                continue;
            }

            try {
                var requestUrlString = URLDecoder.decode(serviceEndpoint + "/" + resolvedRequestTemplate, StandardCharsets.UTF_8.name());
                requestUrlString = Utils.fixUrl(requestUrlString);
                
                for (var queryParameter : queryParameters) { 
                    var requestUrl = URI.create(requestUrlString + queryParameter);
                    
                    // Url- und pdf-Request dürfen nicht mit zusätzlichen Parametern geprüft werden.
                    if ((requestUrl.toString().contains("/url/") || requestUrl.toString().contains("/pdf/")) && (requestUrl.toString().contains("GEOMETRY") || requestUrl.toString().contains("WITHIMAGES"))) {
                        continue;
                    }

                    var probe = new GetExtractByIdProbe();
                    var probeResult = probe.run(requestUrl);
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
