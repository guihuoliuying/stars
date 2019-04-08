package com.stars;

import com.stars.util.LogUtil;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * 服务版本号
 * <p/>
 * Created by zd on 2016/4/11.
 */
public class ServerVersion {

    private final static String CONFIG_PATH = "./config/server_version.properties";

    private volatile static boolean Load = false;
    private volatile static int BIG_VERSION;
    private volatile static int SMALL_VERSION;

    public static void load() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(CONFIG_PATH));
        String temp = properties.getProperty("server.version");
        if (temp == null) {
            throw new Exception("服务缺少版本号配置");
        }
        String array[] = temp.trim().split("\\.");
        try {
            if (array.length != 2) {
                throw new Exception("格式错误" + temp);
            }
            int bigVersion = Integer.parseInt(array[0].trim());
            int smallVersion = Integer.parseInt(array[1].trim());
            BIG_VERSION = bigVersion;
            SMALL_VERSION = smallVersion;
            Load = true;
            LogUtil.info("加载服务版本号成功，BIG_VERSION = {}, SMALL_VERSION = {}", BIG_VERSION, SMALL_VERSION);
        } catch (Exception e) {
            throw new Exception("解析服务版本号异常", e);
        }
    }



    public static int getBigVersion() {
        if (!Load)
            throw new NullPointerException("服务没有加载版本号");
        return BIG_VERSION;
    }

    public static int getSmallVersion() {
        if (!Load)
            throw new NullPointerException("服务没有加载版本号");
        return SMALL_VERSION;
    }

}
