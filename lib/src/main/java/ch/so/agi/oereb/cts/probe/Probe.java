package ch.so.agi.oereb.cts.probe;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Probe /*implements IProbe*/ {
    final Logger log = LoggerFactory.getLogger(Probe.class);
    
    protected String name;
    protected String description;

    // Array von templates? Damit man nur eine GetEGRID-Probe braucht?
    // -> performRequests -> loop performRequest
    // Ah nur für Kombinationen der weiteren Parameter möglich. Dh. dort 
    // wo kein Userinput nötig ist, wie weiss man sonst zu welchem Template
    // die request parameter gehören?
    // Ah und nein, geht auch nciht, weil ich ja z.B. nicht weiss welcher
    // request nun Geometrie haben muss (bei den Checks)
    protected String requestTemplate;
    
    public HttpRequest request;
    
    protected HttpResponse<?> response;
    
    public void performRequest(String resourceUrl, Map<String,String> requestParameters/*, String requestTemplate*/) {
        System.out.println("perform Request..." + this.name);
        
        var subsitutor = new StringSubstitutor(requestParameters);
        var resolvedRequestTemplate = subsitutor.replace(requestTemplate);
        var requestUrl = fixUrl(resourceUrl + "/" + resolvedRequestTemplate);
        
        var httpClient = HttpClient.newBuilder().version(Version.HTTP_1_1).followRedirects(Redirect.ALWAYS)
                .build();
        var requestBuilder = HttpRequest.newBuilder();
        requestBuilder.GET().uri(URI.create(requestUrl));

        request = requestBuilder.build();
        
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            
            
            HttpHeaders headers = response.headers();
            headers.map().forEach((k, v) -> System.out.println(k + ":" + v));

            
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // TODO;
        }
   
    }
   
    
    //User Schnittstelle
    // ProbeVars...
    
    public void run() {
        this.performRequest(description, null);
    }
    
    
//    @Override
//    public String getName() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public String getDescription() {
//        // TODO Auto-generated method stub
//        return null;
//    }
    
    // Entfernt doppelte Slashes
    public String fixUrl(String url) {
        return url.replaceAll("(?<=[^:\\s])(\\/+\\/)", "/");
    }
}
