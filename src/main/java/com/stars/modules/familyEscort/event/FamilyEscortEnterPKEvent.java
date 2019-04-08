package com.stars.modules.familyEscort.event;

import com.stars.core.event.Event;

/**
 * 进入战斗场景事件处理
 * @author xieyuejun
 *
 */
public class FamilyEscortEnterPKEvent extends Event {
	private long famliyId;
	
	public FamilyEscortEnterPKEvent(long famliyId){
		this.famliyId = famliyId;
	}

	public long getFamliyId() {
		return famliyId;
	}

	public void setFamliyId(long famliyId) {
		this.famliyId = famliyId;
	}
}
