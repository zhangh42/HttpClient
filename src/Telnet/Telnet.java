package Telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by zhang on 2016/5/23.
 */

/**
 * a simple and light telnet client<bt/>
 * It's intended to test a host or simulate some simple HTTP connection
 * <p>For example: time.nist.gov:13 </p>
 */
public class Telnet {

    private Socket socket;
    private String host;
    private int port;

    public Telnet(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
    }

    public Telnet(String host) throws IOException {
        this(host, 23);
    }

    public void write(String line) throws IOException {
        OutputStream os = getOutputStream();
        os.write(line.getBytes());
    }

    /**
     * return a line or empty line;
     *
     * @return a line or empty line
     * @throws IOException
     */
    public String readLine() throws IOException {
        InputStream in = getInputStream();
        StringBuilder line = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            if ((char) c == '\n') {
                break;
            }
            line.append((char) c);
        }
        return line.toString().trim();
    }

    public void flush() throws IOException {
        getOutputStream().flush();
    }

    public void setTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public void close() throws IOException {
        if (socket != null)
            socket.close();
    }
}
