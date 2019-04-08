package com.stars.server.login.util;

import com.stars.util.LogUtil;

import java.security.MessageDigest;

/**
 * Created by liuyuheng on 2015/12/28.
 */
public class Md5Util {

    public static String getMD5Str(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
            byte[] byteArray = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < byteArray.length; i++) {
                String t = Integer.toHexString(0xFF & byteArray[i]);
                if (t.length() == 1)
                    stringBuilder.append("0");
                stringBuilder.append(t);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
        	LogUtil.error(e.getMessage());
        }
        return null;
    }
}
