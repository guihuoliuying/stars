package com.stars.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class LoginUtil {
    private static String KEY = "yh8787";

    public static String checkKey(String uid, String key) {
        GregorianCalendar now = new GregorianCalendar();
        byte dayOfMonth = (byte) now.get(Calendar.DAY_OF_MONTH);
        byte dayOfWeek = (byte) now.get(Calendar.DAY_OF_WEEK);
        byte hour = (byte) now.get(Calendar.HOUR);
        byte min = (byte) now.get(Calendar.MINUTE);
        byte second = (byte) now.get(Calendar.SECOND);
        String minSecond = key.substring(key.length() - 4);
        byte keyMin = Byte.parseByte(minSecond.substring(0, 2));
        byte keySecond = Byte.parseByte(minSecond.substring(2));
        // 间隔必须是2分钟以内
        int interval = min * TimeUtil.MINUTE_SECOND + second - keyMin * TimeUtil.MINUTE_SECOND - keySecond;
        if (interval > TimeUtil.MINUTE_SECOND * 10) {
            return "会话已过期, 请重新登陆";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(KEY).append(dayOfWeek).append(dayOfMonth).append(uid).append(hour);
        String obj = Md5Util.getMD5Str(sb.toString()) + minSecond;
        if (obj.equals(key)) {
            return null;
        }
        return "会话未经登陆服验证, 请重新登陆";
    }

}
