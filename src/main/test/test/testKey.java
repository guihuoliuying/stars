package test;

import com.stars.util.Md5Util;
import com.stars.util.TimeUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class testKey {
	public static String KEY = "yh8787";

    private static boolean checkKey(String uid, String key) {
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
        if (interval > TimeUtil.MINUTE_SECOND * 5)
            return false;
        StringBuffer sb = new StringBuffer();
        sb.append(KEY).append(dayOfWeek).append(dayOfMonth).append(uid).append(hour);
        String obj = Md5Util.getMD5Str(sb.toString()) + minSecond;
        System.err.println(obj);
        if (obj.equals(key))
            return true;
        return false;
    }
    
    public static void main(String[] args){
    	System.err.println(checkKey("67022393","35ed0662102573606f87aa5751b2f35b0909"));
    }
}
