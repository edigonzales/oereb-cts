package ch.so.agi.oereb.cts;

import java.net.http.HttpResponse;
import java.nio.file.Path;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

public class CoordSystemCheck extends Check implements ICheck {

    public CoordSystemCheck() {
        super.name = this.getClass().getCanonicalName();
        super.description = "Checks if LV95 coordinates are used in geometry elements.";
    }
    
    @Override
    public Result run(HttpResponse<Path> response) {        
        Result result = new Result();
        result.setClassName(this.name);
        result.setDescription(this.description);
        result.start();

        String expression = "count(//geom:coord[./geom:c1 < 2400000 or ./geom:c1 > 2900000 or ./geom:c2 < 1070000 or ./geom:c2 > 1300000])";

        try {
            Processor proc = new Processor(false);
            XPathCompiler xpath = proc.newXPathCompiler();
            xpath.declareNamespace("geom", "http://www.interlis.ch/geometry/1.0");
            xpath.declareNamespace("extract", "http://schemas.geo.admin.ch/V_D/OeREB/2.0/Extract");
            xpath.declareNamespace("data", "http://schemas.geo.admin.ch/V_D/OeREB/2.0/ExtractData");

            DocumentBuilder builder = proc.newDocumentBuilder();
            builder.setLineNumbering(true);
            builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
            XdmNode responseDoc = builder.build(response.body().toFile());

            XPathSelector selector = xpath.compile(expression).load();
            selector.setContextItem(responseDoc);
            
            int count = Integer.valueOf(selector.evaluateSingle().getStringValue());
            
            if (count > 0) {
                result.setSuccess(false);
                result.setMessage("Coordinates are not in valid LV95 range.");
            } else {
                result.setSuccess(true);
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
