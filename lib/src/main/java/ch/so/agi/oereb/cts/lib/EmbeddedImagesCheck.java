package ch.so.agi.oereb.cts.lib;

import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXParseException;

import net.sf.saxon.s9api.SaxonApiException;

public class EmbeddedImagesCheck extends Check implements ICheck {
    
    public EmbeddedImagesCheck() {
        super.name = this.getClass().getCanonicalName();
        super.description = "Checks if the returned xml document has embedded images or is not allowed to have embedded images and must have references to images.";
    }

    @Override
    public Result run(HttpResponse<Path> response) {
        Result result = new Result();
        result.setClassName(this.name);
        result.setDescription(this.description);
        result.start();

        var messages = new ArrayList<String>();
        
        String requestUrl = response.request().uri().toString();

        var logoList = List.of("LogoPLRCadastre", "FederalLogo", "CantonalLogo", "MunicipalityLogo");
        for (var logo : logoList) {
            {
                try {
                    int count = countNodes(response, "count(//data:"+logo+"/text())");

                    if (requestUrl.contains("WITHIMAGES=true")) {
                        if (count == 0) {
                            result.setSuccess(false);
                            result.setMessage("Response misses "+logo+" element.");
                        }
                    } else if (requestUrl.contains("WITHIMAGES=false") || !requestUrl.contains("WITHIMAGES")) {
                        if (count > 0) {
                            result.setSuccess(false);
                            result.setMessage("Response has a superfluos "+logo+" element.");
                        }
                    }
                } catch (SaxonApiException e) {
                    result.setSuccess(false);
                    messages.add(e.getMessage());
                }
            }
            
            {
                try {
                    int count = countNodes(response, "count(//data:"+logo+"Ref/text())");

                    if (requestUrl.contains("WITHIMAGES=true")) {
                        // Glaube diese Bedingung gilt nicht.
//                        if (count > 0) {
//                            result.setSuccess(false);
//                            result.setMessage("Response has a superfluos "+logo+"Ref element.");
//                        }
                    } else if (requestUrl.contains("WITHIMAGES=false") || !requestUrl.contains("WITHIMAGES")) {
                        if (count == 0) {
                            result.setSuccess(false);
                            result.setMessage("Response misses "+logo+"Ref element.");
                        }
                    }
                } catch (SaxonApiException e) {
                    result.setSuccess(false);
                    messages.add(e.getMessage());
                }
            }
        }
        
        {
            try {
                int count = countNodes(response, "count(//data:RestrictionOnLandownership/data:Map/data:Image/data:LocalisedBlob/data:Blob/text())");
                
                if (requestUrl.contains("WITHIMAGES=true")) {
                    if (count == 0) {
                        result.setSuccess(false);
                        result.setMessage("Response misses image blob element.");
                    }
                } else if (requestUrl.contains("WITHIMAGES=false") || !requestUrl.contains("WITHIMAGES")) {
                    if (count > 0) {
                        result.setSuccess(false);
                        result.setMessage("Response has a superfluos image blob element.");
                    }
                }
            } catch (SaxonApiException e) {
                result.setSuccess(false);
                messages.add(e.getMessage());
            }
        }

        String errorMessage = new String();
        for(String message : messages) {
            errorMessage += message + "\n";
        }
        result.setMessage(errorMessage);

        result.stop();
        return result;        
    }
}
