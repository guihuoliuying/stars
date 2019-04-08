package com.stars.modules.buddy.summary;

import com.stars.core.attr.Attribute;
import com.stars.modules.buddy.BuddyManager;
import com.stars.modules.buddy.prodata.BuddyStageVo;
import com.stars.modules.buddy.userdata.RoleBuddy;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/3.
 */
public class BuddySummaryVo {
    private int buddyId;
    private int level;// '当前等级'
    private int stageLevel;// '阶级'
    private Attribute buddyAttr = new Attribute();
    private int fightScore;
    private byte isFight;// '是否出战'
    private int monsterId;
    private int armLevel;// 武装等级

    public BuddySummaryVo() {
    }

    public BuddySummaryVo(RoleBuddy roleBuddy) {
        if (roleBuddy == null) return;
        this.buddyId = roleBuddy.getBuddyId();
        this.level = roleBuddy.getLevel();
        this.stageLevel = roleBuddy.getStageLevel();
        this.buddyAttr = roleBuddy.getAttribute().clone();
        this.fightScore = roleBuddy.getFightScore();
        this.isFight = roleBuddy.getIsFight();
        BuddyStageVo buddyStageVo = BuddyManager.getBuddyStageVo(this.buddyId,this.stageLevel);
        if(buddyStageVo!=null){
            this.monsterId = buddyStageVo.getMonsterId();
        }
    }

    public BuddySummaryVo(String str) throws Exception  {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');
        this.buddyId = com.stars.util.MapUtil.getInt(map, "buddyId", 0);
        this.level = com.stars.util.MapUtil.getInt(map, "level", 1);
        this.stageLevel = com.stars.util.MapUtil.getInt(map, "stageLevel", 1);
        String attributeString = com.stars.util.MapUtil.getString(map, "buddyAttr", "");
        this.buddyAttr = new Attribute(attributeString.substring(1, attributeString.length() - 1));
        this.fightScore = com.stars.util.MapUtil.getInt(map, "fightScore", 1);
        this.isFight = com.stars.util.MapUtil.getByte(map, "isFight", (byte) 0);
        BuddyStageVo buddyStageVo = BuddyManager.getBuddyStageVo(this.buddyId,this.stageLevel);
        if(buddyStageVo!=null){
            this.monsterId = buddyStageVo.getMonsterId();
        }
        this.armLevel = com.stars.util.MapUtil.getInt(map, "armLevel", 1);
    }

    public int getBuddyId() {
        return buddyId;
    }

    public void setBuddyId(int buddyId) {
        this.buddyId = buddyId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStageLevel() {
        return stageLevel;
    }

    public void setStageLevel(int stageLevel) {
        this.stageLevel = stageLevel;
    }

    public Attribute getBuddyAttr() {
        return buddyAttr;
    }

    public void setBuddyAttr(Attribute buddyAttr) {
        this.buddyAttr = buddyAttr;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public byte getIsFight() {
        return isFight;
    }

    public int getMonsterId() {
        return monsterId;
    }

    public int getArmLevel() {
        return armLevel;
    }

    public void setArmLevel(int armLevel) {
        this.armLevel = armLevel;
    }

    @Override
    public String toString(){
        Map<String, String> map = new HashMap<>();
        com.stars.util.MapUtil.setInt(map, "buddyId", buddyId);
        com.stars.util.MapUtil.setInt(map, "level", level);
        com.stars.util.MapUtil.setInt(map, "stageLevel", stageLevel);
        com.stars.util.MapUtil.setString(map, "buddyAttr", "{" + buddyAttr.getAttributeStr() + "}");
        com.stars.util.MapUtil.setInt(map, "fightScore", fightScore);
        com.stars.util.MapUtil.setByte(map, "isFight", isFight);
        MapUtil.setInt(map, "armLevel", armLevel);
        return StringUtil.makeString2(map, '=', ',');
    }
}
