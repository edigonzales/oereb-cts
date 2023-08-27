package ch.so.agi.oereb.cts.lib;

import java.net.http.HttpResponse;
import java.nio.file.Path;

// Anhand des Requests (d.h. ohne User-Input) kann nicht abschliessend geprüft werden, ob der Status-Code
// korrekt ist. Aus diesem Grund muss er immer auch vom Menschen überprüft werden. Er kann nur drei Werte 
// annehmen. Falls der Status-Code nicht einem dieser Werte entspricht, wird der Check
// als nicht erfüllt dokumentiert.
public class StatusCodeCheck extends Check implements ICheck {

    public StatusCodeCheck() {
        super.name = this.getClass().getCanonicalName();
        super.description = "Checks the returned http status code.";
    }

    @Override
    public Result run(HttpResponse<Path> response) {
        Result result = new Result();
        result.setClassName(this.name);
        result.setDescription(this.description);
        result.start();
        
        int statusCode = response.statusCode();
        result.setStatusCode(statusCode);

        String requestUrl = response.request().uri().toString();
        if (requestUrl.toLowerCase().contains("url") && statusCode != 303) {
            result.setSuccess(false);
            result.setMessage("Returned status '"+String.valueOf(statusCode)+"' code does not match expected status code (303).");
        } else if (statusCode != 200 && statusCode != 204 && statusCode != 500 && !requestUrl.toLowerCase().contains("url")) {
            result.setSuccess(false);
            result.setMessage("Returned status '"+String.valueOf(statusCode)+"' code does not match expected status code (200, 204, 303, 500).");
        }

        result.stop();
        return result;    
    }
}
