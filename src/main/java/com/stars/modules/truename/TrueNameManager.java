package com.stars.modules.truename;

import java.util.HashMap;
import java.util.Map;

public class TrueNameManager {
	
	public static String URL = "http://yhsdk.yhres.cn:4003";

	public static String CHECK_METHOD = "/realname/queryStatus?";

	public static String SAVE_METHOD = "/realname/save?";
	
	public static String appKey = "e45d4508633746cf4fd5c5f6b";
	
	public static String APPID = "1026";
	
	public static String CHECK_OK = "0";
	
	public static String SAVE_OK = "0";
	
	public static int AWARD = 0;
	
	public static int MAIL_ID = 26012;
	
	public static Map<String, String> responseMap = new HashMap<>();
	
	public static boolean isOpen = true;
	
	/*服务下发*/
	public static final byte NOT_IDENTIFICATION = 1;//未实名验证
	
	public static final byte SAVE_SUCCESS = 2;//验证成功
	
	public static final byte SAVE_FAIL = 3;//验证失败
	
	public static final byte LOGIN_NOT_IDENTIFICATION = 4;//登录界面 未实名验证
	
	static{
		responseMap.put("SMZYZ_101", "账号为空");
		responseMap.put("SMZYZ_102", "用户名为空");
		responseMap.put("SMZYZ_103", "证件类型为空");
		responseMap.put("SMZYZ_104", "证件号为空");
		responseMap.put("SMZYZ_105", "用户不存在");
		responseMap.put("SMZYZ_106", "证件格式错误");
		responseMap.put("SMZYZ_107", "证件类型错误");
		responseMap.put("SMZYZ_108", "姓名格式错误");
//		responseMap.put("SMZYZ_109", "此帐号已实名认证");
	}

}
