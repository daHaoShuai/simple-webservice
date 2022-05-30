package com.da.webservice;

/**
 * 消费请求和响应
 */
@FunctionalInterface
public interface Handler {
    void callback(Request request, Response response);
}
