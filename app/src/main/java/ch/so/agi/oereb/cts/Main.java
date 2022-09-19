package ch.so.agi.oereb.cts;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Main {
    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        String config = null;
        String outDirectory = null;
        
        int argi = 0;
        for(;argi<args.length;argi++) {
            String arg = args[argi];
            
            if(arg.equals("--config")) {
                argi++;
                config = args[argi];
            } else if (arg.equals("--out")) {
                argi++;
                outDirectory = args[argi];
            } else if (arg.equals("--help")) {
                System.err.println();
                System.err.println("--config     The ini file with test configuration (required).");
                System.err.println("--out        Output directory where the result file(s) are written.");
                System.err.println();
                return;
            }
        }
        
        if (config == null) {
            System.err.println("config is required.");
            System.exit(2);
        }        
        
        
        /////// Validator-Klasse
        var xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.setTimeZone(TimeZone.getTimeZone("Europe/Zurich"));
        xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        var configFile = new File(config);
        var ini = new Ini(configFile);
       
        var iniSet = ini.entrySet();        
        for (var sectionMap : iniSet) {            
            var section = sectionMap.getValue();
            var serviceEndpoint = section.get("SERVICE_ENDPOINT");
            
            // TODO:
            // FAlls serviceEndpoint == null -> Übungsabbruch...
            
            var params = new HashMap<String,String>();
            if (section.containsKey("EN")) {
                params.put("EN", section.get("EN"));
            }
            if (section.containsKey("IDENTDN")) {
                params.put("IDENTDN", section.get("IDENTDN"));
            }
            if (section.containsKey("NUMBER")) {
                params.put("NUMBER", section.get("NUMBER"));
            }
            if (section.containsKey("EGRID")) {
                params.put("EGRID", section.get("EGRID"));
            }
            log.info(params.toString());
            
            
            var wrapper = new GetEGRIDWrapper();
            List<Result> results = wrapper.run(serviceEndpoint, params);
            log.info(results.toString());
            
            // Mixin
            // Streamen mit einem neuen Root-Element (sieh metabean2file).
            for (Result result : results) {
                var resultXml = xmlMapper.writeValueAsString(result);
                System.out.println(resultXml);
            }

        }
        
        
//        var it = iniSet.iterator();
//        while(it.hasNext()) {
//           // log.info(it.getClass().toString());
//        }
        
        

        
        
        
    }
}