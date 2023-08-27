package ch.so.agi.oereb.cts.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.profiles.RuleId;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;


public class PdfFormatCheck extends Check implements ICheck {

    public PdfFormatCheck() {
        super.name = this.getClass().getCanonicalName();
        //super.description = "Validates PDF/A conformance levels (PDF/A-1a or PDF/A2-a).";
        super.description = "Validates PDF/A conformance level. Conformance level is autodetected.";
    }

    @Override
    public Result run(HttpResponse<Path> response) {        
        Result result = new Result();
        result.setClassName(this.name);
        result.setDescription(this.description);
        result.start();

        VeraGreenfieldFoundryProvider.initialise();
        
        File pdfFile = response.body().toFile();
        
        try (PDFAParser parser = Foundries.defaultInstance().createParser(new FileInputStream(pdfFile))) {            
            PDFAValidator validator = Foundries.defaultInstance().createValidator(parser.getFlavour(), false);
            ValidationResult res = validator.validate(parser);
                        
            if (res.isCompliant()) {
                result.setSuccess(true);
                result.stop();
                return result;
            } else {
                result.setSuccess(false);
                                
                String errorMessage = "PDF/A flavour: " + res.getPDFAFlavour().toString() + "\n\n";
                for (Map.Entry<RuleId, Integer> entry : res.getFailedChecks().entrySet()) {
                    errorMessage += entry.getKey().toString() + "\n";
                }
                                
                result.setMessage(errorMessage);
            }
        } catch (ModelParsingException | EncryptedPdfException | IOException | ValidationException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            return result;  
        } 
        
        result.stop();
        return result;
    }

}
