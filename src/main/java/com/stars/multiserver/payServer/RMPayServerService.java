package com.stars.multiserver.payServer;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

public interface RMPayServerService extends Service, ActorService {
	/**
	 * 
	 *主服到支付转发服注册 
	 * @param rmServerId
	 * @param mainServer
	 */
	@AsyncInvocation
	void registerMainServer(int rmServerId,int mainServer);
	
	/**
	 * 主服处理完订单后通知支付转发服
	 * @param rmServerId
	 * @param orderStr
	 */
	@AsyncInvocation
	void doPayOrderCallBack(int rmServerId,String orderNo,int status);
}
