package com.da.controller;

import com.da.webservice.Handler;
import com.da.webservice.InitAfter;
import com.da.webservice.InitBefore;
import com.da.webservice.Mapping;
import com.da.webservice.Request;
import com.da.webservice.Response;
import com.da.webservice.WebApp;

// mapping会覆盖掉html中的index.html注册的/
@Mapping("/index")
public class IndexController implements Handler, InitBefore, InitAfter {

    private String name;

    @Override
    public void callback(Request request, Response response) {
        response.sendHtml("<h1>hello world" + name + "</h1>");
    }

    @Override
    public void before(WebApp app) {
//        访问/index前会执行
        app.before("/index", () -> System.out.println("hello"));
        this.name = "老王";
        System.out.println("注册/index路由前会执行");
        app.after("/index", () -> System.out.println("world"));
    }

    @Override
    public void after(WebApp app) {
        this.name = "小王";
        System.out.println("注册/index路由后会执行");
    }
}
