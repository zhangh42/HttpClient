package JHTTP_WEB;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhang on 2016/5/22.
 */
public class JHTTP {

    private static final Logger logger = Logger.getLogger(JHTTP.class.getCanonicalName());

    private static final int NUM_THREADS = 50;
    private static final String INDEX_FILE = "index.html";

    private static File rootDirectory;
    private static int port;

    public JHTTP(File rootDirectory, int port) throws IOException {
        if (!rootDirectory.isDirectory())
            throw new IOException(rootDirectory + " does not exist as a directory");
        this.rootDirectory = rootDirectory;
        ;
        this.port = port;
    }

    public static void main(String[] args) {
        File docroot;
        try {
            docroot = new File(args[0]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Usage: java JHTTP docroot port");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[1]);
            if (port < 0 || port > 65535) port = 80;
        } catch (RuntimeException ex) {
            port = 80;
        }

        try {
            JHTTP webserver = new JHTTP(docroot, port);
            webserver.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server could not start", e);
        }
    }

    public void start() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Accepting connections on port " + serverSocket.getLocalPort());
            logger.info("Document Root: " + rootDirectory);

            while (true) {
                Socket request = serverSocket.accept();
                Runnable runnable = new RequestProcessor(rootDirectory, INDEX_FILE, request);
                pool.submit(runnable);
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error accepting connection", ex);
        }
    }
}
