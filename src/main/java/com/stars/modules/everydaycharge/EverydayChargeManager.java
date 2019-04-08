package com.stars.modules.everydaycharge;

import com.stars.modules.everydaycharge.prodata.EverydayChargeVo;

import java.util.Map;

public class EverydayChargeManager {

	/** 每日首充数据Map */
	public static Map<Integer, Map<Integer, EverydayChargeVo>> everydayChargeVoMap;
	
	/** 奖励模板Id */
	public static int REWARD_MAIL_TEMPLATE_ID = 25007;
	
	/* 抽奖资格状态 */
	public static final byte QUALIFY_STATE_NOT_ACTIVE = 0;//资格没激活
	public static final byte QUALIFY_STATE_ACTIVE = 1;//资格已激活
}
