package com.da.webservice;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 封装请求
 */
public class Request {
    //    请求路径
    private String url;
    //    请求参数
    private Map<String, String> params;
    //    请求方法
    private String method;

    public String getUrl() {
        return url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getMethod() {
        return method;
    }

    public Request(InputStream is) {
        try {
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
//              获取请求的第一行数据
                String line = br.readLine();
                if (line != null && line.length() > 0) {
                    String[] requestLine = line.split(" ");
                    if (requestLine.length == 3 && requestLine[2].equals("HTTP/1.1")) {
                        this.method = requestLine[0];
//                      处理url
                        String beforeUrl = requestLine[1];
                        if (beforeUrl.contains("?")) {
                            this.params = new HashMap<>();
                            this.url = beforeUrl.substring(0, beforeUrl.indexOf("?"));
//                            处理url中?a=xx&b=xx的参数
                            String beforeParams = beforeUrl.substring(beforeUrl.indexOf("?") + 1);
//                            如果有&
                            if (beforeParams.contains("&")) {
//                                用&分开
                                for (String s : beforeParams.split("&")) {
//                                    用=分开
                                    if (s != null && s.contains("=")) {
                                        String[] split = s.split("=");
                                        this.params.put(split[0], split[1]);
                                    }
                                }
                            } else if (beforeParams.contains("=")) {
                                String[] split = beforeParams.split("=");
                                this.params.put(split[0], split[1]);
                            }
                        } else {
                            this.url = beforeUrl;
                        }
                    }
                    System.out.println("通过 [" + this.method + "] 方法 访问了 [" + this.url + "] 请求参数 [" + this.params + "]");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
