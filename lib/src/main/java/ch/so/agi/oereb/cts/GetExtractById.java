package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetExtractById extends Probe implements IProbe {
    final Logger log = LoggerFactory.getLogger(GetEGRID.class);
    
    private static String FOLDER_PREFIX = "oerebcts";

    private List<String> requestTemplates = List.of(
            "/extract/xml/?EGRID=${EGRID}",
            "/extract/xml/?IDENTDN=${IDENTDN}&NUMBER=${NUMBER}" 
            );
    
    private List<String> queryParameters = List.of(
            "",
            "&GEOMETRY=true",
            "&GEOMETRY=false",
            "&WITHIMAGES=true",
            "&WITHIMAGES=false"
            );

    @Override
    public List<Result> run(String serviceEndpoint, Map<String, String> parameters) throws IOException {
        File workFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), FOLDER_PREFIX).toFile();        
        List<Result> resultList = new ArrayList<Result>();

        for (var requestTemplate : requestTemplates) {
            var subsitutor = new StringSubstitutor(parameters);
            subsitutor.setEnableUndefinedVariableException(true);
            String resolvedRequestTemplate = null;
            try {
                resolvedRequestTemplate = subsitutor.replace(requestTemplate);
            } catch (IllegalArgumentException e) {
                // Es wurden keine passenden Parameter gefunden, um die Platzhalter zu ersetzen.
                // In diesem Fall soll/kann die Prüfung nicht durchgeführt werden.
                continue;
            }
            log.info(resolvedRequestTemplate);

            var requestUrlString = URLDecoder.decode(serviceEndpoint + "/" + resolvedRequestTemplate, StandardCharsets.UTF_8.name());
            requestUrlString = fixUrl(requestUrlString);
            
            for (var queryParameter : queryParameters) {
                Result probeResult = new Result();
                probeResult.setClassName(this.getClass().getCanonicalName());
                probeResult.setServiceEndpoint(serviceEndpoint);

                var requestUrl = requestUrlString + queryParameter;
                probeResult.setRequest(requestUrl);

                log.info(requestUrl);
            }
        }
        
        
        return null;
        
        // check: content-type der http-response
    }

}
