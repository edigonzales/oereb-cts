package ch.so.agi.oereb.cts;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class GetEGRIDTest {
    @Test
    public void foo() {
        var parameters = Map.of("EN","2600595,1215629","IDENTDN","SO0200002457","NUMBER","168");
        var getEgrid = new GetEGRID();
        getEgrid.run("https://geo.so.ch/api/oereb/", parameters);
        
    }
}
