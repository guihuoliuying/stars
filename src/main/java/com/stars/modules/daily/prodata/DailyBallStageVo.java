package com.stars.modules.daily.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.modules.daily.DailyManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/7/7.
 */
public class DailyBallStageVo {
    private int level; //等级
    private int stage; // 阶数
    private int star; //星数
    private String name; //斗魂珠名称
    private String icon; //斗魂珠图标
    private String iconFrame; //图标框特效
    private String effect; //魂珠特效
    private int reqScore; //需要消耗的斗魂值
    private String reqCost; //需要消耗的道具（货币）
    private String attr; //属性
    private int fightScore; //战力值

    //内存内容
    private Attribute attribute;
   private Map<Integer,Integer> costMap;

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(level);
        buff.writeInt(stage);
        buff.writeInt(star);
        buff.writeString(name);
        buff.writeString(icon);
        buff.writeString(iconFrame);
        buff.writeString(effect);
        buff.writeInt(reqScore);
        buff.writeInt(costMap.size());
        for(Map.Entry<Integer,Integer> entry:costMap.entrySet()){
            buff.writeInt(entry.getKey());
            buff.writeInt(entry.getValue());
        }
        buff.writeInt(DailyManager.getMaxDailyBallLevel());
        buff.writeInt(DailyManager.getDailyBallStageMaxStarMap().get(stage));
        attribute.writeToBuffer(buff);
        buff.writeInt(fightScore);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getReqScore() {
        return reqScore;
    }

    public void setReqScore(int reqScore) {
        this.reqScore = reqScore;
    }

    public String getReqCost() {
        return reqCost;
    }

    public void setReqCost(String reqCost) {
        costMap = new HashMap<>();
        this.reqCost = reqCost;
        if(StringUtil.isEmpty(reqCost) || reqCost.equals("0"))
            return;
        costMap = StringUtil.toMap(reqCost,Integer.class,Integer.class,'=','|');
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconFrame() {
        return iconFrame;
    }

    public void setIconFrame(String iconFrame) {
        this.iconFrame = iconFrame;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
        this.attribute = new Attribute(attr);
        this.fightScore = FormularUtils.calFightScore(attribute);
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public Map<Integer, Integer> getCostMap() {
        return costMap;
    }

    public void setCostMap(Map<Integer, Integer> costMap) {
        this.costMap = costMap;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }
}
