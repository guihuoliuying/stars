package com.stars.modules.masternotice;

import com.stars.modules.masternotice.prodata.MasterNoticeVo;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.prodata.VipinfoVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/11/19.
 */
public class MasterNoticeManager {
	public static int refreshCoolDownTime = 0;//单位：秒
	public static int freeRefreshCount = 0;//免费刷新次数
	public static int costRefreshCount = 0;//扣道具刷新次数
	public static Map<Integer, Integer> refreshCost = new HashMap<Integer, Integer>();
	public static List<Integer> firstNoticeIdList = new ArrayList<Integer>();
	
	private static Map<Integer, MasterNoticeVo> masterNoticeVoMap = null;
	
	public static void setMasterNoticeVoMap(Map<Integer, MasterNoticeVo> map){
		masterNoticeVoMap = map;
    }
	
	public static Map<Integer, MasterNoticeVo> getMasterNoticeVoMap(){
		return masterNoticeVoMap;
	}
	
	public static MasterNoticeVo getMasterNoticeVoById(int noticeId){
		return masterNoticeVoMap.get(noticeId);
	}
	
	public static Map<Integer, MasterNoticeVo> getMasterNoticesByLevel(int level){
		Map<Integer, MasterNoticeVo> retMap = new HashMap<Integer, MasterNoticeVo>();
		
		if (masterNoticeVoMap != null) {
			for (MasterNoticeVo vo : masterNoticeVoMap.values()) {
				if (level >= vo.getMinLevel() && level <= vo.getMaxLevel()) {
    				retMap.put(vo.getNoticeId(), vo);
    			}
			}
		}
		
		return retMap;
	}
	
	public static int getTotalCountByNobelLevel(int nobelLevel){
		if (nobelLevel < 0) return 0;
		VipinfoVo vipinfoVo = VipManager.getVipinfoVo(nobelLevel);
		if(vipinfoVo == null) return 0;
		return vipinfoVo.getNoticeCount();
//		int temp = 0;
//		for (Map.Entry<Integer, Integer> entry : nobleCountMap.entrySet()) {
//			if (entry.getKey() > nobelLevel) {
//				return temp;
//			}
//			temp = entry.getValue();
//		}
//
//		return temp;
	}
}
