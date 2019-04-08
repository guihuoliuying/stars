package com.stars.modules.family.event;

import com.stars.core.event.Event;

public class FamilyLeaveEvent extends Event {
	
	private long familyId;
	
	public FamilyLeaveEvent(long familyId) {
		this.familyId = familyId;
	}

	public long getFamilyId() {
		return familyId;
	}

	public void setFamilyId(long familyId) {
		this.familyId = familyId;
	}

}
