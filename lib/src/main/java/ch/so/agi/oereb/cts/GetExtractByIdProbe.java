package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import net.sf.saxon.s9api.SaxonApiException;

public class GetExtractByIdProbe extends Probe implements IProbe {

    @Override
    public Result run(URI serviceEndpoint, URI requestUrl) throws IOException {
        File workFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), FOLDER_PREFIX).toFile();        

        Result probeResult = new Result();
        probeResult.setClassName(this.getClass().getCanonicalName());
        probeResult.setServiceEndpoint(serviceEndpoint);
        probeResult.setRequest(requestUrl);
        
        try {
            var response = this.makeRequest(workFolder, requestUrl, probeResult);
            probeResult.setResultFileLocation(response.body().toFile().getAbsolutePath());

            {
                var result = this.validateStatusCode(response);
                probeResult.addResult(result);
            } 
            
            if (!requestUrl.toString().contains("/url/")) {
                {
                    var result = this.validateResponseContentType(response);
                    probeResult.addResult(result);
                }
                
                {
                    // TODO: expression für ÖREB fix machen?
                    var result = this.validateSchema(response, "oereb_v2_0/Extract.xsd");
                    probeResult.addResult(result);
                }

                {
                    // Da im Check die RequestUrl bekannt ist, könnte man if/else für die Expression machen.
                    var result = this.validateGeometryNodesCount(response, "count(//data:RestrictionOnLandownership/data:Geometry/data:Surface/geom:exterior/geom:polyline/geom:coord)");
                    probeResult.addResult(result);
                } 
                
                {
                    var result = this.validateEmbeddedImages(response);
                    probeResult.addResult(result);
                }
                
                {
                    var result = this.validateFederalTopicCodesExistence(response);
                    probeResult.addResult(result);
                }
            }  
        } catch (InterruptedException e) { // TODO!!!
            e.printStackTrace();
        } catch (SaxonApiException e) { // TODO!!!
            e.printStackTrace();
        } 
        return probeResult;
    }

}
