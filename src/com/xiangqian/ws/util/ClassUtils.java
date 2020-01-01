package com.xiangqian.ws.util;

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
 * Class工具类：获取指定包下的类名、。
 *
 * @author xiangqian
 * @date 15:41 2020/01/01
 */
public class ClassUtils {

    // class file type
    private static final String FILE_TYPE_CLASS = ".class";

    // separator char
    private static final char SEPARATOR_CHAR_PACKAGE = '.';
    private static final char SEPARATOR_CHAR_URL = '/';

    // separator
    private static final String SEPARATOR_PACKAGE = "" + SEPARATOR_CHAR_PACKAGE;
    private static final String SEPARATOR_URL = "" + SEPARATOR_CHAR_URL;
    private static final String SEPARATOR_FILE = File.separator;

    // protocol
    private static final String PROTOCOL_FILE = "file";
    private static final String PROTOCOL_JAR = "jar";


    /**
     * 查找指定包下所有的全类名
     *
     * @param packageName 包名
     * @return
     * @throws IOException
     */
    public static List<String> getClassNames(String packageName) throws IOException {
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
                File file = new File(url.getPath());
                getClassNames(classNames, packageName, file);
            }
            // 项目中的jar文件
            else if (PROTOCOL_JAR.equals(protocol)) {
                JarFile jarFile = null;
                try {
                    jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                    if (jarFile != null) {
                        getClassNames(classNames, packageName, jarFile);
                    }
                } finally {
                    IOUtils.quietlyClosed(jarFile);
                }
            }
        }
        return classNames;
    }

    /**
     * @param classNames
     * @param packageName
     * @param file
     */
    private static void getClassNames(List<String> classNames, String packageName, File file) {
        getClassNames0(classNames, packageName.replace(SEPARATOR_PACKAGE, SEPARATOR_FILE), file);
    }

    /**
     * @param classNames
     * @param packagePath
     * @param file
     */
    private static void getClassNames0(List<String> classNames, String packagePath, File file) {
        if (!file.exists()) {
            return;
        }

        // 文件
        if (file.isFile()) {
            String path = file.getPath();
            if (path.endsWith(FILE_TYPE_CLASS)) {
                path = path.substring(path.indexOf(packagePath), path.length() - FILE_TYPE_CLASS.length());
                String clazzName = path.replace(SEPARATOR_FILE, SEPARATOR_PACKAGE);
                classNames.add(clazzName);
            }
            return;
        }

        // 文件夹
        File[] childFiles = file.listFiles();
        if (childFiles != null && childFiles.length > 0) {
            for (File childFile : childFiles) {
                getClassNames0(classNames, packagePath, childFile);
            }
        }
    }

    /**
     * @param classNames
     * @param packageName
     * @param jarFile
     */
    private static void getClassNames(List<String> classNames, String packageName, JarFile jarFile) {
        String packagePath = packageName.replace(SEPARATOR_PACKAGE, SEPARATOR_URL);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String classFileRelativePath = jarEntry.getName();
            if (classFileRelativePath.startsWith(packagePath) && classFileRelativePath.endsWith(FILE_TYPE_CLASS)) {
                classFileRelativePath = classFileRelativePath.substring(0, classFileRelativePath.length() - FILE_TYPE_CLASS.length());
                classNames.add(classFileRelativePath.replace(SEPARATOR_URL, SEPARATOR_PACKAGE));
            }
        }
    }

}
