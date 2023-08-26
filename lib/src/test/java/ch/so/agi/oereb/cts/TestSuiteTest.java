package ch.so.agi.oereb.cts;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ehi.basics.settings.Settings;

@Tag("external")
public class TestSuiteTest {
    final Logger log = LoggerFactory.getLogger(TestSuiteTest.class);
    
    @Test
    public void runTestSuite_Ok(@TempDir Path tempDir) throws IOException {
        // Prepare
        Map<String,String> params = new HashMap<>();
        params.put(TestSuite.PARAM_IDENTIFIER, "SO");
        params.put(TestSuite.PARAM_SERVICE_ENDPOINT, "https://geo.so.ch/api/oereb/");
        params.put(TestSuite.PARAM_EN, "2600595,1215629");
//        params.put(TestSuite.PARAM_IDENTDN, "SO0200002457");
//        params.put(TestSuite.PARAM_NUMBER, "168");
//        params.put(TestSuite.PARAM_EGRID, "CH807306583219");
        
        Path logFile = tempDir.resolve("mylog.xtf");
        
        // Run test
        Settings settings = new Settings();
        settings.setValue(TestSuite.SETTING_LOGFILE, logFile.toFile().getAbsolutePath());
        
        TestSuite testSuite = new TestSuite();
        testSuite.run(params, settings);

        // Validate
        String content = Files.readString(logFile);
        
        System.out.println(content);        
        assertTrue(content.contains("TID=\"SO.ch.so.agi.oereb.cts.GetCapabilitiesProbe\""));
        assertTrue(content.contains("<statusCode>200</statusCode>"));        
    }

}
