package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ehi.basics.settings.Settings;

public class TestSuiteTest {
    final Logger log = LoggerFactory.getLogger(TestSuiteTest.class);
    
    @Test
    public void dummy() throws IOException {
        // Prepare
        Map<String,String> params = new HashMap<>();
        params.put(TestSuite.PARAM_IDENTIFIER, "SO");
        params.put(TestSuite.PARAM_SERVICE_ENDPOINT, "https://geo.so.ch/api/oereb/");
        params.put(TestSuite.PARAM_EN, "2600595,1215629");
//        params.put(TestSuite.PARAM_IDENTDN, "SO0200002457");
//        params.put(TestSuite.PARAM_NUMBER, "168");
//        params.put(TestSuite.PARAM_EGRID, "CH807306583219");
        
        // Run test
        Settings settings = new Settings();
        settings.setValue(TestSuite.SETTING_LOGFILE, "/Users/stefan/tmp/cts/fubar.xtf");
        
        TestSuite testSuite = new TestSuite();
        testSuite.run(params, settings);

        // Validate
        
    }

}
