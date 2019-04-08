package com.stars.modules.operateactivity.rolelimit;

/**
 * Created by gaopeidian on 2016/12/6.
 */
public class ActOpenServerDayLimit extends ActRoleLimitBase{
   private int startDays = -1;
   private int endDays = -1;
	
   ActOpenServerDayLimit(String value){
	   this.type = OpenServerDayLimitType;
	   
	   if (value == null || value.equals("") || value.equals("0")) {
		   return;
	   }
	   String sts[] = value.split("\\+");
	   
	   //类型检查
	   if (sts.length > 0 && !sts[0].equals("") && Integer.parseInt(sts[0]) == OpenServerDayLimitType) {
		   if (sts.length >= 3) {
				if (!sts[1].equals("")) {
					this.startDays = Integer.parseInt(sts[1]);
				}
				if (!sts[2].equals("")) {
					this.endDays = Integer.parseInt(sts[2]);
				}
			}	
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
