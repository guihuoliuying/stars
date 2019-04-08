package com.stars.modules.teamdungeon.prodata;

import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

public class TeamDungeonVo {
    /**
     * 组队副本id
     */
    private int teamdungeonid;

    /**
     * 入口
     */
    private byte entrance;

    /**
     * 组队副本类别，标识副本玩法
     */
    private byte type;

    /**
     * 组队副本名称
     */
    private String name;

    /**
     * 进入副本的最小等级
     */
    private short levellimit;

    /**
     * 组队副本对应的副本信息，格式 ：等级上限1+stageid1，等级上限2+stageid2，等级上限3+stageid3
     */
    private String stageid;

    /**
     * 目标血量奖励
     */
    private String targethpreward;

    /**
     * 通关奖励
     */
    private String sucreward;

    /**
     * 失败奖励
     */
    private String failreward;

    /**
     * 伤害奖励。格式为：伤害上限1，物品id1+数量，物品id2+数量|伤害上限2，物品id1+数量，物品id2+数量
     */
    private String damagereward;

    /**
     * 守护目标物的怪物id，配0代表不存在
     */
    private int targetmonster;

    /**
     * 目标物存活图片+目标物死亡图片
     */
    private String targetimg;

    /**
     * 对应日常的id
     */
    private short dailyid;

    /**
     * 客户端显示顺序
     */
    private int displayorder;
    // 组队关系加成,格式:itemid,itemid|type1+百分数,type2+百分数,type3+百分数
    private String addPercent;

    private String timeReward;

    /* 内存数据 */
    private Map<Byte, Map<Integer, Integer>> protectRewards = new HashMap<>();// 守护奖励
    private Map<Integer, Map<Integer, Integer>> damageRewards = new LinkedHashMap<>();// 伤害奖励
    private Map<Integer, Integer> victoryRewards = new HashMap<>();// 胜利奖励
    private Map<Integer, Integer> defeatRewards = new HashMap<>();// 失败奖励
    private Map<Integer, Integer> stageIdMap = new LinkedHashMap<>();// 战场配置信息
    private Set<Integer> addRewardItemId = new HashSet<>();// 组队关系加成奖励物品
    private Map<Byte, Integer> addPercentMap = new HashMap<>();// 组队关系加成系数
    private Map<Integer, Integer> timerAwardMap = new LinkedHashMap<>();

    public boolean containAddReward(int itemId) {
        return addRewardItemId.contains(itemId);
    }

    public int getAddRewardPercent(byte type) {
        if (!addPercentMap.containsKey(type))
            return 0;
        return addPercentMap.get(type);
    }

    public Map<Byte, Map<Integer, Integer>> getProtectRewards() {
        return protectRewards;
    }

    public Map<Integer, Map<Integer, Integer>> getDamageRewards() {
        return damageRewards;
    }

    public Map<Integer, Integer> getVictoryRewards() {
        return victoryRewards;
    }

    public Map<Integer, Integer> getDefeatRewards() {
        return defeatRewards;
    }

    public Map<Integer, Integer> getStageIdMap() {
        return stageIdMap;
    }

    public int getTeamdungeonid() {
        return teamdungeonid;
    }

    public void setTeamdungeonid(int teamdungeonid) {
        this.teamdungeonid = teamdungeonid;
    }

    public byte getEntrance() {
        return entrance;
    }

    public void setEntrance(byte entrance) {
        this.entrance = entrance;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public short getLevellimit() {
        return levellimit;
    }

    public void setLevellimit(short levellimit) {
        this.levellimit = levellimit;
    }

    public String getStageid() {
        return stageid;
    }

    public void setStageid(String stageid) {
        this.stageid = stageid;
        if (StringUtil.isEmpty(stageid) || "0".equals(stageid))
            return;
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (String str : stageid.split("\\,")) {
            String tempStr[] = str.split("\\+");
            if (tempStr.length >= 2) {
                int level = Integer.parseInt(tempStr[0]);
                int sId = Integer.parseInt(tempStr[1]);
                map.put(level, sId);
            }
        }
        stageIdMap = map;
    }

    public String getTargethpreward() {
        return targethpreward;
    }

    public void setTargethpreward(String targethpreward) throws Exception {
        this.targethpreward = targethpreward;
        if (StringUtil.isEmpty(targethpreward) || "0".equals(targethpreward))
            return;
        for (String str : targethpreward.split("\\|")) {
            byte key = Byte.parseByte(str.substring(0, str.indexOf(",")));
            Map<Integer, Integer> rewardMap = StringUtil.toMap(str.substring(str.indexOf(",") + 1, str.length()),
                    Integer.class, Integer.class, '+', ',');
            protectRewards.put(key, rewardMap);
        }
    }

    public String getSucreward() {
        return sucreward;
    }

    public void setSucreward(String sucreward) throws Exception {
        this.sucreward = sucreward;
        if (StringUtil.isEmpty(sucreward) || "0".equals(sucreward))
            return;
        victoryRewards = StringUtil.toMap(sucreward, Integer.class, Integer.class, '+', ',');
    }

    public String getFailreward() {
        return failreward;
    }

    public void setFailreward(String failreward) throws Exception {
        this.failreward = failreward;
        if (StringUtil.isEmpty(failreward) || "0".equals(failreward))
            return;
        defeatRewards = StringUtil.toMap(failreward, Integer.class, Integer.class, '+', ',');
    }

    public int getTargetmonster() {
        return targetmonster;
    }

    public void setTargetmonster(int targetmonster) {
        this.targetmonster = targetmonster;
    }

    public String getTargetimg() {
        return targetimg;
    }

    public void setTargetimg(String targetimg) {
        this.targetimg = targetimg;
    }

    public String getDamagereward() {
        return damagereward;
    }

    public void setDamagereward(String damagereward) throws Exception {
        this.damagereward = damagereward;
        if (StringUtil.isEmpty(damagereward) || "0".equals(damagereward))
            return;
        for (String str : damagereward.split("\\|")) {
            int key = Integer.parseInt(str.substring(0, str.indexOf(",")));
            Map<Integer, Integer> rewardMap = StringUtil.toMap(str.substring(str.indexOf(",") + 1, str.length()),
                    Integer.class, Integer.class, '+', ',');
            damageRewards.put(key, rewardMap);
        }
    }

    public short getDailyid() {
        return dailyid;
    }

    public void setDailyid(short dailyid) {
        this.dailyid = dailyid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayorder() {
        return displayorder;
    }

    public void setDisplayorder(int displayorder) {
        this.displayorder = displayorder;
    }

    public int getStageIdByLevel(int level) {
        int ret = -1;
        if (level <= 0) {
            return ret;
        }
        int flag = 0;
        for (Map.Entry<Integer, Integer> entry : stageIdMap.entrySet()) {
            ret = entry.getValue();
            if (level > flag && level <= entry.getKey()) {
                break;
            }
            flag = entry.getKey();
        }
        return ret;
    }

    public String getAddPercent() {
        return addPercent;
    }

    public void setAddPercent(String addPercent) throws Exception {
        this.addPercent = addPercent;
        if (StringUtil.isEmpty(addPercent) || "0".equals(addPercent)) {
            return;
        }
        String[] temp = addPercent.split("\\|");
        addRewardItemId = new HashSet<>();
        addRewardItemId.addAll(StringUtil.toArrayList(temp[0], Integer.class, '+'));
        addPercentMap = StringUtil.toMap(temp[1], Byte.class, Integer.class, '+', ',');
    }

    public String getTimeReward() {
        return timeReward;
    }

    public void setTimeReward(String timeReward) {
        this.timeReward = timeReward;
        if (timeReward == null || timeReward.equals("") || timeReward.equals("0"))
            return;
        this.timerAwardMap = StringUtil.toMap(timeReward, Integer.class, Integer.class, '+', ',');
    }

    public int getTimeDropId(int time) {
        LinkedList<Integer> timeList = new LinkedList<>();
        for (Map.Entry<Integer, Integer> entry : timerAwardMap.entrySet()) {
            if (time <= entry.getKey()) {
                timeList.add(entry.getKey());
            }
        }
        int minTime = Collections.min(timeList);
        LogUtil.info("时间奖励|dropId:{}", timerAwardMap.get(minTime));
        return timerAwardMap.get(minTime);
    }

    @Override
    public String toString() {
        return "TeamDungeonVo{" +
                "teamdungeonid=" + teamdungeonid +
                ", entrance=" + entrance +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", levellimit=" + levellimit +
                ", stageid='" + stageid + '\'' +
                ", targethpreward='" + targethpreward + '\'' +
                ", sucreward='" + sucreward + '\'' +
                ", failreward='" + failreward + '\'' +
                ", damagereward='" + damagereward + '\'' +
                ", targetmonster=" + targetmonster +
                ", targetimg='" + targetimg + '\'' +
                ", dailyid=" + dailyid +
                ", displayorder=" + displayorder +
                ", addPercent='" + addPercent + '\'' +
                ", timeReward='" + timeReward + '\'' +
                '}';
    }
}
