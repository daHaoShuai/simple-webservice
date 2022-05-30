package com.da.webservice;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 启动器,管理路由和处理器和监听端口
 */
public class WebApp {
    //    记录路由到回调方法的映射
    private final Map<String, Handler> routes = new HashMap<>();
    //    使用线程池执行任务
    private final ExecutorService pool = Executors.newCachedThreadPool();

    //    把路径映射添加到Map中去
    public void use(String path, Handler handler) {
        routes.put(path, handler);
    }

    //    开启监听
    public void listen(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("服务启动成功:");
            System.out.println("\t> 本地访问: http://localhost:" + port);
            System.out.println("\t> 网络访问: http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port);
            while (true) {
//                获取连接
                Socket socket = serverSocket.accept();
                pool.execute(() -> {
                    try {
                        Request request = new Request(socket.getInputStream());
                        Response response = new Response(socket.getOutputStream());
//                        判断路由表中有没有对应的路由
                        if (routes.containsKey(request.getUrl())) {
//                            给处理器传入请求和响应对象
                            routes.get(request.getUrl()).callback(request, response);
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

}
