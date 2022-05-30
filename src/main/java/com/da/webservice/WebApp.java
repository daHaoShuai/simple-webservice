package com.da.webservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 启动器,管理路由和处理器和监听端口
 */
public class WebApp {
    //    记录路由到回调方法的映射
    private final Map<String, Handler> routes = new HashMap<>();
    //    路由前置操作
    private final Map<String, MyConsumer> beforeHandlers = new HashMap<>();
    //    路由后置置操作
    private final Map<String, MyConsumer> afterHandlers = new HashMap<>();
    //    使用线程池执行任务
    private final ExecutorService pool = Executors.newCachedThreadPool();
    //    静态文件目录,默认就是static目录,可以更改
    private String staticPathName = "static";
    //    静态文件路径和类型
    private final Map<String, String> staticFiles = new HashMap<>();
    //    网页文件目录,可以更改
    private String htmlPathName = "html";
    //    静态文件路径和类型,有可能html目录下有不是网页的文件
    private final Map<String, String> htmlFiles = new HashMap<>();
    //    标记服务器是否开启
    private boolean isOpen = true;
    //    默认端口
    private final int PORT = 8080;

    //    把路径映射添加到Map中去
    public void use(String path, Handler handler) {
        routes.put(path, handler);
    }

    //    路由前置处理
    public void before(String path, MyConsumer consumer) {
        beforeHandlers.put(path, consumer);
    }

    //    路由后置处理
    public void after(String path, MyConsumer consumer) {
        afterHandlers.put(path, consumer);
    }

    //    扫描静态文件目录添加到staticFiles列表中去
    public void setStatic(String path) {
//        更新的静态资源目录
        this.staticPathName = path;
        try {
//            扫描添加
            getStaticFilesToMap(Utils.getResourcesFile(path), this.staticFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    处理静态资源文件夹下的所有文件
    private void getStaticFilesToMap(File rootPath, Map<String, String> map) throws IOException {
        if (rootPath.exists()) {
            if (rootPath.isDirectory()) {
                for (File file : Objects.requireNonNull(rootPath.listFiles())) {
                    if (file.isDirectory()) {
                        getStaticFilesToMap(file, map);
                    } else {
//                        处理扫描出来的文件到对应的map中
                        Utils.handlerFileToMap(file, this.staticPathName, map);
                    }
                }
            } else {
//                处理扫描出来的文件到对应的map中
                Utils.handlerFileToMap(rootPath, this.staticPathName, map);
            }
        }
    }


    //    扫描html目录,注册到路由中去
    public void autoHandlerHtml(String path) {
//        更新网页文件目录
        this.htmlPathName = path;
        try {
            getHtmlFilesToMap(Utils.getResourcesFile(path), this.htmlFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    处理html目录下的文件
    private void getHtmlFilesToMap(File rootPath, Map<String, String> map) throws IOException {
        if (rootPath.exists()) {
            if (rootPath.isDirectory()) {
                for (File file : Objects.requireNonNull(rootPath.listFiles())) {
                    if (file.isDirectory()) {
                        getHtmlFilesToMap(file, map);
                    } else {
//                        处理扫描出来的文件到对应的map中
                        Utils.handlerFileToPathToMap(file, this.htmlPathName, map);
                    }
                }
            } else {
//                处理扫描出来的文件到对应的map中
                Utils.handlerFileToPathToMap(rootPath, this.htmlPathName, map);
            }
        }
    }


    //    开启监听
    public void listen() {
        listen(this.PORT);
    }

    public void listen(MyConsumer consumer) {
        listen(this.PORT, consumer);
    }

    public void listen(int port) {
        listen(port, () -> {
        });
    }

    //    @SuppressWarnings("InfiniteLoopStatement") // 去掉循环的黄线提示
    public void listen(int port, MyConsumer consumer) {
        try {
            long startTime = System.currentTimeMillis();
//            不知道有没有用,反正加上也没事
            System.setProperty("java.awt.headless", Boolean.toString(true));
            ServerSocket serverSocket = new ServerSocket(port);
//            加载静态资源目录,服务启动起来先加载默认的静态资源目录
            this.setStatic(this.staticPathName);
            System.out.println("加载静态资源目录完成 耗时: " + (System.currentTimeMillis() - startTime) + "ms");
//            加载html文件映射
            this.autoHandlerHtml(this.htmlPathName);
            System.out.println("加载html文件目录完成 耗时: " + (System.currentTimeMillis() - startTime) + "ms");
            printInitMessage(port, startTime);
            consumer.accept();
            while (isOpen) {
//                获取连接
                Socket socket = serverSocket.accept();
//                使用线程池执行任务
                pool.execute(() -> {
                    try {
                        Request request = new Request(socket.getInputStream());
                        Response response = new Response(socket.getOutputStream());
//                        判断路由表中有没有对应的路由
                        if (routes.containsKey(request.getUrl())) {
//                          前置处理
                            if (beforeHandlers.containsKey(request.getUrl())) {
                                beforeHandlers.get(request.getUrl()).accept();
                            }
//                            给处理器传入请求和响应对象
                            routes.get(request.getUrl()).callback(request, response);
//                          后置处理
                            if (afterHandlers.containsKey(request.getUrl())) {
                                afterHandlers.get(request.getUrl()).accept();
                            }
                        }
//                      判断静态资源路径有没有对应的路径
                        else if (this.staticFiles.containsKey(request.getUrl())) {
//                            写静态内容到浏览器
                            handlerFileToWrite(request, response, this.staticFiles, this.staticPathName);
                        }
//                        处理html文件夹下的映射
                        else if (this.htmlFiles.containsKey(request.getUrl())) {
//                            写静态内容到浏览器
                            handlerFileToWrite(request, response, this.htmlFiles, this.htmlPathName);
                        } else {
                            response.setStatus(404)
                                    .setHeaders("Content-Type", "text/html")
                                    .send("<h1 style='color:red;'>" + request.getUrl() + " is not found</h1>");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    处理文件写到浏览器
    private void handlerFileToWrite(Request req, Response res, Map<String, String> files, String name) throws Exception {
        // 获取当前文件的类型
        String type = files.get(req.getUrl());
        FileInputStream is = null;
//        判断是静态资源还是html目录下的文件
        if (this.staticPathName.equals(name)) {
            URL url = this.getClass().getClassLoader().getResource(name + req.getUrl());
            assert url != null;
            File file = new File(url.getFile());
            is = new FileInputStream(file);
        } else if (this.htmlPathName.equals(name)) {
//            index.html
            if ("/".equals(req.getUrl())) {
                URL url = this.getClass().getClassLoader().getResource(name + req.getUrl() + "index.html");
                assert url != null;
                File file = new File(url.getFile());
                is = new FileInputStream(file);
            } else {
                URL url = this.getClass().getClassLoader().getResource(name + req.getUrl() + ".html");
                assert url != null;
                File file = new File(url.getFile());
                is = new FileInputStream(file);
            }
        }
        assert is != null;
//        写文件内容到浏览器
        res.setStatus(200)
                .setHeaders("Content-Type: ", type + ";charset=utf-8")
                .send(is);
    }

    //    关闭服务器
    public void shutdown() {
        pool.shutdownNow();
        isOpen = false;
    }

    //    打印初始化信息
    private void printInitMessage(int port, long startTime) throws UnknownHostException {
        String banner = "    .___                      ___.    \n" +
                "  __| _/____    __  _  __ ____\\_ |__  \n" +
                " / __ |\\__  \\   \\ \\/ \\/ // __ \\| __ \\ \n" +
                "/ /_/ | / __ \\_  \\     /\\  ___/| \\_\\ \\\n" +
                "\\____ |(____  /   \\/\\_/  \\___  >___  /\n" +
                "     \\/     \\/               \\/    \\/ \n";
        // 打印banner图
        System.out.println(banner);
        System.out.println("服务启动成功:");
        System.out.println("\t> 本地访问: http://localhost:" + port);
        System.out.println("\t> 网络访问: http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port);
        System.out.println("\t启动总耗时: " + (System.currentTimeMillis() - startTime) + "ms\n");
    }

}
