package HTTPServer;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 处理请求
 */
class RequestProcessor implements Runnable {

    private static final Logger logger = Logger.getLogger(
            RequestProcessor.class.getCanonicalName());

    private File rootDirectory;
    private Socket socket;

    RequestProcessor(File rootDirectory, Socket request) {
        this.rootDirectory = rootDirectory;
        this.socket = request;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {
            // 获取报头首行状态信息
            String firstLine = reader.readLine();
            String method = firstLine.split(" ")[0];
            String urlStr = firstLine.split(" ")[1];

            logger.log(Level.INFO, firstLine); // 输出日志信息

            if (method.equalsIgnoreCase("GET")) {
                File file = new File(rootDirectory, urlStr);
                String contentType = URLConnection.getFileNameMap()
                        .getContentTypeFor(urlStr); // 获取类型信息
                Writer out = new OutputStreamWriter(socket.getOutputStream());
                if (file.canRead()) {
                    byte[] data = Files.readAllBytes(file.toPath());
                    sendHeader(out, "HTTP/1.0 200 OK", contentType, data.length);
                    // 发送的内容可能是文件、图像等二进制，所以用字节流
                    socket.getOutputStream().write(data);
                    socket.getOutputStream().flush();
                    logger.log(Level.INFO, "200 OK");
                } else {
                    logger.log(Level.SEVERE, "请求文件地址错误：" + file);
                    logger.log(Level.INFO, "404 File Not Found");
                    String body = "<html>\n" +
                            "<head>\n" +
                            "    <title>File not found</title>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "    <h1>HTTP Error 404: File Not Found</h1>\n" +
                            "</body>\n" +
                            "</html>\r\n";

                    sendHeader(out, "HTTP/1.0 404 File Not Found",
                            "text/html; charset=utf-8", body.length());
                    out.write(body);
                    out.flush();
                }


            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "连接异常", e);
        }
    }

    /**
     * 发送报头
     *
     * @param writer       字符输出流，用于发送报头
     * @param responseCode 相应代码
     * @param contentType  MIME信息
     * @param length       报文长度大小
     * @throws IOException
     */
    void sendHeader(Writer writer, String responseCode, String contentType, int length) throws IOException {
        writer.write(responseCode + "\r\n");
        writer.write("Date: " + new Date() + "\r\n");
        writer.write("Server: ZHTTP\r\n");
        writer.write("Content-length: " + length + "\r\n");
        writer.write("Content-type: " + contentType + "\r\n\r\n");
        writer.flush();
    }
}
