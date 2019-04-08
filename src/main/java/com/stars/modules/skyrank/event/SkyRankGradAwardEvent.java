package com.stars.modules.skyrank.event;

import com.stars.core.event.Event;
import com.stars.modules.skyrank.prodata.SkyRankGradVo;
/**
 * 
 * 段位提升奖励事件
 * @author xieyuejun
 *
 */
public class SkyRankGradAwardEvent extends Event {
	private SkyRankGradVo newRGV;
	
	public SkyRankGradAwardEvent(SkyRankGradVo newRGV){
		this.newRGV = newRGV;
	}

	public SkyRankGradVo getNewRGV() {
		return newRGV;
	}

	public void setNewRGV(SkyRankGradVo newRGV) {
		this.newRGV = newRGV;
	}
	
	
}
