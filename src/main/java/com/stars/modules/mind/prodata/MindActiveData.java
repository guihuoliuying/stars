package com.stars.modules.mind.prodata;

/**
 * 答题游戏参数;
 * Created by gaopeidian on 2016/9/22.
 */
public class MindActiveData{
	public int typeId;
    public int param;
    //public String desc;
	
//	public MindActiveData(int typeId , int param , String desc){
//		this.typeId = typeId;
//		this.param = param;
//		this.desc = desc;
//	}
    
    public MindActiveData(int typeId , int param){
		this.typeId = typeId;
		this.param = param;
	}
}
