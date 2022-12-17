package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                var check = new StatusCodeCheck();
                var result = check.run(response);
                probeResult.addResult(result);
            } 

            {
                var check = new ResponseContentTypeCheck();
                var result = check.run(response);
                probeResult.addResult(result);
            } 
            
            {
                var check = new SchemaCheck();
                var result = check.run(response);
                probeResult.addResult(result);
            }

            {
                var check = new GeometryNodeExistenceCheck();
                var result = check.run(response);
                probeResult.addResult(result);
            } 
            {
                var check = new CoordSystemCheck();
                var result = check.run(response);
                probeResult.addResult(result);
            }
        } catch (InterruptedException e) { // TODO!!!
            e.printStackTrace();
        } 
        return probeResult;
    }
}
