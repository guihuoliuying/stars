package com.stars.services.advertInf;

import com.stars.modules.demologin.userdata.LoginInfo;
import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

public interface AdvertInfService extends Service, ActorService{
	
	@AsyncInvocation
	public void noticeAdvertInf(long roleId, LoginInfo loginInfo);
	
	@AsyncInvocation
	public void addToSendMap(long roleId, LoginInfo loginInfo);

}
