package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.SaxonApiException;

public class Main {
    private static Logger log = LoggerFactory.getLogger(Main.class);

    private final static String FOLDER_PREFIX = "oerebctsapp";
    
    public static void main(String[] args) throws Exception {

        int res = mainWithExitCode(args);
        if(res != 0)
            System.exit(res);

    }
    
    static int mainWithExitCode(String[] args) throws IOException, XMLStreamException, SaxonApiException {
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
                System.err.println("--out        Output directory where the result file(s) are written. Default: System tmp directory.");
                System.err.println();
                return 0;
            }
        }
        
        if (config == null) {
            System.err.println("config is required.");
            return 2;
        }        
        
        if (outDirectory == null) {
            outDirectory = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), FOLDER_PREFIX).toFile().getAbsolutePath();
            System.out.println("File will be written to: " + outDirectory);
        }
        
        var validator = new Validator();
        validator.run(config, outDirectory); // TODO: exceptions
        
        return 0;
    }
}