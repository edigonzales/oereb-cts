package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Den Wrapper bräuchte es eigentlich nicht, da es keine Kombinationen von Requests gibt.
public class GetCapabilitiesMethod {
    final Logger log = LoggerFactory.getLogger(GetCapabilitiesMethod.class);

    public List<Result> run(String serviceEndpoint, Map<String, String> parameters) {
        List<Result> resultList = new ArrayList<Result>();

        try {
            String requestUrlString = URLDecoder.decode(serviceEndpoint + "/capabilities/xml", StandardCharsets.UTF_8.name());
            requestUrlString = Utils.fixUrl(requestUrlString);
            
            URI requestUrl = URI.create(requestUrlString);
            
            GetCapabilitiesProbe probe = new GetCapabilitiesProbe();
            Result probeResult = probe.run(requestUrl);
            probeResult.setIdentifier(parameters.get("identifier"));

            resultList.add(probeResult);
        } catch (UnsupportedEncodingException e) {
            // TODO result...
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 

        return resultList;
    }
}
