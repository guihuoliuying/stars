package com.stars.modules.newofflinepvp;

import com.stars.modules.newofflinepvp.prodata.OfflineAwardVo;
import com.stars.modules.newofflinepvp.prodata.OfflineInitializeVo;
import com.stars.modules.newofflinepvp.prodata.OfflineMatchVo;

import java.util.*;

/**
 * Created by chenkeyu on 2017-03-12 17:06
 */
public class NewOfflinePvpManager {
    private static List<OfflineMatchVo> offlineMatchVos;
    private static Map<Long, OfflineInitializeVo> offlineInitializeVoMap;//<initializeId , OfflineInitializeVo>
    private static Map<Integer, OfflineAwardVo> offlineAwardVoMap;
    private static Map<Integer, Integer> rankSectionMap;
    private static Map<Byte, Integer> winLoseAwardMap;

    public static int maxRank = 5000;       //维护排行榜最大容量
    public static int maxBattleReport = 10; //战报最大容量
    public static int maxShowRank = 100;    //排行榜最大显示条数
    public static int coachRobotRank = 5002;//教练机器人排名
    public static long coachRobotId = 5002L;//教练机器人id

    public static int champion = 1;         //第一名
    public static int runnerup = 2;         //第二名
    public static int size = 3;             //一次性匹配个数

    public static byte role = 1;
    public static byte robot = 0;

    public static byte active = 1;
    public static byte passivity = 0;

    public static byte victory = 1;
    public static byte defeat = 0;

    public static int onRank = 1;
    public static int notOnRank = 0;

    public static byte first = 0;

    private static int god;
    private static int land;
    private static int person;

    private static int maxFightCount;
    private static int maxBuyCount;

    private static int buyCountItemId;
    private static int buyCountItemCount;

    public static int getGod() {
        return god;
    }

    public static void setGod(int god) {
        NewOfflinePvpManager.god = god;
    }

    public static int getLand() {
        return land;
    }

    public static void setLand(int land) {
        NewOfflinePvpManager.land = land;
    }

    public static int getPerson() {
        return person;
    }

    public static void setPerson(int person) {
        NewOfflinePvpManager.person = person;
    }

    public static void setOfflineMatchVos(List<OfflineMatchVo> offlineMatchVos) {
        NewOfflinePvpManager.offlineMatchVos = offlineMatchVos;
    }

    public static void setOfflineInitializeVos(Map<Long, OfflineInitializeVo> offlineInitializeVos) {
        NewOfflinePvpManager.offlineInitializeVoMap = offlineInitializeVos;
        checkCoach();
    }

    private static void checkCoach() {
        if (!offlineInitializeVoMap.containsKey(NewOfflinePvpManager.coachRobotId)) {
            throw new IllegalArgumentException("竞技场数据|没有教练机器人");
        }
    }

    public static void setOfflineAwardVoMap(Map<Integer, OfflineAwardVo> offlineAwardVoMap) {
        NewOfflinePvpManager.offlineAwardVoMap = offlineAwardVoMap;
    }

    public static Map<Byte, Integer> getWinLoseAwardMap() {
        return winLoseAwardMap;
    }

    public static void setWinLoseAwardMap(Map<Byte, Integer> winLoseAwardMap) {
        NewOfflinePvpManager.winLoseAwardMap = winLoseAwardMap;
    }

    public static OfflineMatchVo getOfflineMatchVo(int rank) {
        for (OfflineMatchVo matchVo : offlineMatchVos) {
            if (matchVo.getMinRank() <= rank && matchVo.getMaxRank() >= rank) {
                return matchVo;
            }
        }
        return null;
    }

    public static void setRankSectionMap(Map<Integer, Integer> rankSectionMap) {
        NewOfflinePvpManager.rankSectionMap = rankSectionMap;
    }

    public static int getMaxFightCount() {
        return maxFightCount;
    }

    public static void setMaxFightCount(int maxFightCount) {
        NewOfflinePvpManager.maxFightCount = maxFightCount;
    }

    public static int getMaxBuyCount() {
        return maxBuyCount;
    }

    public static void setMaxBuyCount(int maxBuyCount) {
        NewOfflinePvpManager.maxBuyCount = maxBuyCount;
    }

    public static int getBuyCountItemId() {
        return buyCountItemId;
    }

    public static void setBuyCountItemId(int buyCountItemId) {
        NewOfflinePvpManager.buyCountItemId = buyCountItemId;
    }

    public static int getBuyCountItemCount() {
        return buyCountItemCount;
    }

    public static void setBuyCountItemCount(int buyCountItemCount) {
        NewOfflinePvpManager.buyCountItemCount = buyCountItemCount;
    }

    public static OfflineInitializeVo getOfflineInitializeVo(long initializeId) {
        return offlineInitializeVoMap.get(initializeId);
    }

    public static Map<Long, OfflineInitializeVo> getOfflineInitializeVoMap() {
        return offlineInitializeVoMap;
    }

    public static Map<Integer, OfflineAwardVo> getOfflineAwardVoMap() {
        return offlineAwardVoMap;
    }

    public static List<OfflineAwardVo> getOfflineAwardVo(int newRank, int oldRank) {
        List<OfflineAwardVo> awardVos = new ArrayList<>();
        for (Map.Entry<Integer, OfflineAwardVo> entry : offlineAwardVoMap.entrySet()) {
            if (entry.getKey() >= newRank && (oldRank == -1 || entry.getKey() < oldRank)) {
                awardVos.add(entry.getValue());
            }
        }
        return awardVos;
    }

    public static Set<Integer> getSectionRankDrop(int newRank, int oldRank) {
        //要考虑oldRank初始值为-1的情况
        Set<Integer> sectionRanks = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : rankSectionMap.entrySet()) {
            if (newRank <= entry.getKey() && (oldRank == -1 || oldRank > entry.getKey())) {
                sectionRanks.add(entry.getValue());
            }
        }
        return sectionRanks;
    }

    public static byte getGodOrLandOrPerson(int newRank, int oldRank) {
        //要考虑oldRank初始值为-1的情况
        if (newRank <= getGod() && (oldRank == -1 || oldRank > getGod())) {
            return 1;//晋升到天榜
        } else if ((newRank > getGod() && newRank <= getLand()) &&
                (oldRank == -1 || (oldRank > getGod() && oldRank > getLand()))) {
            return 2;//晋升到地榜
        } else {
            return 0;//未晋升
        }
    }
}
