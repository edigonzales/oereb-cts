package ch.so.agi.oereb.cts;

import java.net.http.HttpResponse;
import java.nio.file.Path;

public interface ICheck {
    public Result run(HttpResponse<Path> response);
}
