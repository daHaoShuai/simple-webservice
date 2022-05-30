package com.da.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 封装响应
 */
public class Response {
    //    http协议版本
    public final static String HTTP_VERSION = "HTTP/1.1";
    //    成功的状态码
    public final static int OK = 200;
    //    失败的状态码
    public final static int ERR = 500;
    //    找不到的状态码
    public final static int NOTFOUND = 404;
    //    content-type的html类型
    public final static String CONTENT_TYPE_HTML = "Content-Type: text/html;charset=utf-8";
    //    content-type的文本类型
    public final static String CONTENT_TYPE_TEXT = "Content-Type: text/plain;charset=utf-8";
    //    content-type的xml类型
    public final static String CONTENT_TYPE_XML = "Content-Type: text/xml;charset=utf-8";
    //    content-type的gif图片类型
    public final static String CONTENT_TYPE_GIF = "Content-Type: image/gif;charset=utf-8";
    //    content-type的jpg图片类型
    public final static String CONTENT_TYPE_JPG = "Content-Type: image/jpeg;charset=utf-8";
    //    content-type的png图片类型
    public final static String CONTENT_TYPE_PNG = "Content-Type: image/png;charset=utf-8";
    //    content-type的json类型
    public final static String CONTENT_TYPE_JSON = "Content-Type: application/json;charset=utf-8";


    //    输出流
    private final OutputStream os;
    //    响应头
    private final Map<String, String> headers = new HashMap<>();
    //    状态码
    private int status;

    public Response(OutputStream os) {
        this.os = os;
    }

    public Response setHeaders(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public Response setStatus(int code) {
        this.status = code;
        return this;
    }

    //    发送自定义的响应数据
    public void send(String data) {
        StringBuilder dataStr = new StringBuilder();
        dataStr.append(Response.HTTP_VERSION)
                .append(" ")
                .append(this.status)
                .append("\n");
        //        添加响应头
        headers.forEach((k, v) -> dataStr.append(k)
                .append(": ")
                .append(v)
                .append("\n"));
        //        添加响应数据
        dataStr.append("\n").append(data);
        write(dataStr.toString());
    }

    public void send(InputStream is) {
        StringBuilder dataStr = new StringBuilder();
        dataStr.append(Response.HTTP_VERSION)
                .append(" ")
                .append(this.status)
                .append("\n");
        //        添加响应头
        headers.forEach((k, v) -> dataStr.append(k)
                .append(": ")
                .append(v)
                .append("\n"));

        //        添加响应数据
        dataStr.append("\n");
        try {
            os.write(dataStr.toString().getBytes(StandardCharsets.UTF_8));
            byte[] buff = new byte[1024 * 8];
            int len;
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
            }
            is.close();
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //    发送文字类型的数据
    public void sendText(String data) {
        // 拼接响应数据
        String textData = Response.HTTP_VERSION +
                " " + Response.OK + "\n" + Response.CONTENT_TYPE_TEXT + "\n\n" + data;
        write(textData);
    }

    //    发送网页类型的数据
    public void sendHtml(String data) {
        //    拼接响应数据
        String htmlData = Response.HTTP_VERSION +
                " " + Response.OK + "\n" + Response.CONTENT_TYPE_HTML + "\n\n" + data;
        write(htmlData);
    }

    //    发送网页文件
    public void sendHtmlFile(String path) {
        try {
            StringBuilder body = new StringBuilder();
            body.append(HTTP_VERSION)
                    .append(" ")
                    .append(Response.OK)
                    .append("\n")
                    .append(Response.CONTENT_TYPE_HTML)
                    .append("\n\n");
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
            assert is != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            br.lines().forEach(body::append);
            is.close();
            write(body.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    发送json类型的数据
    public void sendJson(String data) {
        // 拼接响应数据
        String jsonData = Response.HTTP_VERSION +
                " " + Response.OK + "\n" + Response.CONTENT_TYPE_JSON + "\n\n" + data;
        write(jsonData);
    }

    //    写数据到浏览器
    private void write(String data) {
        try {
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
