package com.stars.modules.daily.prodata;

import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/7/7.
 */
public class DailyAwardVo {
    private int dailyAwardId; //日常奖励id
    private byte awardType; //奖励类型 1-超级大奖 2-多倍奖励
    private short dailyid; //日常id
    private String levelSection; //等级段
    private int mutiple; //奖励倍数
    private String fightScoreSection; //战力差段
    private byte superAwardType; //超级奖励段数 1-普通 2-高级 3-超量
    private String awardStr; //奖励内容
    private String showawardstr; //显示的奖励

    //内存内容，无数据库字段对应
    private int minLevel;
    private int maxLevel;
    private int minFightDelta;
    private int maxFightDelta;
    private Map<Integer,Integer> awardMap = new HashMap<>();
    private Map<Integer,Integer> showAwardMap = new LinkedHashMap<>();

    public int getDailyAwardId() {
        return dailyAwardId;
    }

    public void setDailyAwardId(int dailyAwardId) {
        this.dailyAwardId = dailyAwardId;
    }

    public byte getAwardType() {
        return awardType;
    }

    public void setAwardType(byte awardType) {
        this.awardType = awardType;
    }

    public short getDailyid() {
        return dailyid;
    }

    public void setDailyid(short dailyid) {
        this.dailyid = dailyid;
    }

    public String getLevelSection() {
        return levelSection;
    }

    public void setLevelSection(String levelSection) {
        this.levelSection = levelSection;
        if(StringUtil.isEmpty(levelSection)||levelSection.equals("0"))
            return;
        String[] array = levelSection.split("[+]");
        minLevel = Integer.parseInt(array[0]);
        maxLevel = Integer.parseInt(array[1]);
    }

    public int getMutiple() {
        return mutiple;
    }

    public void setMutiple(int mutiple) {
        this.mutiple = mutiple;
    }

    public String getFightScoreSection() {
        return fightScoreSection;
    }

    public void setFightScoreSection(String fightScoreSection) {
        this.fightScoreSection = fightScoreSection;
        if(StringUtil.isEmpty(fightScoreSection) || fightScoreSection.equals("0"))
            return;
        String[] array = fightScoreSection.split("[+]");
        minFightDelta = Integer.parseInt(array[0]);
        maxFightDelta = Integer.parseInt(array[1]);
    }

    public String getAwardStr() {
        return awardStr;
    }

    public void setAwardStr(String awardStr) {
        this.awardStr = awardStr;
        awardMap = StringUtil.toMap(awardStr, Integer.class, Integer.class, '=', '|');
    }

    public String getShowawardstr() {
        return showawardstr;
    }

    public void setShowawardstr(String showawardstr) {
        this.showawardstr = showawardstr;
        this.showAwardMap = StringUtil.toMap(showawardstr,Integer.class,Integer.class,'=','|');
    }

    public boolean matchAwardType(byte awardType){
        return this.awardType == awardType;
    }

    public boolean matchLevel(int level){
        return (level >= minLevel && level <= maxLevel) || StringUtil.isEmpty(levelSection) || levelSection.equals("0");
    }

    public boolean matchFightScore(int fightScore){
        return (fightScore >= minFightDelta && fightScore <= maxFightDelta) || StringUtil.isEmpty(fightScoreSection) || fightScoreSection.equals("0");
    }

    public boolean matchSuperAwardType(byte superAwardType){
        return superAwardType == this.superAwardType || this.superAwardType == (byte)0;
    }

    public boolean matchDailyId(short dailyId){
        return dailyId == this.dailyid;
    }

    public byte getSuperAwardType() {
        return superAwardType;
    }

    public void setSuperAwardType(byte superAwardType) {
        this.superAwardType = superAwardType;
    }

    public Map<Integer, Integer> getAwardMap() {
        return awardMap;
    }

    public void setAwardMap(Map<Integer, Integer> awardMap) {
        this.awardMap = awardMap;
    }

    public Map<Integer, Integer> getShowAwardMap() {
        return showAwardMap;
    }

    public void setShowAwardMap(Map<Integer, Integer> showAwardMap) {
        this.showAwardMap = showAwardMap;
    }
}
