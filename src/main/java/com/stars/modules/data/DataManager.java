package com.stars.modules.data;

import com.stars.core.SystemRecordMap;
import com.stars.modules.data.prodata.ActivityFlowStepVo;
import com.stars.util.DateUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zd on 2015/3/31.
 */
public class DataManager {
	public static Map<String, String> commonConfigMap = new HashMap<>();
	public static Map<String, String> gametextMap = new HashMap<>();
	public static Map<Integer,String> qualityColorMap = new HashMap<>();
	public static Map<String ,Integer> gradecoeffMap = new HashMap<>();//<key:等级 value<key:typeid,value:系数>>

	/**战力计算的属性系数dic;*/
	public static Map<String, Float> battlePowerRationDic = new HashMap<>();

	//
	public static Map<Integer, List<ActivityFlowStepVo>> activityFlowConfigMap;

	/* 每日重置时间 */
	public static String DAILY_RESET_TIME_STR = "00:00:00";

	public static Map<Integer, Integer> getTool(String key) {
		return StringUtil.toMap(commonConfigMap.get(key), Integer.class, Integer.class, '+', ',');
	}


	public static int getGradeCoeff(String args){
		return gradecoeffMap.get(args);
	}

	public static String getCommConfig(String key){
		return commonConfigMap.get(key);
	}

	public static byte getCommConfig(String key, byte defaultValue) {
		return com.stars.util.MapUtil.getByte(commonConfigMap, key, defaultValue);
	}

	public static double getCommConfig(String key, double defaultValue) {
		return com.stars.util.MapUtil.getDouble(commonConfigMap, key, defaultValue);
	}

	public static int getCommConfig(String key, int defaultValue) {
		return com.stars.util.MapUtil.getInt(commonConfigMap, key, defaultValue);
	}

	public static short getCommConfig(String key, short defaultValue) {
		return com.stars.util.MapUtil.getShort(commonConfigMap, key, defaultValue);
	}

	public static String getCommConfig(String key, String defaultValue) {
		return MapUtil.getString(commonConfigMap, key, defaultValue);
	}

	public static String getGametext(String key){
		return gametextMap.get(key);
	}

	//一些服务器提示的信息部分也需要有颜色需求,如warn那里;
	public static String getQualityColor(int quality){ return qualityColorMap.get(quality);}


	public static void init() {
		initBattlePowerRatio();
		initQualityColor();
	}

	private static void initBattlePowerRatio(){
		String tmpStr = getCommConfig("battlepowerratio");
		String[] itemArr =  tmpStr.split("\\|");
		String[] subItemArr = null;
		Map<String, Float> battlePowerRationDic = new HashMap<>();
		for(int  i =0, len = itemArr.length; i<len; i++){
			subItemArr = itemArr[i].split("=");
			battlePowerRationDic.put(subItemArr[0], Float.parseFloat(subItemArr[1]));
		}

		DataManager.battlePowerRationDic = battlePowerRationDic;
	}

	private static void initQualityColor(){
		String tmpStr = getCommConfig("qualitycolor");
		String[] itemArr =  tmpStr.split("\\|");
		String[] subItemArr = null;
		Map<Integer, String> qualityColorMap = new HashMap<>();
		for(int  i =0, len = itemArr.length; i<len; i++){
			subItemArr = itemArr[i].split("\\+");
			qualityColorMap.put(Integer.parseInt(subItemArr[0]), subItemArr[1]);
		}

		DataManager.qualityColorMap = qualityColorMap;
	}

	/**
	 * 获取开服天数
	 * @param curMillis
	 * @return
     */
//	public static int getServerDays(long curMillis) {
//		if (SystemRecordMap.openServerTime == 0) {	// 如果没有设置开服时间，暂时把当前时间设置为开服时间
//			SystemRecordMap.update("openServerTime", Long.valueOf(DateUtil.getYMDHMSStr()));
//			return 0;
//		}
//		final long dayBase = 1000000;
//		long now = Long.valueOf(new SimpleDateFormat(DateUtil.YMDHMS).format(new Date(curMillis)));
//		int day = (int) (now / dayBase - SystemRecordMap.openServerTime / dayBase) + 1;	// 开服算第一天
//		/** 按自然日算 */
//		/*if (now % dayBase < SystemRecordMap.openServerTime % dayBase) {
//			day = day - 1;
//		}*/
//		return day;
//	}
	
	/**
	 * 获取开服date
	 * @return
     */
	public static Date getOpenServerDate(){
		if (SystemRecordMap.openServerTime == 0) {	// 如果没有设置开服时间，暂时把当前时间设置为开服时间
			SystemRecordMap.update("openServerTime", Long.valueOf(DateUtil.getYMDHMSStr()));
			return new Date();
		}
		
		String dataStrYMDHMS = Long.toString(SystemRecordMap.openServerTime);
		Date openServerDate = DateUtil.toDate(dataStrYMDHMS);
		
		return openServerDate;
	}
	
	
	/**
	 * 获取开服天数
	 * @param curMillis
	 * @return
     */
	public static int getServerDays(long curMillis) {	
		Date openServerDate = getOpenServerDate();
		if (openServerDate == null) return 0;
		/** 按自然日算 */		
		Date curDate = new Date(curMillis);
		int day = DateUtil.getRelativeDifferDays(openServerDate, curDate) + 1;	// 开服算第一天
		
		return day;
	}

	/**
	 * 获取开服天数
	 * @return
     */
	public static int getServerDays() {
		return getServerDays(System.currentTimeMillis());
	}

	public static Map<Integer, String> getActivityFlowConfig(int activityId) {
		if (!activityFlowConfigMap.containsKey(activityId)) {
			return null;
		}
		Map<Integer, String> map = new HashMap<>();
		for (ActivityFlowStepVo vo : activityFlowConfigMap.get(activityId)) {
			map.put(vo.getStep(), vo.getCronExpr());
		}
		return map;
	}

}
