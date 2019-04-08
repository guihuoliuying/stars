package com.stars.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by zd on 2016/3/31.
 */
public class FileUtil {

    public static String readFile(String path)
            throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File(path)));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            throw e;
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

}
