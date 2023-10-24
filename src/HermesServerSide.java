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
            if (FileUtils.checkIfFileExists(fullPath, LOGGER)) {
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
                        int imgWidth = Integer.parseInt(data[2]);
                        int imgHeight = Integer.parseInt(data[3]);
                        Response2 response2 = new Response2.ResponseBuilder2()
                                .setStatus(StatusCodesAndMessage.SUCCESS)
                                .setStatusMessage(StatusCodesAndMessage.SUCCESS_MESSAGE)
                                .addHeader("hermes-chunk-row-count", Integer.toString(rowCount))
                                .addHeader("hermes-chunk-col-count", Integer.toString(colCount))
                                .addHeader("hermes-img-width", Integer.toString(imgWidth))
                                .addHeader("hermes-img-height", Integer.toString(imgHeight))
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

            } else {
                Response response = notFoundResponse();
                writer.println(response.getResponseString());
            }
        } catch (Exception e) {
        }
    }

    public void handleChunkedImage(RequestObj request) {
        try {
            String fullPath = HTML_ROOT + request.path();
                if (FileUtils.checkIfFileExists(fullPath, LOGGER)) {

                File file = new File(fullPath);
                var length = file.length();
                Response2 response2 = new Response2.ResponseBuilder2()
                        .setStatus(StatusCodesAndMessage.SUCCESS)
                        .setStatusMessage(StatusCodesAndMessage.SUCCESS_MESSAGE)
                        .addHeader("hermes-content-length", Long.toString(length))
                        .addHeader("hermes-chunk-size", Long.toString(BUFFER_SIZE))
                        .addHeader("hermes-chunk-count", Long.toString(length / BUFFER_SIZE))
                        .setHtmlContent("")
                        .build();
                writer.println(response2.getResponseString());

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
                // gzipOutput.finish();

            } else {
                Response response = notFoundResponse();
                writer.println(response.getResponseString());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getCause().getMessage());
            Response response = internalServerErrorResponse();
            writer.println(response.getResponseString());
        }
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
                // clientStream.close();
            } else {
                Response response = notFoundResponse();
                writer.println(response.getResponseString());
            }
        } catch (Exception e) {
            Response response = internalServerErrorResponse();
            writer.println(response.getResponseString());
        }
    }

    public void handleImages2(RequestObj request) {

        try {
            String fullPath = HTML_ROOT + request.path();
            if (FileUtils.checkIfFileExists(fullPath, LOGGER)) {
                LOGGER.log(Level.INFO, "INICIO", Long.toString(System.currentTimeMillis()));
                byte[] file = ImageUtils.getScaledImage(fullPath, 100, 100);
                LOGGER.log(Level.INFO, "FIN", Long.toString(System.currentTimeMillis()));

                var length = file.length;
                Response response = new Response.ResponseBuilder()
                        .setStatus(StatusCodesAndMessage.SUCCESS)
                        .setStatusMessage(StatusCodesAndMessage.SUCCESS_MESSAGE)
                        .setResponseLength(length)
                        .setHtmlContent("")
                        .setContentType("image/jpeg")
                        .build();
                writer.println(response.getResponseString());
                // read file in chunks
                int offset = 0;
                while (offset < file.length) {
                    int chunkLength = Math.min(BUFFER_SIZE, file.length - offset);
                    byte[] chunk = new byte[chunkLength];
                    System.arraycopy(file, offset, chunk, 0, chunkLength);
                    offset += chunkLength;
                    clientStream.write(chunk);
                }

                clientStream.close();
            } else {
                Response response = notFoundResponse();
                writer.println(response.getResponseString());
            }
        } catch (Exception e) {
            Response response = internalServerErrorResponse();
            writer.println(response.getResponseString());
        }
    }

    public void handleImages4(RequestObj request) {
        try {
            String fullPath = HTML_ROOT + request.path();
            if (FileUtils.checkIfFileExists(fullPath, LOGGER)) {

                File file = new File(fullPath);
                var length = file.length();
                Response2 response2 = new Response2.ResponseBuilder2()
                        .setStatus(StatusCodesAndMessage.SUCCESS)
                        .setStatusMessage(StatusCodesAndMessage.SUCCESS_MESSAGE)
                        .addHeader("hermes-content-length", Long.toString(length))
                        .addHeader("hermes-chunk-size", Long.toString(BUFFER_SIZE))
                        .addHeader("hermes-chunk-count", Long.toString(length / BUFFER_SIZE))
                        .setHtmlContent("")
                        .build();
                writer.println(response2.getResponseString());

                FileInputStream fileInputStream = new FileInputStream(fullPath);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

                // read file in chunks with loop unrolling
                byte[] bufferedImage = new byte[BUFFER_SIZE];
                int bytesReaded = 0;
                // GZIPOutputStream gzipOutput = new GZIPOutputStream(clientStream);
                while ((bytesReaded = bufferedInputStream.read(bufferedImage, 0, BUFFER_SIZE)) != -1) {
                    // Process the first chunk
                    long startTime = System.currentTimeMillis();
                    clientStream.write(bufferedImage, 0, bytesReaded);
                    long stopTime = System.currentTimeMillis();
                    long elapsedTime = stopTime - startTime;
                    LOGGER.log(Level.INFO, "elapsed time: " + elapsedTime);
                    // Loop unrolling
                    for (int i = 1; i < 4; i++) {
                        bytesReaded = bufferedInputStream.read(bufferedImage, 0, BUFFER_SIZE);
                        if (bytesReaded == -1) {
                            break; // Exit the loop if no more data
                        }
                        long startTime2 = System.nanoTime();
                        clientStream.write(bufferedImage, 0, bytesReaded);
                        long stopTime2 = System.nanoTime();
                        long elapsedTime2 = stopTime2 - startTime2;
                        LOGGER.log(Level.INFO, "elapsed time2: " + elapsedTime2);

                    }
                }

                // gzipOutput.finish();

                bufferedInputStream.close();
                fileInputStream.close();
            } else {
                Response response = notFoundResponse();
                writer.println(response.getResponseString());
            }
        } catch (Exception e) {
            Response response = internalServerErrorResponse();
            writer.println(response.getResponseString());
        }
    }

    public void handleImages5(RequestObj request) {
        try {
            String fullPath = HTML_ROOT + request.path();
            if (FileUtils.checkIfFileExists(fullPath, LOGGER)) {

                File file = new File(fullPath);
                var length = file.length();
                Response2 response2 = new Response2.ResponseBuilder2()
                        .setStatus(StatusCodesAndMessage.SUCCESS)
                        .setStatusMessage(StatusCodesAndMessage.SUCCESS_MESSAGE)
                        .addHeader("hermes-content-length", Long.toString(length))
                        .addHeader("hermes-chunk-size", Long.toString(BUFFER_SIZE))
                        .addHeader("hermes-chunk-count", Long.toString(length / BUFFER_SIZE))
                        .setHtmlContent("")
                        .build();
                writer.println(response2.getResponseString());

                FileChannel fileChannel = new FileInputStream(fullPath).getChannel();
                MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                        fileChannel.size());
                // BufferedInputStream bufferedInputStream = new
                // BufferedInputStream(fileInputStream);
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                // read file in chunks with loop unrolling
                byte[] chunk = new byte[BUFFER_SIZE];
                int bytesReaded = 0;
                // GZIPOutputStream gzipOutput = new GZIPOutputStream(clientStream);
                while (mappedByteBuffer.hasRemaining()) {
                    // Process the first chunk
                    bytesReaded = Math.min(chunk.length, mappedByteBuffer.remaining());
                    mappedByteBuffer.get(chunk, 0, bytesReaded);
                    clientStream.write(chunk, 0, bytesReaded);

                    bytesReaded = Math.min(chunk.length, mappedByteBuffer.remaining());
                    mappedByteBuffer.get(chunk, 0, bytesReaded);
                    clientStream.write(chunk, 0, bytesReaded);

                    bytesReaded = Math.min(chunk.length, mappedByteBuffer.remaining());
                    mappedByteBuffer.get(chunk, 0, bytesReaded);
                    clientStream.write(chunk, 0, bytesReaded);

                    bytesReaded = Math.min(chunk.length, mappedByteBuffer.remaining());
                    mappedByteBuffer.get(chunk, 0, bytesReaded);
                    clientStream.write(chunk, 0, bytesReaded);
                }

                // gzipOutput.finish();

                fileChannel.close();
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
