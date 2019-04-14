package com.stars.util.makeitrun;

import java.io.File;
import java.nio.file.Paths;

public class Vo2Table {

    public static void main(String[] args) {
        File root = Paths.get(".").toFile();
        walk(root);
    }

    private static void walk(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isFile()
                        && child.getAbsolutePath().endsWith("Vo.java")) {
                    String p = child.getAbsolutePath();
                    String className = p.substring(p.indexOf("/src/main/java/") + 15, p.length() - 5)
                            .replace('/', '.');
                    System.out.println(className);
                } else {
                    walk(child);
                }
            }
        }
    }


}
