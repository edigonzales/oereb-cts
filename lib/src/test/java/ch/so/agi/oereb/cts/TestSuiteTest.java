package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class TestSuiteTest {

    @Test
    public void runSuite_Ok() throws IOException {
        // Prepare
        Map<String,String> params = Map.of(
                "SERVICE_ENDPOINT", "https://geo.so.ch/api/oereb/", 
                "EN","2600595,1215629",
                "IDENTDN", "SO0200002457",
                "NUMBER", "168",
                "EGRID", "CH807306583219",
                "SUCCESS", "true");

        // Run test
        TestSuite testSuite = new TestSuite();
        testSuite.run(params);
    }
}
