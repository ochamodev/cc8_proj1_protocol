import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseHandler {
    private static Logger LOGGER;
    private static String HTML_ROOT = "./src/www";
    private static String INDEX_PATH = HTML_ROOT + "/index.html";
    private static String NOT_FOUND_PATH = HTML_ROOT + "/notFound.html";
    private static String INTERNAL_SERVER_ERROR_PATH = HTML_ROOT + "/serverError.html";

    public ResponseHandler(Logger logger) {
        LOGGER = logger;
    }

    public Response createResponse(RequestObj requestObj, String requestBodyString) {
        Response response = null;
        if (requestObj.method().equals("GET") || requestObj.method().equals("HEAD")) {
            if (requestObj.path().equals("/")) {
                response = handleTextTypes(INDEX_PATH, LOGGER, requestObj);
            } else {
                var rootPath = HTML_ROOT;
                var fullPath = rootPath + requestObj.path();
                LOGGER.log(Level.INFO, "[PATH]" + fullPath);
                if (isTextType(requestObj)) {
                    response = handleTextTypes(fullPath, LOGGER, requestObj);
                } else {
                    response = handleBinaryData(fullPath, LOGGER, requestObj);
                }

            }
        }
        if (requestObj.method().equals("POST")) {
            var fullPath = HTML_ROOT + requestObj.path();
            response = handlePostType(fullPath, LOGGER, requestObj, requestBodyString);
        }
        return response;
    }

    private static boolean isTextType(RequestObj requestObj) {
        if (requestObj.type().equals(MimeTypes.CSS)
                || requestObj.type().equals(MimeTypes.JS_TYPE) ||
                requestObj.type().equals(MimeTypes.HTML_TYPE)) {
            return true;
        } else {
            return false;
        }
    }

    private static Response handleTextTypes(String path, Logger logger, RequestObj requestObj) {
        Response response = null;
        if (FileUtils.checkIfFileExists(path, LOGGER)) {
            var content = FileUtils.readHtml(path);
            if (content == null) {
                content = FileUtils.readHtml(INTERNAL_SERVER_ERROR_PATH);
                response = internalServerErrorResponse();
            } else {
                response = new Response.ResponseBuilder()
                        .setStatus(StatusCodesAndMessage.SUCCESS)
                        .setStatusMessage(StatusCodesAndMessage.SUCCESS_MESSAGE)
                        .setHtmlContent(content)
                        .setContentType(requestObj.type())
                        .build();
            }
        } else {
            response = notFoundResponse();
        }
        return response;
    }

    private static Response handleBinaryData(String path, Logger logger, RequestObj requestObj) {
        Response response = null;
        try {
            if (FileUtils.checkIfFileExists(path, logger)) {
                var content = FileUtils.readFile(path);
                response = new Response.ResponseBuilder()
                        .setStatus(StatusCodesAndMessage.SUCCESS)
                        .setStatusMessage(StatusCodesAndMessage.SUCCESS_MESSAGE)
                        .setResponseBody(content)
                        .setHtmlContent("")
                        .setContentType(requestObj.type())
                        .build();
            } else {
                response = notFoundResponse();
            }
        } catch (Exception e) {
            response = internalServerErrorResponse();
            return response;
        }

        return response;
    }

    private static Response handlePostType(String path, Logger logger, RequestObj requestObj, String requestBody) {
        Pattern pattern = Pattern.compile("([^&=]+)=([^&]*)");
        Matcher matcher = pattern.matcher(requestBody);
        Response response = null;
        Map<String, String> formParameters = new HashMap<>();
        while (matcher.find()) {
            formParameters.put(
                decodeParameter(matcher.group(1)),
                decodeParameter(matcher.group(2))
            );
        }

        if (FileUtils.checkIfFileExists(path, logger)) {
            var content = FileUtils.readHtml(path);
            if (content == null) {
                response = internalServerErrorResponse();
            } else {
                var finalResult = replaceTokens(content, formParameters);
                response = new Response.ResponseBuilder()
                        .setStatus(StatusCodesAndMessage.SUCCESS)
                        .setStatusMessage(StatusCodesAndMessage.SUCCESS_MESSAGE)
                        .setHtmlContent(finalResult)
                        .setContentType(requestObj.type())
                        .build();
            }
        }

        return response;
    }

    private static String replaceTokens(String input, Map<String, String> tokens) {
        Pattern pattern = Pattern.compile("\\{([^}]*)\\}");

        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String token = matcher.group(1);
            String replacement = tokens.getOrDefault(token, "");
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static Response notFoundResponse() {
        var content = FileUtils.readHtml(NOT_FOUND_PATH);
        var response = new Response.ResponseBuilder()
                .setStatus(StatusCodesAndMessage.NOT_FOUND)
                .setStatusMessage(StatusCodesAndMessage.NOT_FOUND_MESSAGE)
                .setHtmlContent(content)
                .setContentType(MimeTypes.HTML_TYPE)
                .build();

        return response;
    }

    private static Response internalServerErrorResponse() {
        String content = FileUtils.readHtml(INTERNAL_SERVER_ERROR_PATH);
        Response response = new Response.ResponseBuilder()
                .setStatus(StatusCodesAndMessage.INTERNAL_SERVER_ERROR)
                .setStatusMessage(StatusCodesAndMessage.INTERNAL_SERVER_ERROR_MESSAGE)
                .setHtmlContent(content)
                .setContentType(MimeTypes.HTML_TYPE)
                .build();
        return response;
    }

    private static String decodeParameter(String encodedParameter) {
        try {
            return URLDecoder.decode(encodedParameter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return encodedParameter;
        }
    }

}
