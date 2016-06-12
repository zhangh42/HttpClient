package HttpClient;

import java.io.*;
import java.net.URL;

/**
 * Created by zhang on 2016/6/10.
 */
public class HttpClient {
    private MyHttpURLConnection httpURLConnection;


    public HttpClient(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        httpURLConnection = new MyHttpURLConnection(url);
    }

    public static void main(String[] args) throws IOException {
        String url = new String("http://localhost/2333.exe");
        HttpClient client = new HttpClient(url);
        System.out.println(client.getHeader());
        String html = client.open();
        if (html != null) {
            System.out.println(html);
        }
    }

    /**
     * 打开链接，如果是html，则放回html的内容，如果请求的是其他文件，则返回null
     *
     * @return
     */
    public String open() throws IOException {
        String type = httpURLConnection.getContentType();
        if (type.contains("html")) {
            return getHtml();
        } else {
            downloadFile();
        }
        return null;
    }

    /**
     * 返回报头
     */
    public String getHeader() {
        StringBuilder stringBuilder = new StringBuilder(100);
        httpURLConnection.getHeaders().stream()
                .forEach(s -> stringBuilder.append(s).append("\n"));
        return stringBuilder.toString();
    }

    public String getHtml() throws IOException {
        StringBuilder stringBuilder = new StringBuilder(5000);
        int len = httpURLConnection.getContentLength();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream()));
        int nowLen = 0;
        String s;
        while ((s = reader.readLine()) != null) {
            stringBuilder.append(s).append("\n");
            nowLen += s.getBytes().length;
            if (nowLen >= len) break;
        }
        return stringBuilder.toString();
    }

    public void downloadFile() throws IOException {
        String filename = httpURLConnection.getURL().getPath();
        filename = filename.substring(filename.lastIndexOf('\\') + 1);
        File file = new File("C:\\Users\\zhang\\Desktop\\" + filename);
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        InputStream inputStream = httpURLConnection.getInputStream();
        int len = httpURLConnection.getContentLength();
        int nowlen = 0;
        byte[] bytes = new byte[1024];
        int l;
        while ((l = inputStream.read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, l);
            nowlen += l;
            if (nowlen >= len) break;
            ;
        }
        fileOutputStream.close();
    }
}
