package ch.so.agi.oereb.cts.lib;

import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

public class FederalTopicExistenceCheck extends Check implements ICheck {
    
    public FederalTopicExistenceCheck() {
        super.name = this.getClass().getCanonicalName();
        super.description = "Checks if the returned xml document has all federal topics in the themes table of contents.";
    }

    @Override
    public Result run(HttpResponse<Path> response) {
        Result result = new Result();
        result.setClassName(this.name);
        result.setDescription(this.description);
        result.start();

        Processor proc = new Processor(false);
        XPathCompiler xpath = proc.newXPathCompiler();
        xpath.declareNamespace("geom", "http://www.interlis.ch/geometry/1.0");
        xpath.declareNamespace("extract", "http://schemas.geo.admin.ch/V_D/OeREB/2.0/Extract");
        xpath.declareNamespace("data", "http://schemas.geo.admin.ch/V_D/OeREB/2.0/ExtractData");

        try {
            DocumentBuilder builder = proc.newDocumentBuilder();
            builder.setLineNumbering(true);
            builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
            XdmNode responseDoc = builder.build(response.body().toFile());

            XPathSelector selector = xpath.compile("//data:Code[parent::data:ConcernedTheme|parent::data:NotConcernedTheme|parent::data:ThemeWithoutData]/text()").load();
            selector.setContextItem(responseDoc);
            
            var themeCodesMap = new HashMap<String,String>();
            for (XdmItem item: selector) {
                XdmNode code = (XdmNode)item;
                themeCodesMap.put(code.getStringValue(), code.getStringValue());
            }
            
            var federalThemeCodesList = List.of("ch.Nutzungsplanung","ch.ProjektierungszonenNationalstrassen","ch.BaulinienNationalstrassen","ch.ProjektierungszonenEisenbahnanlagen",
                    "ch.BaulinienEisenbahnanlagen","ch.ProjektierungszonenFlughafenanlagen","ch.BaulinienFlughafenanlagen","ch.Sicherheitszonenplan",
                    "ch.BelasteteStandorte","ch.BelasteteStandorteMilitaer","ch.BelasteteStandorteZivileFlugplaetze","ch.BelasteteStandorteOeffentlicherVerkehr",
                    "ch.Grundwasserschutzzonen","ch.Grundwasserschutzareale","ch.Laermempfindlichkeitsstufen","ch.StatischeWaldgrenzen",
                    "ch.Waldabstandslinien","ch.Planungszonen","ch.Waldreservate","ch.Gewaesserraum","ch.ProjektierungszonenStarkstromanlagen","ch.BaulinienStarkstromanlagen");

            var missingFederalThemeCodes = new ArrayList<String>();
            for (var code : federalThemeCodesList) {
                if (!themeCodesMap.containsKey(code)) {
                    missingFederalThemeCodes.add(code);
                }
            }
            
            if (missingFederalThemeCodes.size() > 0) {
                result.setSuccess(false);
                result.setMessage("Missing federal theme codes in table of contents: " + String.join(", ", missingFederalThemeCodes));
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
