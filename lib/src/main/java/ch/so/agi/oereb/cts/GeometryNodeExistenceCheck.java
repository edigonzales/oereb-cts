package ch.so.agi.oereb.cts;

import java.net.http.HttpResponse;
import java.nio.file.Path;

import net.sf.saxon.s9api.SaxonApiException;

// TODO
// Das reicht im Prinzip schon nicht ganz:
// Es sollte geprüft werden, ob alle Elemente, die eine Geometrie haben sollten, auch eine Geometrie haben.
public class GeometryNodeExistenceCheck extends Check implements ICheck {

    public GeometryNodeExistenceCheck() {
        super.name = this.getClass().getCanonicalName();
        super.description = "Checks if the returned xml document has geometry elements(s) or is not allowed to have geometry elements based on the request parameters.";
    }

    @Override
    public Result run(HttpResponse<Path> response) {
        Result result = new Result();
        result.setClassName(this.name);
        result.setDescription(this.description);
        result.start();
        
        String requestUrl = response.request().uri().toString();
        
        String expression = null;
        if (requestUrl.toLowerCase().contains("getegrid")) {
            expression = "count(//geom:coord)";
        } else {
            expression = "count(//data:RestrictionOnLandownership/data:Geometry/data:Surface/geom:exterior/geom:polyline/geom:coord)";
        }
        
        try {
            int count = countNodes(response, expression);
                        
            if (requestUrl.contains("GEOMETRY=true")) {
                if (count == 0) {
                    result.setSuccess(false);
                    result.setMessage("Response misses geometry element(s).");
                }
            } else if (requestUrl.contains("GEOMETRY=false") || !requestUrl.contains("GEOMETRY")) {
                if (count > 0) {
                    result.setSuccess(false);
                    result.setMessage("Response has superfluos geometry element(s).");
                }
            }     
        } catch (SaxonApiException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        } finally {
            result.stop();
        }
        return result;
    }

}
