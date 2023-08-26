package ch.so.agi.oereb.cts;

import java.io.File;
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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2c;
import ch.interlis.ili2c.Ili2cFailure;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom_j.xtf.XtfWriter;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxWriter;
import ch.interlis.iox_j.ObjectEvent;
import ch.interlis.iox_j.EndBasketEvent;
import ch.interlis.iox_j.EndTransferEvent;
import ch.interlis.iox_j.StartBasketEvent;
import ch.interlis.iox_j.StartTransferEvent;

public class TestSuite {
    final Logger log = LoggerFactory.getLogger(TestSuite.class);
        
    private static final String MODEL_NAME = "SO_AGI_OEREB_CTS_20230819";
    private static final String ILI_TOPIC = MODEL_NAME + ".Results";
            
    public List<Result> run(Map<String,String> params, Settings settings) { 
        String testSuiteTime = settings.getValue(TestSuite.SETTING_TESTSUITE_TIME);
        // TODO: Will ich das wirklich? Dann funktioniert ja genau die Query nicht mehr später, weil
        // unterschiedliche Zeiten.
//        if (testSuiteTime == null) {
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss");
//            testSuiteTime = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime().format(dtf);
//        }
        
        List<Result> results = new ArrayList<>();
        
        String serviceEndpoint = params.get(PARAM_SERVICE_ENDPOINT);

        log.info("Validating service endpoint: " + serviceEndpoint + " ("+params.get(PARAM_IDENTIFIER)+")");

        {
            log.debug("validating capabilities");
            // Capabilities-Prüfungen
            GetCapabilitiesMethod method = new GetCapabilitiesMethod();
            List<Result> probeResults = method.run(serviceEndpoint, params);
            setTestSuitTime(probeResults, testSuiteTime);
            results.addAll(probeResults);
        }

        {
            log.debug("validating versions");
            // Versions-Prüfungen
            GetVersionsMethod method = new GetVersionsMethod();
            List<Result> probeResults = method.run(serviceEndpoint, params);
            setTestSuitTime(probeResults, testSuiteTime);
            results.addAll(probeResults);
        }

        {
            log.debug("validating getegrid");
            // GetEGRID-Prüfungen
            GetEGRIDMethod method = new GetEGRIDMethod();
            List<Result> probeResults = method.run(serviceEndpoint, params);
            setTestSuitTime(probeResults, testSuiteTime);
            results.addAll(probeResults);
        }
        
        {
            log.debug("validating extract");
            // Extract-Prüfungen
            GetExtractByIdMethod method = new GetExtractByIdMethod();
            List<Result> probeResults = method.run(serviceEndpoint, params);
            setTestSuitTime(probeResults, testSuiteTime);
            results.addAll(probeResults);
        }            
        
        if (settings.getValue(SETTING_LOGFILE) != null) {
            File xtfFile = Paths.get(settings.getValue(SETTING_LOGFILE)).toFile();

            try {
                TransferDescription td = getTransferdescription();
                IoxWriter ioxWriter = new XtfWriter(xtfFile, td);

                ioxWriter.write(new StartTransferEvent("SOGIS-20230819", "", null));
                ioxWriter.write(new StartBasketEvent(ILI_TOPIC, params.get(TestSuite.PARAM_IDENTIFIER)));                

                for (Result result : results) {
                    ioxWriter.write(new ObjectEvent(result.toIomObject()));
                }
                
                ioxWriter.write(new EndBasketEvent());
                ioxWriter.write(new EndTransferEvent());
                ioxWriter.flush();
                ioxWriter.close();    

            } catch (Ili2cFailure | IOException | IoxException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        return results;
    }
    
    private void setTestSuitTime(List<Result> results, String testSuiteTime) {
        for (Result result : results) {
            result.setTestSuiteTime(testSuiteTime);
        }
    }
    
    private File copyResourceToTmpDir(String resource) throws IOException {
        Path exportedFile = null;
        InputStream is = TestSuite.class.getClassLoader().getResourceAsStream(resource);
        Path exportDir = Files.createTempDirectory("oerebcts");
        exportedFile = exportDir.resolve(new File(resource).getName());
        Files.copy(is, exportedFile, StandardCopyOption.REPLACE_EXISTING);
        return exportedFile.toFile();
    }
    
    private TransferDescription getTransferdescription() throws IOException, Ili2cFailure {        
        File iliFile = copyResourceToTmpDir("ili/SO_AGI_OEREB_CTS_20230819.ili");

        ArrayList<String> filev = new ArrayList<String>() {{ add(iliFile.getAbsolutePath()); }};
        TransferDescription td = Ili2c.compileIliFiles(filev, null);

        if (td == null) {
            throw new IllegalArgumentException("INTERLIS compiler failed");
        }

        return td;
    }

    public static final String SETTING_LOGFILE = "ch.so.agi.oereb.cts.log";
    public static final String SETTING_TESTSUITE_TIME = "ch.so.agi.oereb.cts.time";
    public static final String PARAM_IDENTIFIER = "identifier";
    public static final String PARAM_SERVICE_ENDPOINT = "SERVICE_ENDPOINT";
    public static final String PARAM_EN = "EN";
    public static final String PARAM_IDENTDN = "IDENTDN";
    public static final String PARAM_NUMBER = "NUMBER";
    public static final String PARAM_EGRID = "EGRID";
}
