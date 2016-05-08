package HttpClient;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by zhang on 2016/4/27.
 */

/**
 * 本类继承HttpURLConnection并且尽可能的用Socket类重写父类方法
 */
public class MyHttpURLConnection extends HttpURLConnection {
    ArrayList<String> requestLine = new ArrayList<>();
    /**
     * Constructor for the HttpURLConnection.
     *
     * @param u the URL
     */

    private Socket socket;
    private String host;
    private int port;
    private boolean isFollowRedirects = true; // 是否支持重定向
    private String responseMessage = null;
    private int responseCode = -1;
    private ArrayList<String> headers;
    private URL url;
    private String protocol;
    private String method = "GET";
    private String body = null;

    protected MyHttpURLConnection(URL u) throws IOException {
        super(u);
        this.url = u;
        host = url.getHost();
        port = url.getPort() > 0 ? url.getPort() : 80;
        protocol = url.getProtocol();
        connect();
    }

    @Override
    public void disconnect() {
        if (socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    @Override
    public void connect() throws IOException {
        if (socket == null) {
            if (protocol.equalsIgnoreCase("http")) {
                socket = new Socket(host, port);
            } else if (protocol.equalsIgnoreCase("https")) {
                socket = SSLSocketFactory.getDefault().createSocket(host, 443);
            } else {
                System.out.println("wrong protocol");
            }
        }
    }

    /**
     * 获取报文
     */
    private void getHeader() {
        StringBuilder header = new StringBuilder();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(socket.getOutputStream());
            String relative_url = url.toString();
            relative_url = relative_url.substring(relative_url.indexOf(host) + host.length());
            if (relative_url.equals("")) {
                relative_url = "/";
            }
            writer.write(method + " " + relative_url + " " + "HTTP/1.1\r\n");
            writer.write("Host: " + host + "\r\n");
            for (String line : requestLine) {
                writer.write(line);
            }
//            writer.write("Accept-Encoding: gzip, deflate, sdch\r\n");
            writer.write("\r\n");
            writer.flush();
            // 若是post、delete等方法，则还有报文数据部分
            if (body != null) {
                writer.write(body);
                body = null; // 避免页面跳转时再次提交
                writer.flush();
            }

            InputStream inputStream = socket.getInputStream();
            int i;
            while ((i = inputStream.read()) != -1) {
                if (i == '\n') {
                    header.append((char) i);
                    i = inputStream.read();
                    if (i == '\r') {
                        break;
                    } else {
                        header.append((char) i);
                    }
                } else {
                    header.append((char) i);
                }
            }

            for (String s : header.toString().split("\n")) {
                if (headers == null) {
                    headers = new ArrayList<>();
                }
                if (s.trim().length() > 0)
                    headers.add(s.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 若code为302等，则说明需要重定向
        if (isFollowRedirects) {
            int code = getResponseCode();
            if (code > 300 && code < 310) {
                String newUrl = getHeaderField("location");
                if (newUrl.contains("http")) {
                    try {
                        url = new URL(newUrl);
                        host = url.getHost();
                        port = url.getPort() > 0 ? url.getPort() : 80;
                        protocol = url.getProtocol();
                        socket.close();
                        socket = null;
                        connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        url = new URL(url.getProtocol() + "://" + host + "/" + newUrl);
                        try {
//                            getInputStream().skip(getContentLength());
                            socket.close();
                            socket = null;
                            connect();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }

                headers.clear();
                getHeader();
            }
        }
    }

    /**
     * 返回报头
     *
     * @return
     */
    public ArrayList<String> getHeaders() {
        if (headers == null) {
            getHeader();
        }
        return headers;
    }

    @Override
    public int getResponseCode() {
        if (headers == null) {
            getHeader();
        }

        String s = headers.get(0);
        if (s.startsWith("HTTP")) {
            responseCode = Integer.parseInt(s.split(" ")[1]);
        }

        return responseCode;
    }

    @Override
    public String getResponseMessage() {
        if (headers == null) {
            getHeader();
        }
        String s = headers.get(0);
        if (s.startsWith("HTTP")) {
            s = s.substring(s.indexOf(" ") + 1);
            s = s.substring(s.indexOf(" ") + 1);
            responseMessage = s;
        }
        return responseMessage;
    }

    @Override
    public boolean getInstanceFollowRedirects() {
//        return super.getInstanceFollowRedirects();
        return isFollowRedirects;
    }

    @Override
    public void setInstanceFollowRedirects(boolean followRedirects) {
        super.setInstanceFollowRedirects(followRedirects);
        this.isFollowRedirects = followRedirects;
    }

    @Override
    public String getRequestMethod() {
        return method;
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        this.method = method;
    }

    @Override
    public String getHeaderField(int n) {
        if (headers == null) {
            getHeader();
        }

        if (n < headers.size()) {
            return headers.get(n);
        } else {
            return null;
        }
    }

    @Override
    public String getHeaderField(String name) {
        if (headers == null) {
            getHeader();
        }
        String value = null;
        for (String i : headers) {
            if (i.contains(":")) {
                String tmp = i.substring(0, i.indexOf(':'));
                if (tmp.equalsIgnoreCase(name)) {
                    value = i.substring(i.indexOf(":") + 2);
                }
            }
        }
        return value;
    }


    @Override
    public long getDate() {
        long date = 0;
        String s = getHeaderField("date");
        if (s != null) {
            date = Date.parse(s);
        }
        return date;
    }


    @Override
    public String getHeaderFieldKey(int n) {
        if (headers == null) {
            getHeader();
        }
        if (n == 0 || n >= headers.size()) {
            return null;
        }
        String s = headers.get(n);
        if (s.contains(":")) {
            return s.substring(0, s.indexOf(':'));
        } else {
            return null;
        }
    }

    @Override
    public String getContentEncoding() {
        return getHeaderField("content-encoding");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public String getContentType() {
        return getHeaderField("content-type");
    }

    @Override
    public void setRequestProperty(String key, String value) {
        super.setRequestProperty(key, value);
        requestLine.add(key + ": " + value + "\r\n");
    }

    /**
     * 设置用Post、Put方法时需要发送的报文内容
     *
     * @param data
     */
    public void putExtraData(String data) {
        body = data;
    }
}
