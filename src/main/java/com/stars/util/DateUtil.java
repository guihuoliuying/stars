package com.stars.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jx on 2015/4/16.
 */
public class DateUtil {

	public static String YMDHMS_ = "yyyy-MM-dd HH:mm:ss";
	public static String YMDHMS = "yyyyMMddHHmmss";
	public static String YMD_ = "yyyy-MM-dd";
	public static String YMD = "yyyyMMdd";
	public static String HM = "HH:mm";
	public static String MS = "mm:ss";
	public static String ms = "mmss";
	public static String CH = "yyyy年MM月dd日";
	public static String YMDHM_ = "yyyy-MM-dd HH:mm";
	public static String MDHM = "MM-dd HH:mm";

	/* 常用时间戳定义 */
	public static long SECOND = 1000;// 秒
	public static long MINUTE = 60 * SECOND;// 分
	public static long HOUR = 60 * MINUTE;// 时
	public static long DAY = 24 * HOUR;// 天

	public static byte MINUTE_SECOND = 60;// 1分钟=60m

	public static long HALF_HOUR = 30 * MINUTE;// 半小时

	public static String getYMDHMS_Str() {
		return new SimpleDateFormat(YMDHMS_).format(new Date()); // format()方法不是线程安全的，需要new一个新对象或者加同步锁，否则返回的日期可能有误
	}

	public static String getYMDHMSStr() {
		return new SimpleDateFormat(YMDHMS).format(new Date());
	}

	public static String getYMD_Str() {
		return new SimpleDateFormat(YMD).format(new Date());
	}

	public static int getYMDInt() {
		return Integer.valueOf(getYMD_Str());
	}

	public static int getHMSInt() {
		return Integer.valueOf(new SimpleDateFormat("HHmmss").format(new Date()));
	}

	public static String getYMDStr() {
		return new SimpleDateFormat(YMD_).format(new Date());
	}

	public static String getCHStr() {
		return new SimpleDateFormat(CH).format(new Date());
	}

	/**
	 * 格式化时间
	 *
	 * @param date
	 *            时间
	 * @param sfmt
	 *            格式化类型
	 * @return
	 */
	public static String formatDate(Date date, String sfmt) {
		return new SimpleDateFormat(sfmt).format(date);
	}

	public static String formatDate(GregorianCalendar now, String sfmt) {
		return new SimpleDateFormat(sfmt).format(now.getTime());
	}

	/**
	 * 将字符串转换为时间
	 *
	 * @param sDate
	 *            字符串类型的时间
	 * @param sFmt
	 *            字符串本身的格式
	 * @return
	 */
	public static Date toDate(String sDate, String sFmt) {
		Date dt = null;
		try {
			dt = new SimpleDateFormat(sFmt).parse(sDate);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			return dt;
		}
		return dt;
	}

	/**
	 * 将字符串转换为时间
	 *
	 * @param sDate
	 *            字符串类型的时间,格式必须为yyyy-MM-dd,yyyyMMdd, yyyy-MM-dd
	 *            HH:mm:ss,yyyyMMddHHmmss
	 * @return
	 */
	public static Date toDate(String sDate) {
		Date dt = null;
		if (sDate != null) {
			SimpleDateFormat fmt;
			if (sDate.length() >= 14) {
				if (sDate.indexOf("-") > 0)
					fmt = new SimpleDateFormat(YMDHMS_);
				else
					fmt = new SimpleDateFormat(YMDHMS);
			} else {
				if (sDate.indexOf("-") > 0)
					fmt = new SimpleDateFormat(YMD_);
				else
					fmt = new SimpleDateFormat(YMD);
			}
			try {
				dt = fmt.parse(sDate);
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
				return dt;
			}
		}
		return dt;
	}

	public static int getSecondTime() {
		return (int) (System.currentTimeMillis() / 1000);
	}

	/**
	 * 比较两个时间时间相差的天数，传入的值为date类型
	 *
	 * @param dateFrom
	 *            开始时间
	 * @param dateEnd
	 *            结束时间
	 * @return
	 */
	public static int getDaysBetweenTwoDates(Date dateFrom, Date dateEnd) {
		if ((dateFrom == null) || (dateEnd == null))
			return -1;
		long begin = dateFrom.getTime();
		long end = dateEnd.getTime();
		long inter = end - begin;

		long dateMillSec = 86400000L;

		long dateCnt = inter / dateMillSec;

		return (int) dateCnt;
	}
	
	/**
	 * 比较两个时间时间相差的天数，传入的值为date类型
	 * 注：这里比较的是两个日期相差的天数，不是绝对时间天数
	 * 比如传入2016-01-01 23:00:00和2016-01-02 01:00:00的date返回是1，而不是0
	 * 另：若传入时间有误，返回-1(dateEnd必须晚于dateFrom)
	 * @param dateFrom
	 *            开始时间
	 * @param dateEnd
	 *            结束时间
	 * @return
	 */
	public static int getRelativeDifferDays(Date dateFrom, Date dateEnd) {
		Calendar calendar = Calendar.getInstance();
		if ((dateFrom == null) || (dateEnd == null))
			return -1;
		
		calendar.setTime(dateFrom);
		int year1 = calendar.get(Calendar.YEAR);
		int day1 = calendar.get(Calendar.DAY_OF_YEAR);
		
		calendar.setTime(dateEnd);
		int year2 = calendar.get(Calendar.YEAR);
		int day2 = calendar.get(Calendar.DAY_OF_YEAR);
		
		if (year1 != year2) {//不同年
			if (year1 > year2) return -1;//若开始时间年数大于结束时间年数，则返回-1
			
			int timeDistance = 0;
			for(int i = year1; i < year2; i++){
				if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0){//闰年
					timeDistance += 366;
				}else{//不是闰年
					timeDistance += 365;
				}
			}
			
			return timeDistance + (day2 - day1);
		}else{//同年
			return day2 - day1;
		}		
	}

	/**
	 * 比较两个时间时间相差的小时数，传入的值为date类型
	 *
	 * @param dateFrom
	 *            开始时间
	 * @param dateEnd
	 *            结束时间
	 * @return
	 */
	public static int getHoursBetweenTwoDates(Date dateFrom, Date dateEnd) {
		if ((dateFrom == null) || (dateEnd == null))
			return -1;
		long begin = dateFrom.getTime();
		long end = dateEnd.getTime();
		long diff = end - begin;
		long hours = diff / (1000 * 60 * 60);

		return (int) hours;

	}

	/**
	 * 比较两个时间时间相差的秒数，传入的值为date类型
	 *
	 * @param dateFrom
	 *            开始时间
	 * @param dateEnd
	 *            结束时间
	 * @return
	 */
	public static int getSecondsBetweenTwoDates(Date dateFrom, Date dateEnd) {
		if ((dateFrom == null) || (dateEnd == null))
			return -1;
		long begin = dateFrom.getTime();
		long end = dateEnd.getTime();
		long diff = end - begin;
		long seconds = diff / 1000;

		return (int) seconds;

	}

	/**
	 * 比较两个时间时间相差的天数，传入的值为String类型
	 *
	 * @param dateFrom
	 *            开始时间
	 * @param dateEnd
	 *            结束时间
	 * @return
	 */
	public static int getDaysBetweenTwoDates(String dateFrom, String dateEnd) {
		Date dateF = toDate(dateFrom, YMD_);
		Date dateE = toDate(dateEnd, YMD_);
		return getDaysBetweenTwoDates(dateF, dateE);
	}

	public static String getHM(long time) {
		return new SimpleDateFormat(HM).format(time);
	}

	/**
	 * 获得mm:ss 的字符串
	 *
	 * @param time
	 * @return
	 */
	public static String getMS(long time) {
		return new SimpleDateFormat(MS).format(time);
	}

	/**
	 * 获得mmss字符串
	 * 
	 */
	public static String getms(long time){
		return new SimpleDateFormat(ms).format(time);
	}
	
	/**
	 * 获得yyy-MM-dd HH:mm:ss 的字符串
	 *
	 * @param time
	 * @return
	 */
	public static String formatDateTime(long time) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
	}

	/**
	 * 获得HH:mm:ss 的字符串
	 *
	 * @param time
	 * @return
	 */
	public static String formatDateTimeHMS(long time) {
		return new SimpleDateFormat("HH:mm:ss").format(time);
	}

	/**
	 * 获取 yyyy-MM-dd HH:mm 字符串
	 *
	 * @param time
	 * @return
	 */
	public static String getYMDHM_(long time) {
		return new SimpleDateFormat(YMDHM_).format(time);
	}

	/**
	 * 获得指定时间格式的long值
	 *
	 * @param time
	 * @return
	 */
	public static long parseDateTime(String time) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time).getTime();
		} catch (ParseException e) {
			LogUtil.error("parseDateTime " + e.getMessage(), e);
			return System.currentTimeMillis();
		}
	}

	/**
	 * 获得Calendar
	 *
	 * @param time
	 * @return
	 */
	public static Calendar getCalendar(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return cal;
	}

	/**
	 * 获得剩余时间
	 *
	 * @param dateFrom
	 * @param dateEnd
	 * @return
	 */
	public static String getRemainBetweenTwoDates(Date dateFrom, Date dateEnd) {
		if (dateFrom == null || dateEnd == null) {
			return "";
		}
		long begin = dateFrom.getTime();
		long end = dateEnd.getTime();
		long inter = end - begin;

		int day = (int) (inter / DateUtil.DAY);
		int hour = (int) (inter % DateUtil.DAY / DateUtil.HOUR);
		int minute = (int) (inter % DateUtil.DAY % DateUtil.HOUR / DateUtil.MINUTE);
		int second = (int) (inter % DateUtil.DAY % DateUtil.HOUR % DateUtil.MINUTE / DateUtil.SECOND);
		return "date.countdown1";
	}

	/**
	 * 获得星期
	 *
	 * @return
	 */
	public static int getWeekDay() {
		Calendar cal = new GregorianCalendar();
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 获得中国习惯用的星期。即星期一为第一天，星期日为第七天
	 * 
	 * @return
	 */
	public static int getChinaWeekDay() {
		int standardDay = getWeekDay();
		standardDay--;
		if (standardDay == 0) {
			standardDay = 7;
		}
		return standardDay;
	}

	/**
	 * 获得中国习惯用的星期。即星期一为第一天，星期日为第七天
	 * 
	 * @param date
	 * @return
	 */
	public static int getChinaWeekDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int standardDay = cal.get(Calendar.DAY_OF_WEEK);
		standardDay--;
		if (standardDay == 0) {
			standardDay = 7;
		}
		return standardDay;
	}

	/**
	 * 获得当前在一年中的多少天
	 *
	 * @return
	 */
	public static int getDayOfYear() {
		Calendar cal = new GregorianCalendar();
		return cal.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * 获得输入时间是一年当中第几周
	 * 
	 * @param date
	 * @return
	 */
	public static int getWeekOfYear(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(date);
		return cal.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * 把习惯上用1表示星期一，用2表示星期二，... 转换为程序用的星期几
	 *
	 * @param day
	 * @return
	 */
	public static int toDay(int day) {
		if (day > 0) {
			if (day == 7) {
				return Calendar.SUNDAY;
			} else if (day < 7) {
				return day + 1;
			}
		}
		return day;
	}

	/**
	 * 将日期装换成长整形时间
	 *
	 * @param dateStr
	 * @return
	 */
	public static long convertDateToTimeInMillis(String dateStr) {
		if (StringUtil.isEmpty(dateStr))
			return 0;
		Calendar cal = new GregorianCalendar();
		Date date = toDate(dateStr.trim());
		if (date == null)
			return 0;
		cal.setTime(date);
		return cal.getTimeInMillis();
	}

	/**
	 * HH:mm:ss转换为当前日期的年月日+HH:mm:ss
	 *
	 * @return
	 */
	public static Date hourStrTimeToDateTime(String hourStrTime) {

		if (hourStrTime == null || hourStrTime.equals("")) {
			return null;
		}
		int year = getCalendar(new Date().getTime()).get(Calendar.YEAR);
		int month = getCalendar(new Date().getTime()).get(Calendar.MONTH);
		int date = getCalendar(new Date().getTime()).get(Calendar.DATE);

		Calendar c = getCalendar(toDate(hourStrTime, "HH:mm:ss").getTime());
		c.set(year, month, date);
		return c.getTime();
	}

	/**
	 * 获得当前日期
	 *
	 * @return
	 */
	public static String getDateStr() {
		Calendar cal = new GregorianCalendar();
		return toDateStr(cal);
	}

	public static String toDateStr(Calendar cal) {
		StringBuffer dateBuf = new StringBuffer();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int month = cal.get(Calendar.MONTH) + 1;
		dateBuf.append(cal.get(Calendar.YEAR)).append("-");
		if (month < 10) {
			dateBuf.append("0");
		}
		dateBuf.append(month).append("-");
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if (day < 10) {
			dateBuf.append("0");
		}
		dateBuf.append(day).append(" ");
		if (hour < 10) {
			dateBuf.append("0");
		}
		dateBuf.append(hour).append(":");
		if (min < 10) {
			dateBuf.append("0");
		}
		dateBuf.append(min).append(":");
		int second = cal.get(Calendar.SECOND);
		if (second < 10) {
			dateBuf.append("0");
		}
		dateBuf.append(second);
		return dateBuf.toString();
	}

	public static String toSimpleDateStr(Calendar cal){
		StringBuffer dateBuf = new StringBuffer();
		int month = cal.get(Calendar.MONTH) + 1;
		dateBuf.append(cal.get(Calendar.YEAR)).append("-");
		if (month < 10) {
			dateBuf.append("0");
		}
		dateBuf.append(month).append("-");
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if (day < 10) {
			dateBuf.append("0");
		}
		dateBuf.append(day).append(" ");
		return dateBuf.toString();
	}

	public static String toDateStrNotSecond(Calendar cal) {
		StringBuffer dateBuf = new StringBuffer();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int month = cal.get(Calendar.MONTH) + 1;
		dateBuf.append(cal.get(Calendar.YEAR)).append("-");
		if (month < 10) {
			dateBuf.append("0");
		}
		dateBuf.append(month).append("-");
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if (day < 10) {
			dateBuf.append("0");
		}
		dateBuf.append(day).append(" ");
		if (hour < 10) {
			dateBuf.append("0");
		}
		dateBuf.append(hour).append(":");
		if (min < 10) {
			dateBuf.append("0");
		}
		dateBuf.append(min);
		return dateBuf.toString();
	}

	/**
	 * 获取0点的时间
	 *
	 * @param cal
	 * @return
	 */
	public static long getZeroTime(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTimeInMillis();
	}
	
	/**
	 * 获取0点的时间
	 *
	 * @param cal
	 * @return
	 */
	public static Date getZeroTimeDate(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	/*
	 * 判断一个日期是否落在一个区间内
	 */
	public static boolean isBetweenTwoDays(Date targetDate, Date dateFrom, Date dateEnd) {

		return getHoursBetweenTwoDates(dateFrom, targetDate) >= 0
				&& getHoursBetweenTwoDates(targetDate, dateEnd) >= 0;

	}

	/**
	 * 转换时间
	 * 
	 * @param time
	 * @return
	 */
	public static Date toDate(long time) {
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);
		return cal.getTime();
	}
	
	public static boolean isBetween(Date targetDate , Date startDate , Date endDate){
		if (targetDate == null || startDate == null || endDate == null) {
			return false;
		}
		
		return ((startDate.getTime() <= targetDate.getTime()) && (endDate.getTime() >= targetDate.getTime()));
	}
	
	/**
	 * 获取当前时间int
	 * @return
	 */
	public static int getCurrentTimeInt(){
		Calendar calendar = Calendar.getInstance();
		return (int)(calendar.getTimeInMillis()/1000);
	}
	
	/**
	 * 获取当前时间long
	 * @return
	 */
	public static long getCurrentTimeLong(){
		Calendar calendar = Calendar.getInstance();
		return calendar.getTimeInMillis();
	}
	
	/**
	 * 获取当天0点N天前或者N后的时间      后：n>0  ；前 n<0
	 */
	public static Date getDateAfterN(Date date, int n){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.DATE, n);
		return calendar.getTime();
	}
	
	/**
	 * 获取当天0点N天前或者N后的时间      后：n>0  ；前 n<0
	 */
	public static int getDateAfterN(int n){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.DATE, n);
		return (int)(calendar.getTimeInMillis()/1000);
	}
	
}
