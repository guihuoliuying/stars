package com.stars.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class I18n {
    public static ResourceBundle StringPool;

    static {
        try {
            StringPool = ResourceBundle.getBundle("yhmessage_zh_CN");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key, Object... args) {
        String message = "国际化请求失败";
        if (StringPool.containsKey(key)) {
            message = StringPool.getString(key);
        } else {
            LogUtil.error("国际化文件中不存在" + key);
        }
        if (args != null) {
            return MessageFormat.format(message, args);
        } else {
            return message;
        }
    }
}
