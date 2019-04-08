package com.stars.modules.operateactivity.opentime;

import com.stars.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by gaopeidian on 2017/2/10.
 * 时间控制类：创角x~y天内开启
 */
public class ActOpenTime4 extends ActOpenTimeBase{
	private int startDays = -1;//开始的创角天数
	private int endDays = -1;//结束的创角天数

	ActOpenTime4(String value){
		this.openTimeType = ActOpenTimeBase.OpenTimeType4;

		if (value == null || value.equals("") || value.equals("0")) {
			return;
		}
		String sts[] = value.split("\\+");

		//检查
		if (sts.length >= 2) {
			this.startDays = Integer.parseInt(sts[0]);
			this.endDays = Integer.parseInt(sts[1]);
		}else{

		}
	}

	/**
	 * 是否在有效时间内
	 * @param openTime
	 * @param openDays
	 * @return
	 */
	public boolean isEffectiveTime(ActOpenTime4 openTime , int createDays) {
		return createDays >= openTime.getStartDays() && createDays <= openTime.getEndDays();
	}

	/**
	 * 获取活动开启的第几天
	 * @param openTime
	 * @param openDays
	 * @return
	 */
	public static int getOpenDays(ActOpenTime4 openTime , int createDays){
		int startDays = openTime.getStartDays();
		return createDays - startDays + 1;
	}

	public static Date getStartDate(ActOpenTime4 openTime, Date createDate) {
		int startDays = openTime.getStartDays();
		long startTime = createDate.getTime() + (startDays - 1) * DateUtil.DAY;

		Date startDate = new Date(startTime);
		startDate.setHours(0);
		startDate.setMinutes(0);
		startDate.setSeconds(0);

		return startDate;
	}

	public static Date getEndDate(ActOpenTime4 openTime, Date createDate) {
		int endDays = openTime.getEndDays();
		long endTime = createDate.getTime() + (endDays - 1) * DateUtil.DAY;
		Date endDate = new Date(endTime);
		endDate.setHours(23);
		endDate.setMinutes(59);
		endDate.setSeconds(59);

		return endDate;
	}

	public static long getStartTimes(ActOpenTime4 openTime, long createTimes){
		int startDays = openTime.getStartDays();
		long startTime = createTimes + (startDays - 1) * DateUtil.DAY;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(startTime);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND,0);
		return calendar.getTimeInMillis();
	}

	public static Calendar getStartCalendar(ActOpenTime4 openTime, long createTimes){
		int startDays = openTime.getStartDays();
		long startTime = createTimes + (startDays - 1) * DateUtil.DAY;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(startTime);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND,0);
		return calendar;
	}

	public static long getEndTimes(ActOpenTime4 openTime, long createTimes){
		int endDays = openTime.getEndDays();
		long endTime = createTimes + (endDays - 1) * DateUtil.DAY;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(endTime);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND,59);
		return calendar.getTimeInMillis();
	}

	public int getStartDays(){
		return this.startDays;
	}

	public int getEndDays(){
		return this.endDays;
	}
}
