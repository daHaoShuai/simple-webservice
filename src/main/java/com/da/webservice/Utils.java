package com.da.webservice;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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
}
