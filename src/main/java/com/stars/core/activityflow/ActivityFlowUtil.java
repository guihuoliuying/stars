package com.stars.core.activityflow;

import com.stars.util.StringUtil;
import com.stars.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by zhaowenshuo on 2016/10/9.
 */
public class ActivityFlowUtil {

    public static final int IDX_SECOND = 0;
    public static final int IDX_MINUTE = 1;
    public static final int IDX_HOUR = 2;
    public static final int IDX_DAY_OF_MONTH = 3;
    public static final int IDX_MONTH = 4;
    public static final int IDX_DAY_OF_WEEK = 5;
    public static final int IDX_YEAR = 6;

    public static Calendar getTodayCalendar(String cronExpr) {
        String[] cronValueArr = cronExpr.split(" ");
        int hour = ActivityFlowUtil.getNumber(cronValueArr, ActivityFlowUtil.IDX_HOUR);
        int minute = ActivityFlowUtil.getNumber(cronValueArr, ActivityFlowUtil.IDX_MINUTE);
        int second = ActivityFlowUtil.getNumber(cronValueArr, ActivityFlowUtil.IDX_SECOND);
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar;
    }

    public static int getSecond(String cronExpr) {
        return getNumberFromCronExpr(cronExpr, IDX_SECOND);
    }

    public static int getMinute(String cronExpr) {
        return getNumberFromCronExpr(cronExpr, IDX_MINUTE);
    }

    public static int getHour(String cronExpr) {
        return getNumberFromCronExpr(cronExpr, IDX_HOUR);
    }

    private static int getNumberFromCronExpr(String cronExpr, int idx) {
        return getNumber(splitCronExpr(cronExpr), idx);
    }

    /**
     * 计算剩余时间，超出表达式时间会为负数，单位:秒   (只比较时分秒)
     *
     * @param now
     * @param cronExpr
     * @return
     */
    public static long remainder(long now, String cronExpr) {
        return (getTimeInMillisByCronExpr(cronExpr) - now) / 1000;
    }

    public static long getTimeInMillisByCronExpr(String cronExpr) {
        String[] array = cronExpr.split(" ");
        int dayOfWeek = getDayOfWeek(array);
        int hour = getNumber(array, IDX_HOUR);
        int minute = getNumber(array, IDX_MINUTE);
        int second = getNumber(array, IDX_SECOND);
        if (hour == -1 || minute == -1 || second == -1) {
            throw new IllegalArgumentException();
        }
        Calendar calendar = Calendar.getInstance();
        if (dayOfWeek != -1) {
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar.getTimeInMillis();
    }

    public static long getTimeInMillisByCronExprByWeek(String cronExpr) {
        String[] array = cronExpr.split(" ");
        String tmpStr = array[IDX_DAY_OF_WEEK];
        int dayOfWeek = getDayOfWeek(tmpStr.split("\\#")[0]);
        int dayOfWeekInMonth = Integer.parseInt(tmpStr.split("\\#")[1]);
        int hour = getNumber(array, IDX_HOUR);
        int minute = getNumber(array, IDX_MINUTE);
        int second = getNumber(array, IDX_SECOND);
        int month = getNumber(array, IDX_MONTH);
        if (hour == -1 || minute == -1 || second == -1) {
            throw new IllegalArgumentException();
        }
        Calendar calendar = Calendar.getInstance();
        if (dayOfWeek != -1) {
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, dayOfWeekInMonth);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        if (month != -1) {
            calendar.set(Calendar.MONTH, month - 1);
        }
        calendar.set(Calendar.SECOND, second);
        return calendar.getTimeInMillis();
    }

    public static long getTimeInMillisByCronExprBak(String cronExpr) {
        String[] array = cronExpr.split(" ");
        int month = getMonth(array);
        int dayOfMonth = getNumber(array, IDX_DAY_OF_MONTH);
        int hour = getNumber(array, IDX_HOUR);
        int minute = getNumber(array, IDX_MINUTE);
        int second = getNumber(array, IDX_SECOND);
        if (hour == -1 || minute == -1 || second == -1) {
            throw new IllegalArgumentException();
        }
        Calendar calendar = Calendar.getInstance();
        if (month != -1) {
            calendar.set(Calendar.MONTH, month);
        }
        if (dayOfMonth != -1) {
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar.getTimeInMillis();
    }


    /**
     * 获得HH：mm：ss的字符串
     *
     * @param cronExpr
     * @return
     */
    public static String getHHmmssStr(String cronExpr) {
        if (StringUtil.isEmpty(cronExpr)) return "";
        long time = getTimeInMillisByCronExprByWeek(cronExpr);
        return TimeUtil.getHHmmss(time);
    }

    /**
     * 获得HH：mm的字符串
     *
     * @param cronExpr
     * @return
     */
    public static String getHHmmStr(String cronExpr) {
        if (StringUtil.isEmpty(cronExpr)) return "";
        long time = getTimeInMillisByCronExprByWeek(cronExpr);
        return TimeUtil.getHHmm(time);
    }

    /**
     * 获得MM-dd HH：mm的字符串
     *
     * @param cronExpr
     * @return
     */
    public static String getMMddHHmmStr(String cronExpr) {
        String[] cronValueArr = cronExpr.split(" ");
        int month = getMonth(cronValueArr) + 1;
        int dayOfMonth = getNumber(cronValueArr, IDX_DAY_OF_MONTH);
        int hour = ActivityFlowUtil.getNumber(cronValueArr, ActivityFlowUtil.IDX_HOUR);
        int minute = ActivityFlowUtil.getNumber(cronValueArr, ActivityFlowUtil.IDX_MINUTE);
        return month + "-" + dayOfMonth + " " + hour + ":" + minute;
    }

    /**
     * 提供一个很弱的判断方法(不能跨日)，只比较秒[0]，分[1]，时[2]，日（月）[3]，月[4]，日（周）[5]
     *
     * @param now
     * @param cronExpr1
     * @param cronExpr2
     * @return
     */
    public static boolean between(long now, String cronExpr1, String cronExpr2) {
        String[] array1 = cronExpr1.split(" ");
        String[] array2 = cronExpr2.split(" ");
        int year1 = getYear(array1);
        int year2 = getYear(array2);
        int dayOfWeek1 = getDayOfWeek(array1);
        int dayOfWeek2 = getDayOfWeek(array2);
        int month1 = getMonth(array1);
        int month2 = getMonth(array2);
        int dayOfMonth1 = getNumber(array1, IDX_DAY_OF_MONTH);
        int dayOfMonth2 = getNumber(array2, IDX_DAY_OF_MONTH);
        int hour1 = getNumber(array1, IDX_HOUR);
        int hour2 = getNumber(array2, IDX_HOUR);
        int minute1 = getNumber(array1, IDX_MINUTE);
        int minute2 = getNumber(array2, IDX_MINUTE);
        int second1 = getNumber(array1, IDX_SECOND);
        int second2 = getNumber(array2, IDX_SECOND);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        int y = calendar.get(Calendar.YEAR);
        int dow = calendar.get(Calendar.DAY_OF_WEEK);
        int M = calendar.get(Calendar.MONTH);
        int dom = calendar.get(Calendar.DAY_OF_MONTH);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);

        if ((year1 == -1 && year2 != -1)
                || (year1 != -1 && year2 == -1)
                || (dayOfWeek1 == -1 && dayOfWeek2 != -1)
                || (dayOfWeek1 != -1 && dayOfWeek2 == -1)
                || (month1 == -1 && month2 != -1)
                || (month1 != -1 && month2 == -1)
                || (dayOfMonth1 == -1 && dayOfMonth2 != -1)
                || (dayOfMonth1 != -1 && dayOfMonth2 == -1)
                || (hour1 == -1 && hour2 != -1)
                || (hour1 != -1 && hour2 == -1)
                || (minute1 == -1 && minute2 != -1)
                || (minute1 != -1 && minute2 == -1)
                || (second1 == -1 && second2 != -1)
                || (second1 != -1 && second2 == -1)) {
            throw new IllegalArgumentException();
        }


        if (year1 != -1 && (y != year1 || y != year2)) {
            return false;
        }
        //一周之内跨天
        if (dayOfWeek1 != -1 && (dayOfWeek1 > dow || dow > dayOfWeek2)) {
            return false;
        }

        if (month1 != -1 && (M != month1 || M != month2)) {
            return false;
        }
        if (dayOfMonth1 != -1 && (dom != dayOfMonth1 || dom != dayOfMonth2)) {
            return false;
        }
        if (hour1 != -1 && (h < hour1 || h > hour2)) {
            return false;
        }
        if (minute1 != -1
                && (((hour1 == -1 || h == hour1) && m < minute1)
                || ((hour2 == -1 || h == hour2) && m > minute2))) {
            return false;
        }
        if (second1 != -1 &&
                (((hour1 == -1 || h == hour1) && (minute1 == -1 || m == minute1) && s < second1)
                        || ((hour2 == -1 || h == hour2) && (minute2 == -1 || m == minute2) && s > second2))) {
            return false;
        }
        return true;
    }

    public static boolean outside(long now, String cronExpr1, String cronExpr2) {
        return !between(now, cronExpr1, cronExpr2);
    }

    private static int getYear(String[] array) {
        if (array.length < 7) {
            return -1;
        }
        return getNumber(array, IDX_YEAR);
    }

    private static int getDayOfWeek(String[] array) {
        String s = array[IDX_DAY_OF_WEEK];
        switch (s) {
            case "SUN":
            case "1":
                return Calendar.SUNDAY;
            case "MON":
            case "2":
                return Calendar.MONDAY;
            case "TUE":
            case "3":
                return Calendar.TUESDAY;
            case "WED":
            case "4":
                return Calendar.WEDNESDAY;
            case "THU":
            case "5":
                return Calendar.THURSDAY;
            case "FRI":
            case "6":
                return Calendar.FRIDAY;
            case "SAT":
            case "7":
                return Calendar.SATURDAY;
            default:
                return -1;
        }
    }

    private static int getDayOfWeek(String s) {
        switch (s) {
            case "SUN":
            case "1":
                return Calendar.SUNDAY;
            case "MON":
            case "2":
                return Calendar.MONDAY;
            case "TUE":
            case "3":
                return Calendar.TUESDAY;
            case "WED":
            case "4":
                return Calendar.WEDNESDAY;
            case "THU":
            case "5":
                return Calendar.THURSDAY;
            case "FRI":
            case "6":
                return Calendar.FRIDAY;
            case "SAT":
            case "7":
                return Calendar.SATURDAY;
            default:
                return -1;
        }
    }

    /**
     * 获得月份，0-11
     *
     * @param array
     * @return
     */
    private static int getMonth(String[] array) {
        String s = array[IDX_MONTH];
        switch (s) {
            case "*":
            case "?":
                return -1;
            case "JAN":
            case "1":
                return Calendar.JANUARY;
            case "FEB":
            case "2":
                return Calendar.FEBRUARY;
            case "MAR":
            case "3":
                return Calendar.MARCH;
            case "APR":
            case "4":
                return Calendar.APRIL;
            case "MAY":
            case "5":
                return Calendar.MAY;
            case "JUN":
            case "6":
                return Calendar.JUNE;
            case "JUL":
            case "7":
                return Calendar.JULY;
            case "AUG":
            case "8":
                return Calendar.AUGUST;
            case "SEP":
            case "9":
                return Calendar.SEPTEMBER;
            case "OCT":
            case "10":
                return Calendar.OCTOBER;
            case "NOV":
            case "11":
                return Calendar.NOVEMBER;
            case "DEC":
            case "12":
                return Calendar.DECEMBER;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static int getNumber(String[] array, int idx) {
        String s = array[idx];
        switch (s) {
            case "*":
            case "?":
                return -1;
            default:
                return Integer.parseInt(s);
        }
    }

    private static String[] splitCronExpr(String cronExpr) {
        return cronExpr.split(" ");
    }

    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);
    }

}
