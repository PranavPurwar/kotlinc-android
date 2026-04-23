package com.sun.tools.javac;

import com.sun.nio.zipfs.ZipFileSystemProvider;

import java.nio.file.spi.FileSystemProvider;

import jdk.internal.jrtfs.JrtFileSystemProvider;

public class ConfigProvider {
    public static String JAVA_HOME = "";

    public static void setJavaHome(String home) {
        JAVA_HOME = home;
    }

    public static String getJavaHome() {
        return JAVA_HOME;
    }

    public static FileSystemProvider jrtFsProvider = new JrtFileSystemProvider();

    public static FileSystemProvider zipFsProvider = new ZipFileSystemProvider();
}
