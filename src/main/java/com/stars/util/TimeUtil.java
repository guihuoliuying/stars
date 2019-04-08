package com.stars.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {

    /* 常用时间戳定义 */
    public static long SECOND = 1000;// 秒
    public static long MINUTE = 60 * SECOND;// 分
    public static long HOUR = 60 * MINUTE;// 时
    public static long DAY = 24 * HOUR;// 天

    public static byte MINUTE_SECOND = 60;//1分钟=60m

    public static long HALF_HOUR = 30 * MINUTE;// 半小时

    //
//    time_second=秒
//    		time_minute=分钟
//    		time_hour=小时
//    		time_day=天
//    		time_month=月
    public static String getTimeDesc(long time) {
        StringBuilder buff = new StringBuilder();
        int day = (int) (time / DAY);
        if (day > 0) {
            buff.append(day).append("time.day").append("time.ago");
            return buff.toString();
        }
        int hour = (int) (time / HOUR);
        if (hour > 0) {
            buff.append(hour).append("time.hour").append("time.ago");
            return buff.toString();
        }
        int minute = (int) (time / MINUTE);
        if (minute > 0) {
            buff.append(minute).append("time.minute").append(
                    "time.ago");
            return buff.toString();
        }
        int second = (int) (time / SECOND);
        if (second > 0) {
            buff.append(second).append("time.second").append(
                    "time.ago");
            return buff.toString();
        }
        return buff.toString();
    }

    public static String toDateString() {
        return toDateString(System.currentTimeMillis());
    }

    public static String toHHmmss(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(time);
    }

    public static String toMMddHHmm(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        return format.format(time);
    }

    public static String toDateString(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(time);
    }

    public static int getYear() {
        return getYear(System.currentTimeMillis());
    }

    public static int getMonth() {
        return getMonth(System.currentTimeMillis());
    }

    public static int getDay() {
        return getDay(System.currentTimeMillis());
    }

    public static String getDateYYYYMMDD() {
        return getDateYYYYMMDD(System.currentTimeMillis());
    }

    public static String getDateYYYYMMDD(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(time);
    }

    public static String getDateMMDDHHmm(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        return format.format(time);
    }

    public static String getDateYYYYMM() {
        return getDateYYYYMM(System.currentTimeMillis());
    }

    public static String getDateYYYYMM(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        return format.format(time);
    }

    public static String getHHmm(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(time);
    }

    public static String getHHmmss(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(time);
    }

    public static int getYear(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String string = format.format(time);
        String[] strings = string.split("-");
        return Integer.parseInt(strings[0]);
    }

    public static int getMonth(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String string = format.format(time);
        String[] strings = string.split("-");
        return Integer.parseInt(strings[1]);
    }

    public static int getDay(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String string = format.format(time);
        String[] strings = string.split("-");
        return Integer.parseInt(strings[2]);
    }

    public static int getDateCount(int year, int month) {
        Calendar time = Calendar.getInstance();
        time.clear();
        time.set(Calendar.YEAR, year);
        time.set(Calendar.MONTH, month - 1);
        return time.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static StringBuilder getChinaShow(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        StringBuilder tmpStr0 = new StringBuilder();
        tmpStr0.append("周").append(returnDayOfWeek(dayOfWeek)).append(getHHmm(time));
        return tmpStr0;
    }

    private static String returnDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                return "日";
            case 2:
                return "一";
            case 3:
                return "二";
            case 4:
                return "三";
            case 5:
                return "四";
            case 6:
                return "五";
            case 7:
                return "六";
        }
        return "";
    }
}
