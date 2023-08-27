package ch.so.agi.oereb.cts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.oereb.cts.TestSuite;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class Validator {
    private static Logger log = LoggerFactory.getLogger(Main.class);
            
    public void run(String config, String outDirectory) throws InvalidFileFormatException, IOException, XMLStreamException, SaxonApiException {
        List<String> logFiles = new ArrayList<>();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss");
        String testSuiteTime = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime().format(dtf);

        File configFile = new File(config);
        Ini ini = new Ini(configFile);

        Set<Entry<String, Section>> iniSet = ini.entrySet();       
        for (Entry<String, Section> sectionMap : iniSet) {            
            Section section = sectionMap.getValue();
            String serviceEndpoint = section.get("SERVICE_ENDPOINT");
            
            if (serviceEndpoint == null) {
                throw new IllegalArgumentException("Service endpoint is missing.");
            }
            
            var params = new HashMap<String,String>();
            params.put(TestSuite.PARAM_IDENTIFIER, sectionMap.getKey());
            
            params.put(TestSuite.PARAM_SERVICE_ENDPOINT, serviceEndpoint);
            
            if (section.containsKey("EN")) {
                params.put(TestSuite.PARAM_EN, section.get("EN"));
            }
            if (section.containsKey("IDENTDN")) {
                params.put(TestSuite.PARAM_IDENTDN, section.get("IDENTDN"));
            }
            if (section.containsKey("NUMBER")) {
                params.put(TestSuite.PARAM_NUMBER, section.get("NUMBER"));
            }
            if (section.containsKey("EGRID")) {
                params.put(TestSuite.PARAM_EGRID, section.get("EGRID"));
            }
            
            if (params.size() == 0) {
                throw new IllegalArgumentException("No test parameters defined.");
            }

            //log.info("Validating service endpoint: " + serviceEndpoint + " ("+sectionMap.getKey()+")");
              
            Settings settings = new Settings();
            String logFile = Paths.get(outDirectory, "cts-" + params.get(TestSuite.PARAM_IDENTIFIER) + ".xtf").toFile().getAbsolutePath();
            settings.setValue(TestSuite.SETTING_LOGFILE, logFile);
            
            settings.setValue(TestSuite.SETTING_TESTSUITE_TIME, testSuiteTime);

            TestSuite testSuite = new TestSuite();
            testSuite.run(params, settings);

            logFiles.add(logFile);            
        }
        
        for (String logFile : logFiles) {
            File resultHtmlFile = Paths.get(outDirectory, new File(logFile).getName() + ".html").toFile();
            
            String XML2PDF_XSL = "xml2html.xsl";
            File xsltFile = new File(Paths.get(outDirectory, XML2PDF_XSL).toFile().getAbsolutePath());
            InputStream xsltFileInputStream = Validator.class.getResourceAsStream("/"+XML2PDF_XSL); 
            Files.copy(xsltFileInputStream, xsltFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            xsltFileInputStream.close();

            Processor proc = new Processor(false);
            XsltCompiler comp = proc.newXsltCompiler();
            XsltExecutable exp = comp.compile(new StreamSource(xsltFile));
            
            XdmNode source = proc.newDocumentBuilder().build(new StreamSource(logFile));
            Serializer outHtml = proc.newSerializer(resultHtmlFile);
            XsltTransformer trans = exp.load();
            trans.setInitialContextNode(source);
            trans.setDestination(outHtml);
            trans.transform();
            trans.close();   
        }
    }    
}
