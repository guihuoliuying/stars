package com.stars.modules.escort;

import com.stars.modules.escort.prodata.CargoAIVo;
import com.stars.modules.escort.prodata.CargoCarVo;
import com.stars.modules.escort.prodata.CargoMonsterVo;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/2.
 */
public class EscortManager {

    private static Map<Integer, CargoCarVo> CARGO_CAR_VO_MAP;
    private static List<CargoAIVo> CARGO_AI_VO_LIST;
    private static Map<Integer,CargoAIVo> CARGO_AI_VO_MAP;
    private static List<CargoMonsterVo> CARGO_MONSTER_VO_LIST;
    private static Map<Integer,Integer> CARGOCAR_LOSE_CARGO_MAP;//百分比，表示第一次和第二次被劫镖损失的货物比例

    private static LinkedHashMap<Integer,Integer> TEAM_MEMBER_COUNT_LIMIT;  //vip等级对应可邀请队员数量

    public static EscortActivityFlow activityFlow;

    private static int CARGOCAR_SAFEID;         //镖车列队场景
    private static int CARGOCAR_STAGEID;        //运镖和战斗场景
    private static int OPEN_LEVEL;              //运镖系统开启等级
    private static int TEAM_MODE_LEVEL;         //组队模式开启等级
    private static int CARGOCAR_COEFFICIENT;    //用于计算和获取镖车
    private static int CARGOCAR_CARCOUNT;       //镖车队列场景初始取车上限
    private static int CARGOCAR_DAY_COUNT;      //每天运镖次数
    private static int CARGOCAR_ROB_COUNT;      //每天劫镖次数
    private static int CARGOCAR_DAILY_REFRESH_COUNT;   //每天可手动刷新次数
    private static int CARGOCAR_PROTECT_TIME;   //镖车被劫后的保护时间
    private static long CARGOCAR_ENEMY_TIME;     //仇人关系维持的时间
    private static int CARGOCAR_ENEMY_BUFF_ID;   //buffid  表示劫镖队伍攻击仇人运镖队伍时附加的buff
    private static int CARGOCAR_ENEMY_BUFF_LEVEL;//buff_level  表示劫镖队伍攻击仇人运镖队伍时附加的buff的等级
    private static int CARGOCAR_MASK;           //使用面具劫镖时，消耗多少个面具
    private static int CARGOCAR_MASK_ITEMID;    //面具itemId
    private static int CARGOCAR_REFRESH_COUNT;  //刷新镖车选择列表所需元宝数量
    private static int CARGOCAR_REFRESH_ITEMID; //刷新镖车选择列表所需元宝itemId
    private static int CARGOCAR_FIGHT_TIME;     //每次劫镖战斗持续的时间，超时算劫镖失败
    private static int CARGOCAR_DOUBLE;      //所有奖励如果不是在活动时间段内进行，则乘以此百分比

    private static Map<Integer,int[]> CARGO_GET_CAR_COUNT_COEF;//各个carid镖车取车时的战力范围
    private static String[] movePositionData;   //镖车根据顺序逐个移动向寻路点

    public static int ESCORT_TIME = 600;  // 运镖时长

    /**************************以下是对应产品数据的get/set方法*************************/
    public static Map<Integer, CargoCarVo> getCargoCarVoMap() {
        return CARGO_CAR_VO_MAP;
    }

    public static void setCargoCarVoMap(Map<Integer, CargoCarVo> cargoCarVoMap) {
        CARGO_CAR_VO_MAP = cargoCarVoMap;
    }

    public static CargoCarVo getCargoCarVoById(int carId){
        if(StringUtil.isEmpty(CARGO_CAR_VO_MAP)) return null;
        return CARGO_CAR_VO_MAP.get(carId);
    }

    public static void setCargoAiVoList(List<CargoAIVo> cargoAiVoList) {
        CARGO_AI_VO_LIST = cargoAiVoList;
        int minLimitLevel = 0;
        for(int i = 0, len = CARGO_AI_VO_LIST.size(); i<len; i++){
            CARGO_AI_VO_LIST.get(i).setMinScoreSection(minLimitLevel + 1);
            minLimitLevel = CARGO_AI_VO_LIST.get(i).getPowerSection();
        }

        CARGO_AI_VO_MAP = new HashMap<>();
        for(CargoAIVo cargoAIVo:CARGO_AI_VO_LIST){
            CARGO_AI_VO_MAP.put(cargoAIVo.getPowerSection(),cargoAIVo);
        }
    }

    public static CargoAIVo getCargoAiVoByPowerSection(int section) {
        if(StringUtil.isEmpty(CARGO_AI_VO_MAP)) return null;
        return CARGO_AI_VO_MAP.get(section);
    }

    public static CargoAIVo getCargoAiByFightScore(int fightScore){
        for(CargoAIVo aiVo : CARGO_AI_VO_LIST){
            if(aiVo.isInRange(fightScore)){
                return aiVo;
            }
        }
        return CARGO_AI_VO_LIST.get(CARGO_AI_VO_LIST.size() - 1);
    }

    public static void setCargoMonsterVoList(List<CargoMonsterVo> cargoMonsterVoList) {
        CARGO_MONSTER_VO_LIST = cargoMonsterVoList;
        int minLimitLevel = 0;
        for(int i = 0, len = CARGO_MONSTER_VO_LIST.size(); i<len; i++){
            CARGO_MONSTER_VO_LIST.get(i).setMinScoreSection(minLimitLevel + 1);
            minLimitLevel = CARGO_MONSTER_VO_LIST.get(i).getPowerSection();
        }
    }

    public static CargoMonsterVo getCargoMonsterByFightScore(int fightScore){
        if(StringUtil.isEmpty(CARGO_MONSTER_VO_LIST)) return null;
        for(CargoMonsterVo monsterVo : CARGO_MONSTER_VO_LIST){
            if(monsterVo.isInRange(fightScore)){
                return monsterVo;
            }
        }
        return CARGO_MONSTER_VO_LIST.get(CARGO_MONSTER_VO_LIST.size() - 1);
    }

    public static LinkedHashMap<Integer, Integer> getTeamMemberCountLimit() {
        return TEAM_MEMBER_COUNT_LIMIT;
    }

    public static void setTeamMemberCountLimit(LinkedHashMap<Integer, Integer> teamMemberCountLimit) {
        TEAM_MEMBER_COUNT_LIMIT = teamMemberCountLimit;
    }

    public static int getCargocarSafeid() {
        return CARGOCAR_SAFEID;
    }

    public static void setCargocarSafeid(int cargocarSafeid) {
        CARGOCAR_SAFEID = cargocarSafeid;
    }

    public static int getCargocarStageid() {
        return CARGOCAR_STAGEID;
    }

    public static void setCargocarStageid(int cargocarStageid) {
        CARGOCAR_STAGEID = cargocarStageid;
    }

    public static int getOpenLevel() {
        return OPEN_LEVEL;
    }

    public static void setOpenLevel(int openLevel) {
        OPEN_LEVEL = openLevel;
    }

    public static int getTeamModeLevel() {
        return TEAM_MODE_LEVEL;
    }

    public static void setTeamModeLevel(int teamModeLevel) {
        TEAM_MODE_LEVEL = teamModeLevel;
    }

    public static int getCargocarCoefficient() {
        return CARGOCAR_COEFFICIENT;
    }

    public static void setCargocarCoefficient(int cargocarCoefficient) {
        CARGOCAR_COEFFICIENT = cargocarCoefficient;
    }

    public static int getCargocarCarcount() {
        return CARGOCAR_CARCOUNT;
    }

    public static void setCargocarCarcount(int cargocarCarcount) {
        CARGOCAR_CARCOUNT = cargocarCarcount;
    }

    public static int getCargocarDayCount() {
        return CARGOCAR_DAY_COUNT;
    }

    public static void setCargocarDayCount(int cargocarDayCount) {
        CARGOCAR_DAY_COUNT = cargocarDayCount;
    }

    public static int getCargocarRobCount() {
        return CARGOCAR_ROB_COUNT;
    }

    public static void setCargocarRobCount(int cargocarRobCount) {
        CARGOCAR_ROB_COUNT = cargocarRobCount;
    }

    public static int getCargocarProtectTime() {
        return CARGOCAR_PROTECT_TIME;
    }

    public static void setCargocarProtectTime(int cargocarProtectTime) {
        CARGOCAR_PROTECT_TIME = cargocarProtectTime;
    }

    public static long getCargocarEnemyTime() {
        return CARGOCAR_ENEMY_TIME;
    }

    public static void setCargocarEnemyTime(long cargocarEnemyTime) {
        CARGOCAR_ENEMY_TIME = cargocarEnemyTime;
    }

    public static int getCargocarEnemyBuffId() {
        return CARGOCAR_ENEMY_BUFF_ID;
    }

    public static void setCargocarEnemyBuffId(int cargocarEnemyBuffId) {
        CARGOCAR_ENEMY_BUFF_ID = cargocarEnemyBuffId;
    }

    public static int getCargocarEnemyBuffLevel() {
        return CARGOCAR_ENEMY_BUFF_LEVEL;
    }

    public static void setCargocarEnemyBuffLevel(int cargocarEnemyBuffLevel) {
        CARGOCAR_ENEMY_BUFF_LEVEL = cargocarEnemyBuffLevel;
    }

    public static int getCargocarMask() {
        return CARGOCAR_MASK;
    }

    public static void setCargocarMask(int cargocarMask) {
        CARGOCAR_MASK = cargocarMask;
    }

    public static int getCargocarMaskItemid() {
        return CARGOCAR_MASK_ITEMID;
    }

    public static void setCargocarMaskItemid(int cargocarMaskItemid) {
        CARGOCAR_MASK_ITEMID = cargocarMaskItemid;
    }

    public static int getCargocarRefreshCount() {
        return CARGOCAR_REFRESH_COUNT;
    }

    public static void setCargocarRefreshCount(int cargocarRefreshCount) {
        CARGOCAR_REFRESH_COUNT = cargocarRefreshCount;
    }

    public static int getCargocarRefreshItemid() {
        return CARGOCAR_REFRESH_ITEMID;
    }

    public static void setCargocarRefreshItemid(int cargocarRefreshItemid) {
        CARGOCAR_REFRESH_ITEMID = cargocarRefreshItemid;
    }

    public static Map<Integer, Integer> getCargocarLoseCargoMap() {
        return CARGOCAR_LOSE_CARGO_MAP;
    }

    public static void setCargocarLoseCargoMap(Map<Integer, Integer> cargocarLoseCargoMap) {
        CARGOCAR_LOSE_CARGO_MAP = cargocarLoseCargoMap;
    }

    public static int getCargocarFightTime() {
        return CARGOCAR_FIGHT_TIME;
    }

    public static void setCargocarFightTime(int cargocarFightTime) {
        CARGOCAR_FIGHT_TIME = cargocarFightTime;
    }

    public static int getCargocarDouble() {
        return CARGOCAR_DOUBLE;
    }

    public static void setCargocarDouble(int cargocarDouble) {
        CARGOCAR_DOUBLE = cargocarDouble;
    }

    public static Map<Integer, int[]> getCargoGetCarCountCoef() {
        return CARGO_GET_CAR_COUNT_COEF;
    }

    public static void setCargoGetCarCountCoef(Map<Integer, int[]> cargoGetCarCountCoef) {
        CARGO_GET_CAR_COUNT_COEF = cargoGetCarCountCoef;
    }

    /**
     * 根据vip等级获得可组队人数
     */
    public static int getMemberCount(int vipLevel){
        int memberCount = 2;
        if(StringUtil.isEmpty(TEAM_MEMBER_COUNT_LIMIT)) return memberCount;
        for(Map.Entry<Integer,Integer> entry:TEAM_MEMBER_COUNT_LIMIT.entrySet()){
            if(vipLevel >= entry.getKey()){
                memberCount = entry.getValue();
            }else{
                return memberCount;
            }
        }
        return memberCount;
    }

    /**
     * 根据被劫镖次数获得货物损失比例
     */
    public static int getLoseCargo(int robTimes){
        if(robTimes < 1 || robTimes > 2) return 0;
        return CARGOCAR_LOSE_CARGO_MAP.get(robTimes);
    }

    public static String[] getMovePositionData() {
        return movePositionData;
    }

    public static void setMovePositionData(String[] movePositionData) {
        EscortManager.movePositionData = movePositionData;
    }

    public static int getCargocarDailyRefreshCount() {
        return CARGOCAR_DAILY_REFRESH_COUNT;
    }

    public static void setCargocarDailyRefreshCount(int cargocarDailyRefreshCount) {
        CARGOCAR_DAILY_REFRESH_COUNT = cargocarDailyRefreshCount;
    }
}
