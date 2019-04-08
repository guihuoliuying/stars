package com.stars.modules.operateactivity.labeldisappear;

import com.stars.util.DateUtil;

import java.util.Date;
/**
 * Created by gaopeidian on 2017/3/22.
 */

public class DisappearByTime extends LabelDisappearBase{
   private String dateString = "";
   private Date date = null;
	
   DisappearByTime(String value){
	   this.disappearType = DisappearByTime;
	   
	   if (value == null || value.equals("") || value.equals("0")) {
		   return;
	   }
	   
	   this.dateString = value;
	   this.date = DateUtil.toDate(this.dateString);
   }
   
   public String getDateString(){
	   return this.dateString;
   }
   
   public Date getDate(){
	   return this.date;
   }
}
