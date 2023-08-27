package ch.so.agi.oereb.cts.lib;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetVersionsProbe extends Probe implements IProbe {
    final Logger log = LoggerFactory.getLogger(GetVersionsProbe.class);

    @Override
    public Result run(URI requestUrl) throws IOException {
        File workFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), FOLDER_PREFIX).toFile();        

        Result probeResult = new Result();
        probeResult.setClassName(this.getClass().getCanonicalName());
        probeResult.setRequest(requestUrl);
        
        // Wenn man probeResult als Parameter übergibt, kann man den ServiceEnpoint ausserhalb bereits
        // setzen.
        int idx = requestUrl.toString().indexOf("versions");
        String serviceEndpoint = requestUrl.toString().substring(0, idx);
        probeResult.setServiceEndpoint(URI.create(serviceEndpoint));

        try {
            HttpResponse<Path> response = this.makeRequest(workFolder, requestUrl, probeResult);
            probeResult.setResultFileLocation(response.body().toFile().getAbsolutePath());

            {
                StatusCodeCheck check = new StatusCodeCheck();
                Result result = check.run(response);
                probeResult.addResult(result);
            } 
            {
                ResponseContentTypeCheck check = new ResponseContentTypeCheck();
                Result result = check.run(response);
                probeResult.addResult(result);
            } 
            {
                SchemaCheck check = new SchemaCheck();
                Result result = check.run(response);
                probeResult.addResult(result);
            }
            {
                VersionsCheck check = new VersionsCheck();
                Result result = check.run(response);
                probeResult.addResult(result);
            }

        } catch (InterruptedException e) { // TODO!!!
            e.printStackTrace();
        } 
        return probeResult;
    }

}
