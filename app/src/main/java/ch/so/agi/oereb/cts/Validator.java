package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
import javax.xml.transform.stream.StreamSource;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class Validator {
    private static Logger log = LoggerFactory.getLogger(Main.class);

    XmlMapper xmlMapper;
    
    @Deprecated
    HashMap<String, Boolean> summaryMap;
    
    HashMap<String, List<Result>> resultsMap = new HashMap<>();

    public Validator() {
        xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.setTimeZone(TimeZone.getTimeZone("Europe/Zurich"));
        xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
        xmlMapper.addMixIn(Result.class, ResultMixin.class);
    }

    public void run(String config, String outDirectory) throws InvalidFileFormatException, IOException, XMLStreamException, SaxonApiException {
        var configFile = new File(config);
        var ini = new Ini(configFile);

        var iniSet = ini.entrySet();    // Exception    
        for (var sectionMap : iniSet) {  
            //log.info(sectionMap.getKey());
            
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

            resultsMap.put(sectionMap.getKey(), results);            
        }
        
        // TODO: result.xml pro sectionMap.getKey 
        var resultXmlFile = Paths.get(outDirectory, "result.xml").toFile();
        var xof = XMLOutputFactory.newFactory();
        var xsw = xof.createXMLStreamWriter(new FileWriter(resultXmlFile));
        xsw.writeStartDocument("utf-8", "1.0");
        xsw.writeStartElement("results");

        for (var entry : resultsMap.entrySet()) {
            for (Result result : entry.getValue()) {
                xmlMapper.writeValue(xsw, result);
    
                String fileName = new File(result.getResultFileLocation()).getName();
                Files.copy(Path.of(result.getResultFileLocation()), Path.of(outDirectory, fileName), StandardCopyOption.REPLACE_EXISTING);            
            }
        }
        
        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.flush();
        xsw.close();        
        
        var resultHtmlFile = Paths.get(outDirectory, "result.html").toFile();
        
        String XML2PDF_XSL = "xml2html.xsl";
        File xsltFile = new File(Paths.get(outDirectory, XML2PDF_XSL).toFile().getAbsolutePath());
        InputStream xsltFileInputStream = Validator.class.getResourceAsStream("/"+XML2PDF_XSL); 
        Files.copy(xsltFileInputStream, xsltFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        xsltFileInputStream.close();

        Processor proc = new Processor(false);
        XsltCompiler comp = proc.newXsltCompiler();
        XsltExecutable exp = comp.compile(new StreamSource(xsltFile));
        
        XdmNode source = proc.newDocumentBuilder().build(new StreamSource(resultXmlFile));
        Serializer outHtml = proc.newSerializer(resultHtmlFile);
        XsltTransformer trans = exp.load();
        trans.setInitialContextNode(source);
        trans.setDestination(outHtml);
        trans.transform();
        trans.close();

    }
    
    // Glaubs keine gute Idee. Die Klasse ist bloss zuständig für die Prüfung und das
    // Herstellen der Resultate-Files.
    // Mmmmh, wenn ich aber die Lib in Spring Boot verwende, wer macht mir das XML???
    @Deprecated(forRemoval = true)
    public HashMap<String, Boolean> getSummary() {
        if (summaryMap != null) {
            return summaryMap;
        }
        
        summaryMap = new HashMap<String,Boolean>();
        
        for (var entry : resultsMap.entrySet()) {
            log.info(entry.getKey());
            for (Result result : entry.getValue()) {
                summaryMap.put(entry.getKey(), result.isSuccess());
            }
        }

        return summaryMap;
    }
}
