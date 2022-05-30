package com.da.webservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
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
    //    标记服务器是否开启
    private boolean isOpen = true;

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
        URL url = this.getClass().getClassLoader().getResource(path);
        assert url != null;
        File staticPath = new File(url.getFile());
        try {
//            扫描添加
            getFiles(staticPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    处理当前文件夹下的所有文件
    private void getFiles(File staticPath) throws IOException {
        if (staticPath.exists()) {
            if (staticPath.isDirectory()) {
                for (File file : Objects.requireNonNull(staticPath.listFiles())) {
                    if (file.isDirectory()) {
                        getFiles(file);
                    } else {
                        handlerFileToMap(file);
                    }
                }
            } else {
                handlerFileToMap(staticPath);
            }
        }
    }

    //    处理扫描出来的文件到对应的map中
    private void handlerFileToMap(File file) throws IOException {
//                        获取当前文件的父文件名字
        String parentName = file.getParentFile().getName();
//                        文件的路径
        String path;
        if (staticPathName.equals(parentName)) {
//                            当前文件对应的路径
            path = "/" + file.getName();
        } else {
            path = "/" + parentName + "/" + file.getName();
        }
//                        文件的类型
        String type = Files.probeContentType(file.toPath());
//                        加到静态文件路径和类型的map中去
        staticFiles.put(path, type);
    }

    //    开启监听
    public void listen(int port) {
        listen(port, () -> {
        });
    }

    //    @SuppressWarnings("InfiniteLoopStatement") // 去掉循环的黄线提示
    public void listen(int port, MyConsumer consumer) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            printInitMessage(port);
//            加载静态资源目录,服务启动起来先加载默认的静态资源目录
            this.setStatic(this.staticPathName);
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
                        else if (staticFiles.containsKey(request.getUrl())) {
//                           获取当前文件的类型
                            String type = staticFiles.get(request.getUrl());
                            URL url = this.getClass().getClassLoader().getResource(staticPathName + request.getUrl());
                            assert url != null;
                            File file = new File(url.getFile());
                            FileInputStream is = new FileInputStream(file);
                            response.setStatus(200)
                                    .setHeaders("Content-Type: ", type + ";charset=utf-8")
                                    .send(is);
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

    //    关闭服务器
    public void shutdown() {
        pool.shutdownNow();
        isOpen = false;
    }

    //    打印初始化信息
    private void printInitMessage(int port) throws UnknownHostException {
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
    }

}
