package ch.so.agi.oereb.cts;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public interface ICheck {
    public Result run(HttpResponse<Path> response) throws CheckException;
}
