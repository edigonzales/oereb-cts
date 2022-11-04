package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Validator {
    private static Logger log = LoggerFactory.getLogger(Main.class);

    XmlMapper xmlMapper;

    public Validator() {
        xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.setTimeZone(TimeZone.getTimeZone("Europe/Zurich"));
        xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
        xmlMapper.addMixIn(Result.class, ResultMixin.class);
    }

    public void run(String config, String outDirectory) throws InvalidFileFormatException, IOException, XMLStreamException {
        var configFile = new File(config);
        var ini = new Ini(configFile);
               
        log.info("Xml einmalig");
        var xof = XMLOutputFactory.newFactory();
        var xsw = xof.createXMLStreamWriter(new FileWriter(Paths.get(outDirectory, "result.xml").toFile()));
        xsw.writeStartDocument("utf-8", "1.0");
        xsw.writeStartElement("results");

        var iniSet = ini.entrySet();    // Exception    
        for (var sectionMap : iniSet) {  
            var results = new ArrayList<Result>();

            var section = sectionMap.getValue();
            var serviceEndpoint = section.get("SERVICE_ENDPOINT");
            
            if (serviceEndpoint == null) {
                throw new IllegalArgumentException("Service endpoint is missing.");
            }
            
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
            
            if (params.size() == 0) {
                throw new IllegalArgumentException("No test parameters defined.");
            }

            log.info("Validating service endpoint: " + serviceEndpoint + " ("+sectionMap.getKey()+")");
                        
            {
                // GetEGRID-Prüfungen
                var wrapper = new GetEGRIDWrapper();
                var probeResults = wrapper.run(serviceEndpoint, params);
                results.addAll(probeResults);
            }
            
            {
                // Extract-Prüfungen
                var wrapper = new GetExtractByIdWrapper();
                var probeResults = wrapper.run(serviceEndpoint, params);
                results.addAll(probeResults);
            }            

            for (Result result : results) {
                xmlMapper.writeValue(xsw, result);
                
                String fileName = new File(result.getResultFileLocation()).getName();
                Files.copy(Path.of(result.getResultFileLocation()), Path.of(outDirectory, fileName), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        
        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.flush();
        xsw.close();            
    }
    
    // xml2html
    
    
}
