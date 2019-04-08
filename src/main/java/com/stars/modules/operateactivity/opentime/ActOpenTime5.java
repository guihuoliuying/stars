package com.stars.modules.operateactivity.opentime;

import com.stars.util.DateUtil;

import java.util.Date;
/**
 * Created by gaopeidian on 2016/12/6.
 * 时间控制类：固定时间开放,无开服时间限制
 */

public class ActOpenTime5 extends ActOpenTimeBase{
   private String startDateString = "";
   private String endDateString = "";
   private Date startDate = null;
   private Date endDate = null;
	
   ActOpenTime5(String value){
	   this.openTimeType = ActOpenTimeBase.OpenTimeType5;
	   
	   if (value == null || value.equals("") || value.equals("0")) {
		   return;
	   }
	   String sts[] = value.split("\\&");
	   
	   //检查
	   if (sts.length >= 2) {
		   this.startDateString = sts[0];
		   this.endDateString = sts[1];			   
		   this.startDate = DateUtil.toDate(startDateString);
		   this.endDate = DateUtil.toDate(endDateString);
	   }else{

	   }
   }
   
   public String getStartDateString(){
	   return this.startDateString;
   }
   
   public String getEndDateString(){
	   return this.endDateString;
   }
   
   public Date getStartDate(){
	   return this.startDate;
   }
   
   public Date getEndDate(){
	   return this.endDate;
   }
}
