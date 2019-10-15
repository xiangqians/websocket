package com.xq.ws.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * @author xiangqian
 */
public class ClassUtils {

    // class file type
    private static final String FILE_TYPE_CLASS = ".class";

    // separator char
    private static final char SEPARATOR_CHAR_PACKAGE = '.';
    private static final char SEPARATOR_CHAR_URL = '/';
    private static final char SEPARATOR_CHAR_FILE = File.separatorChar;
    // separator
    private static final String SEPARATOR_PACKAGE = "" + SEPARATOR_CHAR_PACKAGE;
    private static final String SEPARATOR_URL = "" + SEPARATOR_CHAR_URL;
    private static final String SEPARATOR_FILE = "" + SEPARATOR_CHAR_FILE;

    // protocol
    private static final String PROTOCOL_FILE = "file";
    private static final String PROTOCOL_JAR = "jar";

    // package level
    private static final int PACKAGE_LEVEL_NO = -1;
    private static final int PACKAGE_LEVEL_ALL = 0;
    private static final int PACKAGE_LEVEL_1 = 1;

    /**
     * 查找指定包下所有的全类名
     *
     * @param packageName
     * @return
     * @throws IOException
     */
    public static List<String> getClassNames(String packageName) throws IOException {
        return getClassNames(packageName, true);
    }

    /**
     * 查找指定包下的全类名
     *
     * @param packageName          包名
     * @param isGetAllChildPackage 是，则获取所有子包；否，则获取当前包
     * @return
     */
    public static List<String> getClassNames(String packageName, boolean isGetAllChildPackage) throws IOException {
        List<String> classNames = new ArrayList<>();
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(packageName.replace(SEPARATOR_PACKAGE, SEPARATOR_URL));
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url == null) {
                continue;
            }

            // 协议
            String protocol = url.getProtocol();

            // 项目中的class文件
            if (PROTOCOL_FILE.equals(protocol)) {
                getClassNames(classNames, packageName.replace(SEPARATOR_PACKAGE, SEPARATOR_FILE), new File(url.getPath()), isGetAllChildPackage ? PACKAGE_LEVEL_ALL : PACKAGE_LEVEL_1);
            }
            // 项目中的jar文件
            else if (PROTOCOL_JAR.equals(protocol)) {
                JarFile jarFile = null;
                try {
                    jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                    if (jarFile != null) {
                        getClassNames(classNames, packageName, jarFile, isGetAllChildPackage ? PACKAGE_LEVEL_ALL : PACKAGE_LEVEL_1);
                    }
                } finally {
                    IOUtils.quietlyClosed(jarFile);
                }
            }
        }
        return classNames;
    }

    private static void getClassNames(List<String> classNames, String packagePath, File file, int packageLevel) {
        if (!file.exists()) {
            return;
        }

        // 文件
        if (file.isFile()) {
            String path = file.getPath();
            if (path.endsWith(FILE_TYPE_CLASS)) {
                path = path.substring(path.indexOf(packagePath), path.length() - 6);
                String clazzName = path.replace(SEPARATOR_FILE, SEPARATOR_PACKAGE);
                classNames.add(clazzName);
            }
            return;
        }

        // 文件夹
        File[] listFiles = file.listFiles();
        if (listFiles == null || listFiles.length == 0) {
            return;
        }
        if (packageLevel == PACKAGE_LEVEL_ALL) {
            for (File childFile : listFiles) {
                getClassNames(classNames, packagePath, childFile, PACKAGE_LEVEL_ALL);
            }
        } else if (packageLevel == PACKAGE_LEVEL_1) {
            for (File childFile : listFiles) {
                getClassNames(classNames, packagePath, childFile, PACKAGE_LEVEL_NO);
            }
        }
    }

    private static void getClassNames(List<String> classNames, String packageName, JarFile jarFile, int packageLevel) {
        String packagePath = packageName.replace(SEPARATOR_PACKAGE, SEPARATOR_URL);
        int count = containCharCount(packagePath, SEPARATOR_CHAR_URL);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String relativePath = jarEntry.getName();
            if (relativePath.indexOf(packagePath) == -1 || !relativePath.endsWith(FILE_TYPE_CLASS)) {
                continue;
            }
            if (packageLevel == PACKAGE_LEVEL_ALL) {
                relativePath = relativePath.substring(0, relativePath.length() - 6);
                String clazzName = relativePath.replace(SEPARATOR_URL, SEPARATOR_PACKAGE);
                classNames.add(clazzName);
            } else if (packageLevel == PACKAGE_LEVEL_1 && containCharCount(relativePath, SEPARATOR_CHAR_URL) == (count + 1)) {
                relativePath = relativePath.substring(0, relativePath.length() - 6);
                String clazzName = relativePath.replace(SEPARATOR_URL, SEPARATOR_PACKAGE);
                classNames.add(clazzName);
            }
        }
    }

    private static int containCharCount(String str, char containChar) {
        char[] arr = str.toCharArray();
        int count = 0;
        int len = arr.length;
        for (int i = 0; i < len; i++) {
            if (arr[i] == containChar) {
                count++;
            }
        }
        return count;
    }

}
