package com.da;

import com.da.webservice.WebApp;

public class App {
    public static void main(String[] args) {
        WebApp app = new WebApp();
        app.use("/", (res, resp) -> resp.sendHtml("<h1>hello</h1>"));
//        静态资源目录,默认就是resources/static目录
//        app.setStatic("static");
        app.listen(8080);
    }
}
