package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.SaxonApiException;

public class GetEGRIDProbe extends Probe implements IProbe {
    final Logger log = LoggerFactory.getLogger(GetEGRIDWrapper.class);

    @Override
    public Result run(URI serviceEndpoint, URI requestUrl) throws IOException {
        var workFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), FOLDER_PREFIX).toFile();        

        var probeResult = new Result();
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

            {
                var result = this.validateResponseContentType(response);
                probeResult.addResult(result);
            } 
            
            {
                var result = this.validateSchema(response, "oereb_v2_0/Extract.xsd");
                probeResult.addResult(result);
            }

            {
                // Prüfen, ob Geometrie vorhanden ist resp. nicht vorhanden sein darf.
                var result = this.validateGeometryNodesCount(response, "count(//geom:coord)");
                probeResult.addResult(result);
            } 
        } catch (InterruptedException e) { // TODO!!!
            e.printStackTrace();
        } catch (SaxonApiException e) { // TODO!!!
            e.printStackTrace();
        }
        return probeResult;
    }
}
