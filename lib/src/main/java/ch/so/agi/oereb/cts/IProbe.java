package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IProbe {
    public  List<Result> run(String serviceEndpoint, Map<String,String> parameters) throws IOException;
}
