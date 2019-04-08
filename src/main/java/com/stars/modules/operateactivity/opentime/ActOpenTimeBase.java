package com.stars.modules.operateactivity.opentime;

import com.stars.util.DateUtil;

import java.util.Date;

/**
 * 活动开放时间基类;
 * Created by gaopeidian on 2016/12/26.
 */
public class ActOpenTimeBase  {
    public static final byte OpenTimeType0 = 0;//永久开放
    public static final byte OpenTimeType1 = 1;//固定时间开放,有开服时间限制
    public static final byte OpenTimeType2 = 2;//固定时间+开服前几天内开放
    public static final byte OpenTimeType3 = 3;//开服x~y天内开启
    public static final byte OpenTimeType4 = 4;//创角x~y天内开启
    public static final byte OpenTimeType5 = 5;//固定时间开放,无开服时间限制
    public static final byte OpenTimeType6 = 6;//开服x天内不开此活动,之后开启
    
    protected int openTimeType;
    
    public int getOpenTimeType(){
    	return this.openTimeType;
    }
    
    public static ActOpenTimeBase newActOpenTimeBaseByStr(String str){
    	if (str == null || str.equals("")){
    		return null;
    	}
    	byte openType = -1;
    	String sts[] = str.split("\\|");   	
    	
    	if (sts.length > 0) {
  			if (!sts[0].equals("")) {
  				openType = Byte.parseByte(sts[0]);
  			}	
  		}
    	
    	switch (openType) {
		case OpenTimeType0:{
			return new ActOpenTime0(); 
		}		
		case OpenTimeType1:{
			if (sts.length >= 2) {
				return new ActOpenTime1(sts[1]); 
			}else{
				return null;
			}
		}
		case OpenTimeType2:{
			if (sts.length >= 2) {
				return new ActOpenTime2(sts[1]); 
			}else{
				return null;
			}
		}
		case OpenTimeType3:{
			if (sts.length >= 2) {
				return new ActOpenTime3(sts[1]); 
			}else{
				return null;
			}
		}
		case OpenTimeType4:{
			if (sts.length >= 2) {
				return new ActOpenTime4(sts[1]); 
			}else{
				return null;
			}
		}
		case OpenTimeType5:{
			if (sts.length >= 2) {
				return new ActOpenTime5(sts[1]); 
			}else{
				return null;
			}
		}
		case OpenTimeType6:{
			if (sts.length >= 2) {
				return new ActOpenTime6(sts[1]); 
			}else{
				return null;
			}
		}
		default:
			return null;
		}
    }
    
    public static int getOpenDaysByOpenTime3(ActOpenTime3 openTime , int openServerDays){
		int startDays = openTime.getStartDays();
		return openServerDays - startDays + 1;
	}
    
    public static Date getStartDateByOpenTime3(ActOpenTime3 openTime , Date openServerDate){
    	int startDays = openTime.getStartDays();
    	long startTime = openServerDate.getTime() + (startDays - 1) * DateUtil.DAY;
    	Date startDate = new Date(startTime);
    	startDate.setHours(0);
    	startDate.setMinutes(0);
    	startDate.setSeconds(0);
    	
    	return startDate;
    }
    
    public static Date getEndDateByOpenTime3(ActOpenTime3 openTime , Date openServerDate){
    	int endDays = openTime.getEndDays();
    	long endTime = openServerDate.getTime() + (endDays - 1) * DateUtil.DAY;
    	Date endDate = new Date(endTime);
    	endDate.setHours(23);
    	endDate.setMinutes(59);
    	endDate.setSeconds(59);
    	
    	return endDate;
    }
    
    
    @SuppressWarnings("unused")
	public static void main(String args[]){
    	int endDays = 365;
    	long dayMilliSeconds = 1000 * 60 * 60 * 24;
    	long endTime = new Date().getTime() + (endDays - 1) * dayMilliSeconds;
    	Date endDate = new Date(endTime);
    	endDate.setHours(23);
    	endDate.setMinutes(59);
    	endDate.setSeconds(59);
    	
    	int test = 0;
    }
}
