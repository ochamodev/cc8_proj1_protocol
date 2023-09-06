import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler {
    private static Logger LOGGER;

    public RequestHandler(Logger logger) {
        LOGGER = logger;
    }

    public RequestObj handleRequest(String requestLine) {
        var lines = requestLine.split("\n");

        var firstLine = lines[0];
        LOGGER.log(Level.INFO, "**************FirstLine: "+firstLine);
        var separatedLine = firstLine.split(" ");
        var method = separatedLine[0];
        var path = separatedLine[1];
        var httpVersion = separatedLine[2];
        for (int i = 0; i < separatedLine.length; i++) {
            LOGGER.log(Level.INFO, "firstLine: " + separatedLine[i]);
        }
        for (int i = 0; i < lines.length; i++) {
            LOGGER.log(Level.INFO, String.format("Line: %d content: %s", i, lines[i]));
        }
        LOGGER.log(Level.INFO, "method: " + method);
        LOGGER.log(Level.INFO, "path: " + path);
        LOGGER.log(Level.INFO, "httpVersion: " + httpVersion);
        String type = MimeTypes.HTML_TYPE;
        if (path.contains("css")) {
            type = MimeTypes.CSS;
        } else if (path.contains(".ttf")) {
            type = MimeTypes.FONT_TTF;
        } else if (path.contains(".otf")) {
            type = MimeTypes.FONT_SFNT;
        } else if (path.contains(".woff2")) {
            type = MimeTypes.FONT_WOFF2;
        } else if (path.contains(".woff")) {
            type = MimeTypes.FONT_WOFF;
        } else if (path.contains(".eot")) {
            type = MimeTypes.FONT_EOT;
        } else if (path.contains(".js")) {
            type = MimeTypes.JS_TYPE;
        } else if (path.contains(".png")) {
            type = MimeTypes.PNG_TYPE;
        }
        LOGGER.log(Level.INFO, "[pathWithoutSplit]: " + path);
        if (path.contains("?")) {
            path = path.split("\\?")[0];
            LOGGER.log(Level.INFO, "[pathWithSplit]: " + path);
        }
        var req = new RequestObj(method, path, httpVersion, type);
        LOGGER.log(Level.INFO, "REQ: " + req);
        return req;
    }

}
