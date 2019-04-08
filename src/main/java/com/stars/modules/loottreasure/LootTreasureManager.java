package com.stars.modules.loottreasure;

import com.stars.modules.loottreasure.prodata.LootSectionVo;
import com.stars.util.LogUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by panzhenfeng on 2016/10/10.
 */
public class LootTreasureManager {

    private static List<LootSectionVo> lootSectionVoList = null;
    public static Map<Byte, Integer> roomFlagBoxLimitMap = null;
    public static List<LootTreasureRangeParam> loseParamList = null;
    public static int getBoxFormularParamA ;
    public static int getBoxFormularParamB ;
    public static int PVE_WAIT_TIME; //pve等待的时间;
    public static int PVE_FIGHT_TIME;//pve战斗的持续时间;
    public static int PVP_WAIT_TIME;//pvp等待的时间;
    public static int PVP_FIGHT_TIME;//pvp战斗的持续时间;
    public static int PVP_OVER_WAIT_TIME;//PVP战斗结束后,等待多少时间后,对活动进行回收,以及将玩家送回城;
    public static int OVER_EMAIL_TEMPLATE_ID; //活动结束后发送奖励的邮件模版ID;
    public static int PVP_SWITCH_ROOM_CD; //PVP切房间CD;
    public static Map<Byte, LootTreasureRangeParam> pvpSwitchRoomConditionMap ; //PVP切房间的条件;
    public static int PERSON_LIMITCOUNT_PER_ROOM; //每个房间的上限人数;
    public static int CAMP_MIN = 11;
    public static int CAMP_MAX = 20;
    /**
     * 获取获得宝箱的概率;
     * @param boxCount
     * @return
     */
    public static int getBoxCountFormularRate(int boxCount){
        return getBoxFormularParamA + boxCount+getBoxFormularParamB;
    }

    public static int getLoseCountByCurrentBox(int boxCount){
        for (int i = 0, len = loseParamList.size(); i<len; i++){
            if(loseParamList.get(i).isInRange(boxCount) == 0){
                return loseParamList.get(i).getLoseCount();
            }
        }
        return 0;
    }

    public static LootSectionVo getLootSectionVo(int levelSection){
        for(int i = 0, len = lootSectionVoList.size(); i<len; i++){
            if (lootSectionVoList.get(i).getLevelsection() == levelSection){
                return lootSectionVoList.get(i);
            }
        }
        return null;
    }

    public static void setLootSectionVoList(List<LootSectionVo> list){
        lootSectionVoList = list;
        int maxlimitLevel = 0;
        for(int i = 0, len = lootSectionVoList.size(); i<len; i++){
            lootSectionVoList.get(i).setMinLevelSectionLevel(maxlimitLevel+1);
            maxlimitLevel = lootSectionVoList.get(i).getLevelsection();
        }
    }

    public static LootSectionVo getLootSectionVoByLevel(int level){
        for(int i = 0, len = lootSectionVoList.size(); i<len; i++){
            if(lootSectionVoList.get(i).isInRange(level)){
                return lootSectionVoList.get(i);
            }
        }
        return null;
    }

	public static List<LootSectionVo> getLootSectionVoList() {
		return lootSectionVoList;
	}

    public static void log(String message){
        if(LootTreasureConstant.DEBUG){
            LogUtil.info("夺宝活动: "+message);
        }
    }
}
