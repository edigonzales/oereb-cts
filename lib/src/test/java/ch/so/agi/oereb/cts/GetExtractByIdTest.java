package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class GetExtractByIdTest {
    @Test
    public void foo() throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.setTimeZone(TimeZone.getTimeZone("Europe/Zurich"));
        xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        {
            //var parameters = Map.of("EN","2600595,1215629","IDENTDN","SO0200002457","NUMBER","168");
            var parameters = Map.of("EGRID","CH807306583219");
            var probe = new GetExtractByIdWrapper();
            List<Result> result = probe.run("https://geo.so.ch/api/oereb/", parameters);

            String resultXml = xmlMapper.writeValueAsString(result);
            System.out.println(resultXml);
        }
        
//        {
//            //var parameters = Map.of("EN","2694124,1180546","IDENTDN","UR0200001216","NUMBER","112");
//            var parameters = Map.of("EN","2694124,1180546");
//            var probe = new GetEGRID();
//            List<Result> result = probe.run("https://prozessor-oereb.ur.ch/oereb/", parameters);
//            
//            String resultXml = xmlMapper.writeValueAsString(result);
//            System.out.println(resultXml);
//        }
        
        
        
        
    }
}