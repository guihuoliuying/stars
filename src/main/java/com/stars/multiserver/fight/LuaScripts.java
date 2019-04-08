package com.stars.multiserver.fight;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/8.
 */
public class LuaScripts {

    private static String basepath = System.getProperty("user.dir") + "/config/lua";
    private static Map<String, String> scripts = new HashMap<>(); // filename -> text

    public static void loadAll() throws IOException {
        load(basepath);
    }

    private static void load(String filename) throws IOException {
        File file = new File(filename);
        if (file.isFile() && filename.endsWith(".lua")) {
            loadScript(filename);
        } else if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                load(filename + "/" + child);
            }
        }
    }

    private static void loadScript(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            if (!line.endsWith("\n")) {
                sb.append("\n");
            }
        }
        br.close();
        scripts.put(filename.substring(basepath.length() + 1, filename.length() - 4), sb.toString());
    }

    public static String get(String key) {
        if (scripts.containsKey(key)) {
            return scripts.get(key);
        } else {
            return "";
        }
    }

}
