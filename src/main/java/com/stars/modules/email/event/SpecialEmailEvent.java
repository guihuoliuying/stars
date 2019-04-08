package com.stars.modules.email.event;

import com.stars.core.event.Event;

public class SpecialEmailEvent extends Event {
	
	private int emailType;
	
	private int emailId;

	public int getEmailId() {
		return emailId;
	}

	public void setEmailId(int emailId) {
		this.emailId = emailId;
	}

	public int getEmailType() {
		return emailType;
	}

	public void setEmailType(int emailType) {
		this.emailType = emailType;
	}

}
