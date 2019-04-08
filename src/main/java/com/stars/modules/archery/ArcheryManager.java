package com.stars.modules.archery;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huzhipeng
 * 2017-06-08
 */
public class ArcheryManager {
	
	public static Map<int[], Integer> integralAwardMap = new HashMap<>();
	
	public static int MaxIntegral = 0;
	
	public static byte TotalPlayNum = 2;//总可参与次数（也可以理解为可领取奖励次数）
	
	/*服务下发*/
	public static final byte PLAY_TIMES = 1;//剩余次数返回
	
	public static final byte SHOW_AWARD = 2;//展示奖励信息
	
	/*客户端上传*/
	public static final byte GET_PLAY_TIMES = 1;//获取剩余次数
	
	public static final byte SYN_INTEGRAL = 2;//同步积分
	
	public static final byte GET_AWARD = 3;//获取奖励
	
	public static final byte START_PLAY = 4;//开始游戏
	
}
