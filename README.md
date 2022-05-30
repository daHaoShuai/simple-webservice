# 非常简单的web服务器

```java
    WebApp app = new WebApp();
//        app.before("/", () -> System.out.println("before"));
//        app.after("/", () -> System.out.println("after"));
//        app.use("/", (req, res) -> res.sendHtml("<h1>hello world</h1>"));
//        静态资源目录,默认就是resources/static目录
//        app.setStatic("static");
//        默认监听8080端口
    app.listen();
//        定义启动后要做的操作
//        app.listen(() -> {
//            System.out.println("服务启动了...");
//        });
//        指定端口监听
//        app.listen(8081);
//        指定端口监听并且定义启动后要做的操作(5秒后关闭服务器)
//        app.listen(8081, () -> {
//            Utils.sleep(5000);
//            app.shutdown();
//        });
//        System.out.println("看到我,服务器就关闭了...");
```
