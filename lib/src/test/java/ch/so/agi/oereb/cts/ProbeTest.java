package ch.so.agi.oereb.cts;

import java.util.Map;

import org.junit.jupiter.api.Test;

import ch.so.agi.oereb.cts.probe.GetEGRIDByEN;
import ch.so.agi.oereb.cts.probe.GetEGRIDByENWithGeometry;

public class ProbeTest {
    @Test
    public void fubar() {        
        {
            var probe = new GetEGRIDByEN();
            probe.performRequest("https://geo.so.ch/api/oereb/", Map.of("EN", "2600599,1215639"));
        }
        {
            var probe = new GetEGRIDByENWithGeometry();
            probe.performRequest("https://geo.so.ch/api/oereb/", Map.of("EN", "2600599,1215639"));
        }

    }
}
