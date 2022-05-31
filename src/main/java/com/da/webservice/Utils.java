package com.da.webservice;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工具类
 */
public class Utils {
    //    休眠指定时间
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //    判断字符串是不是为空
    public static boolean isBlank(String str) {
        return str != null && !"".equals(str);
    }

    //    判断字符串不是为空
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    //    扫描指定文件路径下面的所有文件的方法
    public static void getAllFileToPath(File rootFile, List<File> files) {
        if (rootFile.exists()) {
            if (rootFile.isDirectory()) {
                for (File file : Objects.requireNonNull(rootFile.listFiles())) {
                    if (file.isDirectory()) {
                        getAllFileToPath(file, files);
                    } else {
                        files.add(file);
                    }
                }
            } else {
                files.add(rootFile);
            }
        }
    }

    //    扫描指定文件路径下面的所有文件的方法
    public static void getAllFileToPath(String filePath, List<File> files) {
        File file = new File(filePath);
        getAllFileToPath(file, files);
    }

    //    处理扫描出来的文件到对应的map中
    public static void handlerFileToMap(File file, String parentPathName, Map<String, String> map) throws IOException {
//     获取当前文件的父文件名字
        String parentName = file.getParentFile().getName();
//      文件的路径
        String path;
        if (parentPathName.equals(parentName)) {
//          当前文件对应的路径
            path = "/" + file.getName();
        } else {
            path = "/" + parentName + "/" + file.getName();
        }
//      文件的类型
        String type = Files.probeContentType(file.toPath());
//      加到静态文件路径和类型的map中去
        map.put(path, type);
    }

    //    处理扫描出来的文件到对应的map中
    public static void handlerFileToPathToMap(File file, String parentPathName, Map<String, String> map) throws IOException {
//     获取当前文件的父文件名字
        String parentName = file.getParentFile().getName();
//      文件的路径
        String path;
        String name = file.getName();
        if (parentPathName.equals(parentName)) {
            if ("index.html".equals(name)) {
                path = "/";
            } else {
//          当前文件对应的路径
                path = "/" + name.substring(0, name.indexOf("."));
            }
        } else {
            path = "/" + parentName + "/" + name.substring(0, name.indexOf("."));
        }
//      文件的类型
        String type = Files.probeContentType(file.toPath());
//      加到静态文件路径和类型的map中去
        map.put(path, type);
    }

    //    获取静态目录下的文件
    public static File getResourcesFile(String path) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        assert url != null;
        return new File(url.getFile());
    }

    //    扫描有注解并且实现了Handler接口的类注入到路由映射中去
    public static void initApp(Class<?> clz, WebApp app) {
//        要扫描的包名
        String packageName = clz.getPackage().getName();
//        要扫描的文件根路径
        File rootPath = getResourcesFile(packageName.replace('.', '/'));
//        保存扫描出来的文件
        List<File> files = new ArrayList<>();
        getAllFileToPath(rootPath, files);
//        保存扫描出来的可以加载的类名
        List<String> classNames = new ArrayList<>();
//        把文件变成可以加载的类名
        handlerFileToClass(files, packageName, classNames);
//        处理符合的className注册到路由表中
        handlerClassName(classNames, clz.getClassLoader(), app);
    }

    //        处理扫描出来的符合的class
    private static void handlerFileToClass(List<File> files, String packageName, List<String> classNames) {
        String parentFileName = packageName.substring(packageName.lastIndexOf('.') + 1);
        files.forEach(file -> {
//            获取当前文件的名字
            String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
//            获取当前文件的父文件的名字
            String parentName = file.getParentFile().getName();
//            扫描出来的能加载的类名
            String className;
//            如果父路径和包名的最后一级一样说明是最外层的类
            if (parentFileName.equals(parentName)) {
//                拼接要加载的类名
                className = packageName + "." + fileName;
            } else {
                className = packageName + "." + parentName + "." + fileName;
            }
//            加到集合中去
            classNames.add(className);
        });
    }

    //        处理符合的className
    private static void handlerClassName(List<String> classNames, ClassLoader classLoader, WebApp app) {
        classNames.forEach(className -> {
            try {
                Class<?> clz = classLoader.loadClass(className);
//                判断这个类有没有Mapping注解
                if (clz.isAnnotationPresent(Mapping.class)) {
//                    获取路径
                    String path = clz.getAnnotation(Mapping.class).value();
//                    获取当前类实现的所有接口
                    Class<?>[] interfaces = clz.getInterfaces();
//                    判断当前类是不是实现了Handler接口
                    if (Arrays.asList(interfaces).contains(Handler.class)) {
//                        实例化当前类
                        Handler instance = (Handler) clz.getConstructor().newInstance();
//                        注册到路由表中
                        app.use(path, instance);
                    }
                }
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
    }

}
