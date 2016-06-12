package HTTPServer;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhang on 2016/5/30.
 * <p>简易HTTP服务器</p>
 */
public class HTTPServer {

    private static final Logger logger = Logger.getLogger(
            HTTPServer.class.getCanonicalName());
    private static int NUM_OF_THREADS = 50;
    private final File rootDirectory;
    private final int port;

    /**
     * 新建一个简易HTTP服务器
     *
     * @param rootDirectory 文件资源的目录
     * @param port          将要监听的端口号
     * @throws IOException 如果rootDirectory不存在或者不是目录则抛出异常
     */
    public HTTPServer(File rootDirectory, int port) throws IOException {
        if (!rootDirectory.isDirectory()) {
            throw new IOException(rootDirectory + " does not exist as a directory");
        }

        this.rootDirectory = rootDirectory;
        this.port = port;
    }

    /**
     * 启动服务器
     */
    public void start() {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_OF_THREADS);
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("开始监听" + server.getLocalPort() + "端口");
            logger.info("文件目录：" + rootDirectory);

            while (true) {
                try {
                    Socket request = server.accept();
                    logger.log(Level.INFO, "+*+*+*+*+" + " 收到请求 " + "*+*+*+*+*+");
                    Runnable runnable = new RequestProcessor(rootDirectory, request);
                    pool.submit(runnable);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "该连接失败", e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "无法启动服务器");
        }
    }

    /**
     * 设置线程池里的最大线程数，默认为50
     *
     * @param n
     */
    public void setNumOfThreads(int n) {
        HTTPServer.NUM_OF_THREADS = n;
    }
}
