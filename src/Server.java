import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.logging.FileHandler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.lang.System.err;
import static java.lang.System.out;

public class Server {
    private static final int PORT = 1850;
    private ExecutorService pool;
    private ServerSocket serverSocket;
    private static Logger LOGGERServer;
    private static Logger LOGGERClient;

    public Server(String[] argv){
        Integer count = (argv.length>0)?Integer.parseInt(argv[0]):10;
        out.println( "#Theads: " + count );
        out.println( this.getClass() );
        LOGGERServer = this.initLogger(LOGGERServer, this.getClass().getName());
        LOGGERClient = this.initLogger(LOGGERClient, ClientHandler.class.getName());
        pool = Executors.newFixedThreadPool(count);
        try {
            serverSocket = new ServerSocket(PORT);
            LOGGERServer.log(Level.INFO, "Server Port [" + PORT + "]");
        } catch (IOException e) {
            this.stop();
            e.printStackTrace();
        }
    }

    public void stop() {
        pool.shutdown();
    }

    public Logger initLogger(Logger logger, String nameClass) {
        logger = Logger.getLogger(nameClass);
        logger.setLevel(Level.FINE);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.WARNING);
        consoleHandler.setFormatter(new ConsoleFormatter());
        logger.addHandler(consoleHandler);
        try {
            Handler fileHandler = new FileHandler(nameClass + ".log", Integer.MAX_VALUE, 5,true);
            fileHandler.setLevel(Level.FINE);
            fileHandler.setFormatter(new FileFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.setUseParentHandlers(false);
        return logger;
    }

    public void start() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                LOGGERServer.log(Level.INFO, "IP Client [" + clientSocket.getInetAddress().getHostAddress() + "]");
                ClientHandler clientHandler = new ClientHandler(clientSocket, LOGGERClient);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            this.stop();
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        (new Server(args)).start();
    }
}

