import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.util.logging.FileHandler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private static Logger LOGGER;

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public ClientHandler(Socket clientSocket, Logger logger) {
        this.clientSocket = clientSocket;
        LOGGER = logger;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            var outputStream = clientSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);

            String requestLine;
            StringBuilder requestBuilder = new StringBuilder();
            Map<String, String> headers = new HashMap<>();
            while ((requestLine = reader.readLine()) != null && !requestLine.isEmpty()) {
                String[] headerParts = requestLine.split(":");
                if (headerParts.length == 2) {
                    headers.put(headerParts[0].trim(), headerParts[1].trim());
                }
                requestBuilder.append(requestLine).append("\n");
            }

            int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
            char[] reqBody = new char[contentLength];

            reader.read(reqBody);
            String requestBodyString = new String(reqBody);
            LOGGER.log(Level.INFO, "[Request]\n\r" + requestBuilder.toString().trim());
            LOGGER.log(Level.INFO, "[RequestBody]" + requestBodyString);
            var reqHandler = new RequestHandler(LOGGER);
            var reqObj = reqHandler.handleRequest(requestBuilder.toString().trim());

            if (!reqObj.isHermesRequest()) {
                var responseGenerator = new ResponseHandler(LOGGER);

                var response = responseGenerator.createResponse(reqObj, requestBodyString);
                LOGGER.log(Level.INFO, "[response] \n" + response.getResponseString());
                writer.println(response.getResponseString());
                // implementación protodo aquí de imagenes

                /*
                 * if (response.getResponseBody() != null) {
                 * outputStream.write(response.getResponseBody());
                 * }
                 */

                if (response.getResponseBody() != null) {
                    outputStream.write(response.getResponseBody());
                }
            } else {
                HermesServerSide hermesServerSide = new HermesServerSide(LOGGER, outputStream, writer);
                hermesServerSide.handleImages(reqObj);
            }

            reader.close();
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
