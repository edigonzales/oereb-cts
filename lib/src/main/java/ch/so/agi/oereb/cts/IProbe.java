package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.net.URI;

public interface IProbe {
    public Result run(URI requestUrl) throws IOException;
}
