import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public void provideImageInfo(RequestObj request) {
        String fullPath = HTML_ROOT + request.path();
        try {
                Path path = Paths.get(fullPath);
                String fileNameWithExtension = path.getFileName().toString();
                String[] fileData = fileNameWithExtension.split("\\.");
                String fileName = fileData[0];
                String parentDir = path.getParent().toString();
                String manifest = parentDir.concat("/").concat(fileName).concat("chunks").concat("/manifest.txt");
                String chunkLocation = parentDir.concat("/").concat(fileName).concat("chunks/");
                String chunkName = "img_";
                //File manifestFile2 = new File(manifest);
                if (FileUtils.checkIfFileExists(manifest, LOGGER)) {
                    File manifestFile = new File(manifest);
                    BufferedReader manifestReader = new BufferedReader(new FileReader(manifestFile));
                    String rowColumnInfo = manifestReader.readLine();

                    if (rowColumnInfo != null) {
                        String[] data = rowColumnInfo.split(";");
                        int rowCount = Integer.parseInt(data[0]);
                        int colCount = Integer.parseInt(data[1]);
                        Response2 response2 = new Response2.ResponseBuilder2()
                                .setStatus(StatusCodesAndMessage.SUCCESS)
                                .setStatusMessage(StatusCodesAndMessage.SUCCESS_MESSAGE)
                                .addHeader("hermes-chunk-row-count", Integer.toString(rowCount))
                                .addHeader("hermes-chunk-col-count", Integer.toString(colCount))
                                .addHeader("hermes-chunk-src", chunkLocation.replace("./src/www/", ""))
                                .addHeader("hermes-chunk-name", chunkName)
                                .setHtmlContent("")
                                .build();
                        writer.println(response2.getResponseString());
                    }
                    manifestReader.close();

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
