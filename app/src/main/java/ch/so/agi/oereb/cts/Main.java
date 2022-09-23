package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

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
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
        //xmlMapper.disable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.addMixIn(Result.class, ResultMixin.class);

        var configFile = new File(config);
        var ini = new Ini(configFile);
       
        var iniSet = ini.entrySet();    // Exception    
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

            var xof = XMLOutputFactory.newFactory();
//            try { // Exception
                var xsw = xof.createXMLStreamWriter(new FileWriter("/Users/stefan/tmp/fubar.xml"));
                xsw.writeStartDocument("utf-8", "1.0");
                xsw.writeStartElement("results");

                for (Result result : results) {
                    xmlMapper.writeValue(xsw, result);
                    
                    String fileName = new File(result.getResultFileLocation()).getName();
                    Files.copy(Path.of(result.getResultFileLocation()), Path.of(outDirectory, fileName), StandardCopyOption.REPLACE_EXISTING);
                }

                xsw.writeEndElement();
                xsw.writeEndDocument();
                xsw.flush();
                xsw.close();

//            } catch (Exception e) { // TODO
//                e.printStackTrace();
//                log.error(e.getMessage());
//                log.error(e.getClass().toString());
//                //throw new Meta2FileException(e.getMessage());
//            }
            
//            for (Result result : results) {
//                var resultXml = xmlMapper.writeValueAsString(result);
//                System.out.println(resultXml);
//            }

        }
        
        
//        var it = iniSet.iterator();
//        while(it.hasNext()) {
//           // log.info(it.getClass().toString());
//        }
        
        

        
        
        
    }
}