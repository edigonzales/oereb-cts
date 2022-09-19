package ch.so.agi.oereb.cts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class Meta2File_CliTest {

    private static final String[] PARA_TEMPLATE = new String[]{
            "-g", "dummy",
            "-c", "connurl",
            "-u", "test",
            "-p", "test"
    };

    @Test
    public void main_MissingParam_DoesNotThrow() throws Exception{
        String[] args = Arrays.copyOf(PARA_TEMPLATE, PARA_TEMPLATE.length - 1);

        //Meta2File.main(args);
    }

    @Test
    public void main_InvalidConn_DoesNotThrow() throws Exception{
        String[] args = Arrays.copyOf(PARA_TEMPLATE, PARA_TEMPLATE.length);

        /*
        Assertions.assertThrows(
                Exception.class,
                () -> Meta2File.main(args)
        );
        */
    }
}
