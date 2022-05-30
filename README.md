# 非常简单的web服务器

```java
    WebApp app = new WebApp();
    app.use("/", (res, resp) -> resp.sendHtml("index.html"));
    app.listen(8080);
```
