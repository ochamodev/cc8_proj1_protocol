import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class HermesServerSide {
    private static final int BUFFER_SIZE = 8192;

    private OutputStream clientStream;
    private PrintWriter writer;
    private static Logger LOGGER;
    private static String HTML_ROOT = "./src/www";
    private static String INDEX_PATH = HTML_ROOT + "/index.html";
    private static String NOT_FOUND_PATH = HTML_ROOT + "/notFound.html";
    private static String INTERNAL_SERVER_ERROR_PATH = HTML_ROOT + "/serverError.html";

    public HermesServerSide(Logger logger, OutputStream clienStream, PrintWriter writer) {
        LOGGER = logger;
        this.clientStream = clienStream;
        this.writer = writer;
    }

    public void processFileInChunks(RequestObj request) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(request.path());
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        // read file in chunks
        byte[] bufferedImage = new byte[BUFFER_SIZE];
        int bytesReaded = 0;
        while (((bytesReaded = bufferedInputStream.read(bufferedImage)) != -1)) {
            clientStream.write(bufferedImage, 0, bytesReaded);
        }
        fileInputStream.close();
        clientStream.close();
    }

    public void handleImages(RequestObj request) {
        try {
            String fullPath = HTML_ROOT + request.path();
            if (FileUtils.checkIfFileExists(fullPath, LOGGER)) {

                File file = new File(fullPath);

                var length = file.length();
                Response response = new Response.ResponseBuilder()
                        .setStatus(StatusCodesAndMessage.SUCCESS)
                        .setStatusMessage(StatusCodesAndMessage.SUCCESS_MESSAGE)
                        .setResponseLength(length)
                        .setHtmlContent("")
                        .setContentType(request.type())
                        .build();
                writer.println(response.getResponseString());
                FileInputStream fileInputStream = new FileInputStream(fullPath);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                // read file in chunks
                byte[] bufferedImage = new byte[BUFFER_SIZE];
                int bytesReaded = 0;
                while (((bytesReaded = bufferedInputStream.read(bufferedImage)) != -1)) {
                    clientStream.write(bufferedImage, 0, bytesReaded);
                }
                bufferedInputStream.close();
                fileInputStream.close();
                //clientStream.close();
            } else {
                Response response = notFoundResponse();
                writer.println(response.getResponseString());
            }
        } catch (Exception e) {
            Response response = internalServerErrorResponse();
            writer.println(response.getResponseString());
        }
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

}
