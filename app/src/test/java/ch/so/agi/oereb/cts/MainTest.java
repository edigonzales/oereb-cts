package ch.so.agi.oereb.cts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTest {

    @Test
    public void missingConfigParam() throws Exception{
        String[] args = new String[] {
                "--out", System.getProperty("java.io.tmpdir")
        };
         
        int ret = Main.mainWithExitCode(args);
        assertEquals(2, ret);
        
        System.out.println(ret);
        
//        Assertions.assertThrows(
//                Exception.class,
//                () -> Main.mainWithExitCode(args)
//        );        
    }
}
