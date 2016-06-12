package JHTTP_WEB;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhang on 2016/5/22.
 */
public class RequestProcessor implements Runnable {

    public final static Logger logger = Logger.getLogger(
            RequestProcessor.class.getCanonicalName());

    private File rootDirectory;
    private String indexFilename = "index.html";
    private Socket connection;

    public RequestProcessor(File rootDirectory, String indexFileName, Socket request) {

        if (rootDirectory.isFile()) {
            throw new IllegalArgumentException("rootDirectory must be a directory, not a file");
        }

        try {
            rootDirectory = rootDirectory.getCanonicalFile();
        } catch (IOException e) {
        }

        this.rootDirectory = rootDirectory;
        if (indexFilename != null) this.indexFilename = indexFilename;
        this.connection = request;
    }

    @Override
    public void run() {
        // 安全检查
        String root = rootDirectory.getPath();
        try {
            OutputStream raw = new BufferedOutputStream(
                    connection.getOutputStream());
            Writer out = new OutputStreamWriter(raw);
            Reader in = new InputStreamReader(
                    new BufferedInputStream(
                            connection.getInputStream()));
            StringBuilder requestLine = new StringBuilder();
            while (true) {
                int c = in.read();
                if (c == '\r' || c == '\n') break;
                requestLine.append((char) c);
            }
            String get = requestLine.toString();

            logger.info(connection.getRemoteSocketAddress() + " " + get);
            String[] tokens = get.split("\\s+");
            String method = tokens[0];
            String version = "";
            if (method.equalsIgnoreCase("GET")) {
                String filename = tokens[1];
                if (filename.endsWith("/")) filename += indexFilename; // FIXME: 2016/5/22 startWith() ?
                String contentType =
                        URLConnection.getFileNameMap().getContentTypeFor(filename);
                if (tokens.length > 2) {
                    version = tokens[2];
                }

                File theFile = new File(rootDirectory,
                        filename.substring(1, filename.length()));

                if (theFile.canRead()
                        // 禁止访问指定外的目录
                        && theFile.getCanonicalPath().startsWith(root)) {
                    byte[] theData = Files.readAllBytes(theFile.toPath());
                    if (version.startsWith("HTTP/")) {
                        sendHeader(out, "HTTP/1.0 200 OK", contentType, theData.length);
                    }
                    // 发送文件，可能是图像什么的，所以用底层输出流，而不用writer
                    raw.write(theData);
                    raw.flush();
                } else {
                    String body = new StringBuilder("<html>\r\n")
                            .append("<head><title>File not Found</title>\r\n")
                            .append("</head>\r\n")
                            .append("<body>")
                            .append("<h1>HTTP Error 404: File Not Found</h1>\r\n")
                            .append("</body></html>\r\n").toString();
                    if (version.startsWith("HTTP/")) {
                        sendHeader(out, "HTTP/1.0 404 File Not Found",
                                "text/html; charset=utf-8", body.length()); // FIXME: 2016/5/22 body.getByte().length()
                    }
                    out.write(body);
                    out.flush();
                }
            } else { // 方法不等于GET
                String body = new StringBuilder("<html>\r\n")
                        .append("<head><title>Not Implemented</title>\r\n")
                        .append("</head>\r\n")
                        .append("<body>")
                        .append("<h1>HTTP Error 501: Not Implemented</h1>\r\n")
                        .append("</body></html>\r\n").toString();
                if (version.startsWith("HTTP/")) {
                    sendHeader(out, "HTTP/1.0 501 Not Implemented",
                            "text/html; charset=utf-8", body.length()); // FIXME: 2016/5/22 body.getByte().length()
                }
                out.write(body);
                out.flush();
            }


        } catch (IOException e) {
            logger.log(Level.WARNING,
                    "Error talking to " + connection.getRemoteSocketAddress(), e);
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
            }
        }
    }

    private void sendHeader(Writer out, String responseCode,
                            String contentType, int length) throws IOException {
        out.write(responseCode + "\r\n");
        Date now = new Date();
        out.write("Date: " + now + "\r\n");
        out.write("Server: JHTTP 1.0\r\n");
        out.write("Content-length: " + length + "\r\n");
        out.write("Content-type: " + contentType + "\r\n\r\n");
        out.flush();
    }
}
