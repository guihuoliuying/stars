package com.stars.multiserver.payServer;

import com.stars.services.pay.PayService;

public class RMPayServerHelper {
	static PayService payService;
	public static PayService payService(){
		return payService;
	}
}
