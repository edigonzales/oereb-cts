package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.sf.saxon.s9api.SaxonApiException;

public class GetExtractByIdProbe extends Probe implements IProbe {

    @Override
    public Result run(URI requestUrl) throws IOException {
        File workFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), FOLDER_PREFIX).toFile();        

        Result probeResult = new Result();
        probeResult.setClassName(this.getClass().getCanonicalName());
        probeResult.setRequest(requestUrl);
        
        int idx = requestUrl.toString().indexOf("extract");
        String serviceEndpoint = requestUrl.toString().substring(0, idx);
        probeResult.setServiceEndpoint(URI.create(serviceEndpoint));

        try {
            HttpResponse<Path> response = this.makeRequest(workFolder, requestUrl, probeResult);
            probeResult.setResultFileLocation(response.body().toFile().getAbsolutePath());

            // Alle Requests
            {
                StatusCodeCheck check = new StatusCodeCheck();
                Result result = check.run(response);
                probeResult.addResult(result);
            } 
            
            // Nur XML- und PDF-Requests
            if (!requestUrl.toString().contains("/url/")) {
                {
                    ResponseContentTypeCheck check = new ResponseContentTypeCheck();
                    Result result = check.run(response);
                    probeResult.addResult(result);
                }
            }
            
            // Nur XML-Requests
            if (!(requestUrl.toString().contains("/url/") || requestUrl.toString().contains("/pdf/"))) { 
                {
                    SchemaCheck check = new SchemaCheck();
                    Result result = check.run(response);
                    probeResult.addResult(result);
                }
                {
                    GeometryNodeExistenceCheck check = new GeometryNodeExistenceCheck();
                    Result result = check.run(response);
                    probeResult.addResult(result);
                } 
                {
                    CoordSystemCheck check = new CoordSystemCheck();
                    Result result = check.run(response);
                    probeResult.addResult(result);
                }
                {
                    EmbeddedImagesCheck check = new EmbeddedImagesCheck();
                    Result result = check.run(response);
                    probeResult.addResult(result);
                }
                {
                    FederalTopicExistenceCheck check = new FederalTopicExistenceCheck();
                    Result result = check.run(response);
                    probeResult.addResult(result);
                }
            }  
            
            // Nur PDF-Requests
            if (requestUrl.toString().contains("/pdf/")) {
                {
                    PdfFormatCheck check = new PdfFormatCheck();
                    Result result = check.run(response);
                    probeResult.addResult(result);   
                }
            }
        } catch (InterruptedException e) { // TODO!!!
            e.printStackTrace();
        }  
        return probeResult;
    }

}
