package ch.so.agi.oereb.cts.probe;

import java.util.Map;

public class GetEGRIDByENWithGeometry extends Probe {

    private String requestTemplate = "/getegrid/xml/?GEOMETRY=true&EN=${EN}";
    
    public GetEGRIDByENWithGeometry() {
        super();
        super.requestTemplate = this.requestTemplate;
        
        super.name = "GetEGRID by EN with Geometry";
    }
    
//    @Override
//    public Map<String, String> getRequestHeaders() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public String getRequestMethod() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public String getRequestTemplate() {
//        // TODO Auto-generated method stub
//        return null;
//    }
}
