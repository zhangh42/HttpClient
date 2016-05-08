# HttpClient
设计一个简易HTTP客户端

## 起因
本项目的最初目的是为了完成学校的实验作业。但是我想在完成作业的基础上进一步完善它。使他具有基本的HTTP功能。

## MyHttpURLConnection
该类主要是基于Socket实现的。
首先根据传入URL获取protocol、host和port。然后根据protocol是HTTP还是HTTPS来选择用普通的socket类还是用SSLSocket。在之后是发送请求，接受反应报文并且分析报头。
1.	首先要实现的抽象方法是disconnect()、usingProxy()，其中第一个主要用来关闭连接HTTP连接，也就是socket连接。第二个方法暂时不用，为空。
2.	该类的重点在于分析报头，所以在该类中实现了getHeader() 方法，该方法是首先发送请求报文，然后接收响应报文，并将报头一行一行的保存在ArrayList中。
最后会特别的检查状态码，如果状态码是30X，那么该类会提取报头Location中的重定向地址，然后重新建立socket连接（主要有些比较大的网站有多个域名，需要重新建立新的socket连接）并重复上述过程获取报头。
3.	重写setRequestProperty(String key, String value)，该方法主要是用来写请求报文的各项设置。该还输将key和value拼接成标准的报文格式后保存在数组中，待发送请求时再写入请求报头中。
4.	重写public String getHeaderField(String name) ，因为方法在HttpURLConnection中是个空方法，所以需要重新实现。而且该方法是关键方法，实现该方法后很多get方法都可以根据此方法实现。
