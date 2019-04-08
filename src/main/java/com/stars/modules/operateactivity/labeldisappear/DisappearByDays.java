package com.stars.modules.operateactivity.labeldisappear;

/**
 * Created by gaopeidian on 2017/3/22.
 */
public class DisappearByDays extends LabelDisappearBase{
   private int days = 0;//开服几天后消失
	
   DisappearByDays(String value){
	   this.disappearType = LabelDisappearBase.DisappearByDays;
	   
	   if (value == null || value.equals("") || value.equals("0")) {
		   return;
	   }
	   
	   days = Integer.parseInt(value);
   }
   
   public int getDays(){
	   return this.days;
   }
}
