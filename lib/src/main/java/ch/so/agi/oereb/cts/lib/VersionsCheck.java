package ch.so.agi.oereb.cts.lib;

import java.net.http.HttpResponse;
import java.nio.file.Path;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

public class VersionsCheck extends Check implements ICheck {

    public VersionsCheck() {
        super.name = this.getClass().getCanonicalName();
        super.description = "Checks if GetVersionsResponse version element contains 'extract-2.0'.";
    }
    
    @Override
    public Result run(HttpResponse<Path> response) {        
        Result result = new Result();
        result.setClassName(this.name);
        result.setDescription(this.description);
        result.start();

        String expression = "//versioning:version/text()[1]";

        try {
            Processor proc = new Processor(false);
            XPathCompiler xpath = proc.newXPathCompiler();
            xpath.declareNamespace("geom", "http://www.interlis.ch/geometry/1.0");
            xpath.declareNamespace("extract", "http://schemas.geo.admin.ch/V_D/OeREB/2.0/Extract");
            xpath.declareNamespace("data", "http://schemas.geo.admin.ch/V_D/OeREB/2.0/ExtractData");
            xpath.declareNamespace("versioning", "http://schemas.geo.admin.ch/V_D/OeREB/1.0/Versioning");

            DocumentBuilder builder = proc.newDocumentBuilder();
            builder.setLineNumbering(true);
            builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
            XdmNode responseDoc = builder.build(response.body().toFile());

            XPathSelector selector = xpath.compile(expression).load();
            selector.setContextItem(responseDoc);
            
            XdmItem versionItemText = selector.evaluateSingle();
            if (versionItemText == null) {
                result.setSuccess(false);
                result.setMessage("No version element found.");
                result.stop();
                return result;
            }
            
            String versionTxt = versionItemText.getStringValue();
            
            if (!versionTxt.equals("extract-2.0")) {
                result.setSuccess(false);
                result.setMessage("Text of version element (='"+versionTxt+"') is not valid ('=extract-2.0')");
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
