package com.da.webservice;

import java.io.File;
import java.util.List;
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
}
