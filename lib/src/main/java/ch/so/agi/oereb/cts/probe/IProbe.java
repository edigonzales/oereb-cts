package ch.so.agi.oereb.cts.probe;

import java.util.Map;

// TODO:
// Wozu brauche ich das alles? Ist das Meta-Beschreibung der Probe? Damit man ein GUI machen kann?
// Brauche ich das interface noch?
public interface IProbe {
    public void performRequest(String resourceUrl, Map<String,String> requestParameters /*, String requestTemplate*/);
    
//    public Map<String, String> getRequestHeaders();
//    
//    public String getRequestMethod();
//    
//    public String getRequestTemplate();
    
    public String getName();
    
    public String getDescription();
}
