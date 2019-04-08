package com.stars.modules.operateactivity.rolelimit;

/**
 * Created by gaopeidian on 2016/12/6.
 */
public class ActLevelLimit extends ActRoleLimitBase{
   private int level = -1;
	
   ActLevelLimit(String value){
	   this.type = LevelLimitType;
	   
	   if (value == null || value.equals("") || value.equals("0")) {
		   return;
	   }
	   String sts[] = value.split("\\+");
	   
	   //类型检查
	   if (sts.length > 0 && !sts[0].equals("") && Integer.parseInt(sts[0]) == LevelLimitType) {
		   if (sts.length >= 2) {
				if (!sts[1].equals("")) {
					this.level = Integer.parseInt(sts[1]);
				}
			}	
	   }else{

	   }
   }
   
   public int getLevel(){
	   return this.level;
   }
}
