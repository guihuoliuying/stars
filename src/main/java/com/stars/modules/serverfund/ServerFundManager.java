package com.stars.modules.serverfund;

import com.stars.modules.serverfund.prodata.ServerFundVo;

import java.util.Map;

public class ServerFundManager {
	
	public static final byte FUND_STATE_OF_NOTBUY = 1;	//未购买
	public static final byte FUND_STATE_OF_CANTGET = 2;	//购买不可领
	public static final byte FUND_STATE_OF_CANGET = 3;	//购买可领
	public static final byte FUND_STATE_OF_HAVEGET = 4;	//已领取
	
	/** 购买基金最低vip等级 */
	public static int minVipLevel = 3;
	
	/** 购买基金的货币id */
	public static int moneyId = 1;
	/** 购买基金的货币数量 */
	public static int moneyCount = 1;
	
	/** 基金产品数据Map */
	public static Map<Integer, Map<Integer, ServerFundVo>> ServerFundVoMap;
	
}
