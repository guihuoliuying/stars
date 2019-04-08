package com.stars.services.pay;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

public interface PayService extends Service, ActorService {
	/**
	 * 收到支付订单 被支付转发服rpc调用
	 * @param serverId
	 * @param orderStr
	 * @param isUseTool 是否使用伪充值道具
	 */
	@AsyncInvocation
	void recaivePayOrder(int serverId, int payServer,PayOrderInfo pOrderInfo,boolean isUseTool);
	
	/**
	 * module发完货后回调service
	 * @param orderNo
	 * @param money
	 * @param chargeId
	 */
	@AsyncInvocation
	void consignmentCallBack(String orderNo,int money,int chargeId);
}
