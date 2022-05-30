package com.da;

import com.da.webservice.WebApp;

public class App {
    public static void main(String[] args) {
        WebApp app = new WebApp();
        app.use("/", (res, resp) -> resp.sendHtml("index.html"));
        app.listen(8080);
    }
}
