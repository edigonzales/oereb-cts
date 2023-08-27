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

public abstract class Check {
    protected String name;
    protected String description;
    
    protected int countNodes(HttpResponse<Path> response, String expression) throws SaxonApiException {
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
        return count;
    }
}
