package com.stars.modules.operateactivity;

/**
 * Created by gaopeidian on 2016/12/10.
 */
public interface OpActivityModule {
	/**
	 * 获得当前活动的id，若无该类型的活动正在进行，则返回-1
	 */
	int getCurShowActivityId();
	
	/**
	 * 是否显示活动页签，返回0则不显示，1则显示
	 */
	byte getIsShowLabel();
}
