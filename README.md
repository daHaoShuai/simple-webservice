# 非常简单的web服务器

```java
    WebApp app = new WebApp();
    app.before("/", () -> System.out.println("hello"));
    app.after("/", () -> System.out.println("world"));
    app.use("/", (req, res) -> res.sendHtml("<h1>hello world</h1>"));
//        静态资源目录,默认就是resources/static目录
//        app.setStatic("static");
//        app.listen(8080);
//        启动后5秒后关闭
    app.listen(8080, () -> {
    Utils.sleep(5000);
    app.shutdown();
    });
    System.out.println("看到我,服务器就关闭了...");
```
