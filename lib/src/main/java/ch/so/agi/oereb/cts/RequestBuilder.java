package ch.so.agi.oereb.cts;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

/*
 * Erzeugt alle möglichen Request anhand der Parameter.
 */
public class RequestBuilder {
    private static final List<String> REQUEST_TEMPLATES = List.of(
            "/versions/xml",
            "/capabilities/xml",
            "/getegrid/xml/?EN=${EN}",
            "/getegrid/xml/?IDENTDN=${IDENTDN}&NUMBER=${NUMBER}",
            "/extract/xml/?EGRID=${EGRID}",
            "/extract/xml/?IDENTDN=${IDENTDN}&NUMBER=${NUMBER}",
            "/extract/pdf/?EGRID=${EGRID}",
            "/extract/url/?EGRID=${EGRID}"
            );

    private static final List<String> GET_EGRID_QUERY_PARAMETERS = List.of(
            "",
            "&GEOMETRY=true",
            "&GEOMETRY=false"
            );
    
    private static final List<String> EXTRACT_QUERY_PARAMETERS = List.of(
            "",
            "&GEOMETRY=true",
            "&GEOMETRY=false",
            "&WITHIMAGES=true",
            "&WITHIMAGES=false"
            );

    
    public static List<String> build(Map<String,String> params) {
        List<String> requestUrls = new ArrayList<>();
        
        for (String requestTemplate : REQUEST_TEMPLATES) {            
            StringSubstitutor subsitutor = new StringSubstitutor(params);
            subsitutor.setEnableUndefinedVariableException(true);
            String resolvedRequestTemplate = null;
            try {
                resolvedRequestTemplate = subsitutor.replace(requestTemplate);
            } catch (IllegalArgumentException e) {
                // Es wurden keine passenden Parameter gefunden, um die Platzhalter zu ersetzen.
                // In diesem Fall soll/kann die Prüfung nicht durchgeführt werden.
                continue;
            }

            List<String> queryParameters = List.of();
            if (resolvedRequestTemplate.contains("/getegrid/")) {
                queryParameters = GET_EGRID_QUERY_PARAMETERS;
            } else if (resolvedRequestTemplate.contains("/extract/")) {
                queryParameters = EXTRACT_QUERY_PARAMETERS;
            } else {
                // Liste mit einem leeren String initialisieren, damit die folgende Forschleife auch
                // für Requests ohne Query Parameter durchlaufen wird.
                queryParameters = List.of("");
            }
            
            for (String queryParameter : queryParameters) {
                String requestUrl = resolvedRequestTemplate + queryParameter;
                
                // "url" und "pdf"-Requests dürfen keine Query Parameter erhalten resp. die Requests werden 
                // ignoriert.
                if ((requestUrl.contains("/url/") || requestUrl.toString().contains("/pdf/")) && (requestUrl.contains("GEOMETRY") || requestUrl.contains("WITHIMAGES"))) {
                    continue;
                }

                requestUrl = params.get("SERVICE_ENDPOINT") + requestUrl;                
                requestUrls.add(fixUrl(requestUrl));
            }
        }
        
        return requestUrls;
    }
    
    
    // Entfernt doppelte Slashes
    public static String fixUrl(String url) {
        return url.replaceAll("(?<=[^:\\s])(\\/+\\/)", "/");
    }
}
