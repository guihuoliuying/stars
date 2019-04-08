package com.stars.modules.operateactivity.rolelimit;


/**
 * Created by gaopeidian on 2016/12/6.
 */
public class ActSystemLimit extends ActRoleLimitBase{
   private String systemName = "";
	
   ActSystemLimit(String value){
	   this.type = ActRoleLimitBase.SystemLimitType;
	   
	   if (value == null || value.equals("") || value.equals("0")) {
		   return;
	   }
	   String sts[] = value.split("\\+");
	   
	   //类型检查
	   if (sts.length > 0 && !sts[0].equals("") && Integer.parseInt(sts[0]) == ActRoleLimitBase.SystemLimitType) {
		   if (sts.length >= 2) {
				if (!sts[1].equals("")) {
					this.systemName = sts[1];
				}
			}	
	   }else{

	   }
   }
   
   public String getSystemName(){
	   return this.systemName;
   }
}
