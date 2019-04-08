package com.stars.server.login2.helper;

import com.google.gson.Gson;
import com.stars.util.Md5Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/1/29.
 */
public class LTokenHelper {

    private static String padding = "TheDamnHacker,HereYouAre,Fuck!!!";
    private static Gson gson = new Gson();

    public static String makeToken(String userId, long timestamp) {
        String md5 = com.stars.util.Md5Util.getMD5Str(formPlainText(userId, timestamp));
        Map<String, String> map = new HashMap<>();
        map.put("uniqueId", userId);
        map.put("timestamp", Long.toString(timestamp));
        map.put("md5", md5);
        return gson.toJson(map);
    }

    public static Map<String, String> toMap(String token) {
        return gson.fromJson(token, HashMap.class);
    }

    public static boolean verifyToken(String userId, long timestamp, String md5) {
        return md5.equals(com.stars.util.Md5Util.getMD5Str(formPlainText(userId, timestamp)));
    }

    public static String formPlainText(String userId, long timestamp) {
        return new StringBuilder()
                .append(userId).append("-")
                .append(timestamp).append("-")
                .append(padding).toString();
    }

    public static void main(String[] args) {
        System.out.println(makeToken("Godson", 1290L));
        System.out.println(toMap(makeToken("Godson", 1290L)));

        String md5 = null;
        long s, e;

        for (int j = 0; j < 1000; j++) {
            s = System.currentTimeMillis();
            for (int i = 0; i < 2000000; i++) {
                md5 = com.stars.util.Md5Util.getMD5Str(Integer.toString(i));
            }
            e = System.currentTimeMillis();
            System.out.println("time: " + (e -s) + ", md5: " + md5);
        }

        System.out.println("md5: " + Md5Util.getMD5Str("zirencheng"));

    }

}
