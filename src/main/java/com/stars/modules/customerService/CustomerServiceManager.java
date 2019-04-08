package com.stars.modules.customerService;

import java.util.HashMap;
import java.util.Map;

public class CustomerServiceManager {
	
	public static int COUNT_DAYS = 30;
	
	public static int COOL_DAYS = 7;
	
	public static int CHARGE_MONEY_TARGET = 198;
	
	public static byte SEND_STATE = 1;//已经发过邮件
	
	public static int AWARD_MAIL_ID = 26013;
	
	public static int AWARD_DROP_ID = 0;
	
	public static Map<String, Integer> channelMailMap;
	
	//渠道
	public static String CHANNEL_ZYY = "zyy";//自运营
	public static String CHANNEL_MHA = "mha";//硬核
	public static String CHANNEL_MB = "mb";//米宝
	public static String CHANNEL_IOS = "ios";//苹果
//	public static String CHANNEL_9377 = "9377";//9377
	
	//邮件模板id
	public static int MAIL_ID_ZYY = 30005;
	public static int MAIL_ID_MHA = 30006;
	public static int MAIL_ID_MB = 30007;
	public static int MAIL_ID_IOS = 30008;
//	public static int MAIL_ID_9377 = 11;
	
	static{
		channelMailMap = new HashMap<String, Integer>();
		channelMailMap.put(CHANNEL_ZYY, MAIL_ID_ZYY);
		channelMailMap.put(CHANNEL_MB, MAIL_ID_MB);
		channelMailMap.put(CHANNEL_MHA, MAIL_ID_MHA);
		channelMailMap.put(CHANNEL_IOS, MAIL_ID_IOS);
//		channelMailMap.put(CHANNEL_9377, MAIL_ID_9377);
	}
}
