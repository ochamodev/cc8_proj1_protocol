import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.logging.Level;
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

    public void provideImageInfo(RequestObj request) {
        String fullPath = HTML_ROOT + request.path();
        if (FileUtils.checkIfFileExists(fullPath, LOGGER)) {
            byte[] bytes;
            try {
                bytes = ImageUtils.getScaledImage(fullPath, 600, 600);
                Response2 response2 = new Response2.ResponseBuilder2()
                        .setStatus(StatusCodesAndMessage.SUCCESS)
                        .setStatusMessage(StatusCodesAndMessage.SUCCESS_MESSAGE)
                        .addHeader("hermes-content-length", Long.toString(bytes.length))
                        .addHeader("hermes-chunk-size", Long.toString(BUFFER_SIZE))
                        .addHeader("hermes-chunk-count", Long.toString(bytes.length / BUFFER_SIZE))
                        .setHtmlContent("")
                        .build();
                writer.println(response2.getResponseString());
            } catch (IOException e) {
                var server = internalServerErrorResponse();
                writer.println(server.getResponseString());
            }

        } else {
            Response response = notFoundResponse();
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

    public void handleImages3(RequestObj request) {
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

                // Loop through the image in chunks

                FileInputStream fileInputStream = new FileInputStream(fullPath);
                BufferedImage image = ImageIO.read(fileInputStream); // reading the image file

                int rows = 6; // You should decide the values for rows and cols variables
                int cols = 6;
                int chunks = rows * cols;
                int chunkWidth = image.getWidth() / cols; // determines the chunk width and height
                int chunkHeight = image.getHeight() / rows;
                int count = 0;
                BufferedImage imgs[] = new BufferedImage[chunks];
                for (int x = 0; x < rows; x++) {
                    for (int y = 0; y < cols; y++) {
                        // Initialize the image array with image chunks
                        imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                        Graphics2D gr = imgs[count].createGraphics();
                        gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y,
                                chunkHeight * x,
                                chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                        gr.dispose();

                    }
                }
                fileInputStream.close();

                for (int i = 0; i < imgs.length; i++) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    Thumbnails.of(imgs[i])
                            .size(100, 100)
                            .outputFormat("jpeg")
                            .outputQuality(0.8)
                            .toOutputStream(outputStream);
                    imgs[i].flush();

                    clientStream.write(outputStream.toByteArray());
                    outputStream.close();
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

                // read file in chunks with loop unrolling
                byte[] bufferedImage = new byte[BUFFER_SIZE];
                int bytesReaded = 0;

                while ((bytesReaded = bufferedInputStream.read(bufferedImage, 0, BUFFER_SIZE)) != -1) {
                    // Process the first chunk
                    clientStream.write(bufferedImage, 0, bytesReaded);

                    // Loop unrolling
                    for (int i = 1; i < 6; i++) {
                        bytesReaded = bufferedInputStream.read(bufferedImage, 0, BUFFER_SIZE);
                        if (bytesReaded == -1) {
                            break; // Exit the loop if no more data
                        }
                        clientStream.write(bufferedImage, 0, bytesReaded);
                    }
                }

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

    public void handleImages6(RequestObj request) {
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

                // Loop through the image in chunks

                FileInputStream fileInputStream = new FileInputStream(fullPath);
                BufferedImage image = ImageIO.read(fileInputStream); // reading the image file

                int rows = 4; // You should decide the values for rows and cols variables
                int cols = 4;
                int chunks = rows * cols;
                int chunkWidth = image.getWidth() / cols; // determines the chunk width and height
                int chunkHeight = image.getHeight() / rows;
                int count = 0;
                BufferedImage imgs[] = new BufferedImage[chunks];
                for (int x = 0; x < rows; x++) {
                    for (int y = 0; y < cols; y++) {
                        // Initialize the image array with image chunks
                        imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                        Graphics2D gr = imgs[count].createGraphics();
                        gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y,
                                chunkHeight * x,
                                chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                        gr.dispose();
                        count++;
                    }
                }
                fileInputStream.close();

                for (int i = 0; i < imgs.length; i++) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    /*
                     * File tempFile = File.createTempFile("scaled_image", "jpg");
                     * ImageIO.write(imgs[i], "jpeg", tempFile);
                     * 
                     * Files.copy(tempFile.toPath(), outputStream);
                     * tempFile.delete();
                     * 
                     * clientStream.write(outputStream.toByteArray(), 0, outputStream.size());
                     */
                    Thumbnails.of(imgs[i])
                            .size(100, 100)
                            .outputFormat("jpeg")
                            .outputQuality(0.3)
                            .toOutputStream(outputStream);
                    clientStream.write(outputStream.toByteArray(), 0, outputStream.size());
                    outputStream.close();
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
