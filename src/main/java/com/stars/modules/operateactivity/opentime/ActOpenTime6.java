package com.stars.modules.operateactivity.opentime;

/**
 * Created by gaopeidian on 2017/2/10.
 * 时间控制类：开服x天内不开此活动,之后开启
 */
public class ActOpenTime6 extends ActOpenTimeBase{
   private int days = -1;//开服的第几天内
	
   ActOpenTime6(String value){
	   this.openTimeType = ActOpenTimeBase.OpenTimeType6;
	   
	   if (value == null || value.equals("") || value.equals("0")) {
		   return;
	   }
	   String sts[] = value.split("\\+");
	   
	   //检查
	   if (sts.length >= 1) {
		   this.days = Integer.parseInt(sts[0]);
	   }else{

	   }
   }
   
   public int getDays(){
	   return this.days;
   }
}
