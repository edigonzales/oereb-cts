package ch.so.agi.oereb.cts;

public class Utils {
    // Entfernt doppelte Slashes
    public static String fixUrl(String url) {
        return url.replaceAll("(?<=[^:\\s])(\\/+\\/)", "/");
    }
}
