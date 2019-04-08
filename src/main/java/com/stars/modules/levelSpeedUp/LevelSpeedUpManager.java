package com.stars.modules.levelSpeedUp;

import com.stars.modules.data.DataManager;
import com.stars.modules.levelSpeedUp.productData.LevelSpeedUpAdditionVo;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.RoleRankPo;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LevelSpeedUpManager {
	
	public static Map<Integer, LevelSpeedUpAdditionVo> gadAdditionMap;
	
	public static int MAX_LEVEL_GAD = 20;

	public static int OPEN_DAYS = 0;
	
	public static int TOP_LEVEL = 1;//全服最高玩家等级
	
	public static int MEAN_LEVEL = 1;//等级排行前 MEAN_NUM 个玩家的等级平均值
	
	public static int MEAN_NUM = 10;//平均值基数
	
	public static int START_LEVEL = 30;//开始享有加成的等级
	
	public static int OPEN_DAYS_STANDARD = 10;//开服多少天后满足等级加成条件
	
	public static int TOP_LEVEL_STANDARD = 50;//最高玩家等级标准
	
	public static int GAD_STANDARD = 5;//和最高玩家等级的等级差距条件
	
	public static void conditionReset(){
		try {
			List<AbstractRankPo> list = ServiceHelper.rankService().getFrontRank(RankConstant.RANKID_ROLELEVEL, MEAN_NUM);
			if(StringUtil.isNotEmpty(list)){
				RoleRankPo rankPo = (RoleRankPo)list.get(0);
				int roleLevel = rankPo.getRoleLevel();
				TOP_LEVEL = roleLevel;
				int size = list.size();
				int totalValue = 0;
				for(int i=0;i<size;i++){
					rankPo = (RoleRankPo)list.get(i);
					totalValue += rankPo.getRoleLevel();
				}
				MEAN_LEVEL = totalValue/size;
			}
			Date openServerDate = DataManager.getOpenServerDate();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(openServerDate);
			Date dateFrom = DateUtil.getZeroTimeDate(calendar);
			calendar.setTime(new Date());
			Date dateEnd = DateUtil.getZeroTimeDate(calendar);
			OPEN_DAYS = DateUtil.getDaysBetweenTwoDates(dateFrom, dateEnd);
		} catch (Exception e) {
			LogUtil.error("LevelSpeedUpManager, conditionReset fail", e);
		}
	}
	
}
