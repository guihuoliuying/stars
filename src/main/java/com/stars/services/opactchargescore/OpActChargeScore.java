package com.stars.services.opactchargescore;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

public interface OpActChargeScore extends Service, ActorService {
	@AsyncInvocation
	void view(long roleId);

	/**
	 * 活动结束
	 *
	 * @param activityId
	 */
	@AsyncInvocation
	public void closeActivity(int activityId);

}
