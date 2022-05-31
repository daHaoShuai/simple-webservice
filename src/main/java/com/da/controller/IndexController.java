package com.da.controller;

import com.da.webservice.Handler;
import com.da.webservice.Mapping;
import com.da.webservice.Request;
import com.da.webservice.Response;

// mapping会覆盖掉html中的index.html注册的/
@Mapping("/index")
public class IndexController implements Handler {

    @Override
    public void callback(Request request, Response response) {
        response.sendHtml("<h1>hello world</h1>");
    }
}
