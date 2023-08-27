package ch.so.agi.oereb.cts;

import java.net.http.HttpResponse;
import java.nio.file.Path;

public class ResponseContentTypeCheck extends Check implements ICheck {

    public ResponseContentTypeCheck() {
        super.name = this.getClass().getCanonicalName();
        super.description = "Checks the content type header of the response.";
    }

    @Override
    public Result run(HttpResponse<Path> response) {
        Result result = new Result();
        result.setClassName(this.name);
        result.setDescription(this.description);
        result.start();
        
        String requestUrl = response.request().uri().toString();
        try {
            String contentType = response.headers().firstValue("content-type").orElseThrow(()-> {
                return new Exception("Content-Type header not present.");
            });
                        
            if (requestUrl.contains("/pdf/")) {
                if (contentType.equalsIgnoreCase("application/pdf")) {
                    result.setSuccess(true);
                } else {
                    result.setSuccess(false);
                    result.setMessage("Response Content-Type must be 'application/pdf'. Response Content-Type is '" + contentType + "'.");
                }
            } else if (requestUrl.contains("/xml/") || (requestUrl.contains("/capabilities/xml") || requestUrl.contains("/versions/xml"))) {
                if (contentType.equalsIgnoreCase("application/xml") || contentType.toLowerCase().replace(" ", "").equalsIgnoreCase("application/xml;charset=utf-8")) {
                    result.setSuccess(true);
                } else {
                    result.setSuccess(false);
                    result.setMessage("Response Content-Type must be 'application/xml' (or application/xml; charset=utf-8). Response Content-Type is '" + contentType + "'.");
                }
            } else {
                result.setSuccess(false);
                result.setMessage("Response Content-Type '" + contentType + "' is not allowed.");
            }       
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        } finally {
            result.stop();
        }
        return result;
    }
}
