package com.stars.modules.operateactivity.opentime;

/**
 * Created by gaopeidian on 2017/2/10.
 * 时间控制类：开服x~y天内开启
 */
public class ActOpenTime3 extends ActOpenTimeBase{
   private int startDays = -1;//开始的开服天数
   private int endDays = -1;//结束的开服天数
	
   ActOpenTime3(String value){
	   this.openTimeType = ActOpenTimeBase.OpenTimeType3;
	   
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
   
   public int getStartDays(){
	   return this.startDays;
   }
   
   public int getEndDays(){
	   return this.endDays;
   }
}
