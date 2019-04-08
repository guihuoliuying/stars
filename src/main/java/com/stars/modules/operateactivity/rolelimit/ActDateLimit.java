package com.stars.modules.operateactivity.rolelimit;

/**
 * Created by gaopeidian on 2016/12/6.
 */
public class ActDateLimit extends ActRoleLimitBase{
   private String startDateString = "";
   private String endDateString = "";
	
   ActDateLimit(String value){
	   this.type = DateLimitType;
	   
	   if (value == null || value.equals("") || value.equals("0")) {
		   return;
	   }
	   String sts[] = value.split("\\+");
	   
	   //类型检查
	   if (sts.length > 0 && !sts[0].equals("") && Integer.parseInt(sts[0]) == DateLimitType) {
		   if (sts.length >= 3) {
				if (!sts[1].equals("")) {
					this.startDateString = sts[1];
				}
				if (!sts[2].equals("")) {
					this.endDateString = sts[2];
				}
			}	
	   }else{

	   }
   }
   
   public String getStartDateString(){
	   return this.startDateString;
   }
   
   public String getEndDateString(){
	   return this.endDateString;
   }
}
