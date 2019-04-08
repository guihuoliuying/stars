package com.stars.modules.operateactivity.labeldisappear;

/**
 * 标签显示控制基类;
 * Created by gaopeidian on 2017/3/22.
 */
public class LabelDisappearBase  {
	public static final short NeverDisappear = 0;//标签永不消失
    public static final short DisappearByDays = 1;//活动持续时间x天后消失,格式为 1|x
    public static final short DisappearByTime = 2;//2=固定时间点消失,格式为:2|yy-mm-dd hh:mm:ss
   
    protected short disappearType;
    
    public short getDisappearType(){
    	return this.disappearType;
    }
    
    public static LabelDisappearBase newDisappearBaseByStr(String str){
    	if (str == null || str.equals("")){
    		return null;
    	}
    	short type = -1;
    	String sts[] = str.split("\\|");   	
    	
    	if (sts.length > 0) {
  			if (!sts[0].equals("")) {
  				type = Short.parseShort(sts[0]);
  			}	
  		}
    	
    	switch (type) {	
    	case NeverDisappear:{
			return new NeverDisappear();
		}
		case DisappearByDays:{
			if (sts.length >= 2) {
				return new DisappearByDays(sts[1]); 
			}else{
				return null;
			}
		}
		case DisappearByTime:{
			if (sts.length >= 2) {
				return new DisappearByTime(sts[1]); 
			}else{
				return null;
			}
		}
		default:
			return null;
		}
    }
}
