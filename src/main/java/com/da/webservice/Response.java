package com.da.webservice;

import java.io.BufferedReader;
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

    //    发送数据
    public void send(String data) {
        try {
            StringBuilder dataStr = new StringBuilder();
            dataStr.append("HTTP/1.1 ")
                    .append(this.status)
                    .append("\n");

            headers.forEach((k, v) -> {
                dataStr.append(k)
                        .append(": ")
                        .append(v)
                        .append("\n");
            });

            dataStr.append("\n").append(data);

            os.write(dataStr.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    发送网页文件
    public void sendHtml(String path) {
        try {
            StringBuilder body = new StringBuilder();
            body.append("HTTP/1.1 ")
                    .append(200)
                    .append("\n")
                    .append("Content-Type: ")
                    .append("text/html;charset=utf-8")
                    .append("\n\n");
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
            assert is != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            br.lines().forEach(body::append);
            is.close();
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
