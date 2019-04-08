package com.stars.modules.baby.prodata;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-25.
 */
public class BabySweepVo {
    private int id;
    private String levelSection;//等级区间段
    private int loopMark;//扫荡标记，用于六国寻宝
    private String award;//扫荡一次奖励
    private int reqStage;//所需宝宝阶段
    private int reqLevel;//所需宝宝等级
    private int sweepConsume;//扫荡一次所需宝宝的精力
    private String icon;//图标
    private String path;//窗口常量
    private int stage;//当前扫荡属于哪个阶段
    private String funcName;//功能名称
    private int type;//显示类型

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(id);
        buff.writeInt(reqStage);
        buff.writeInt(reqLevel);
        buff.writeInt(sweepConsume);
        buff.writeString(icon);
        buff.writeString(path);
        buff.writeInt(stage);
        buff.writeString(funcName);
    }

    private int minLv;
    private int maxLv;
    private Map<Integer, Integer> itemMap = new HashMap<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLevelSection() {
        return levelSection;
    }

    public void setLevelSection(String levelSection) {
        this.levelSection = levelSection;
        String[] tmp = levelSection.split("\\+");
        this.minLv = Integer.parseInt(tmp[0]);
        this.maxLv = Integer.parseInt(tmp[1]);
    }

    public int getLoopMark() {
        return loopMark;
    }

    public void setLoopMark(int loopMark) {
        this.loopMark = loopMark;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
        this.itemMap = StringUtil.toMap(award, Integer.class, Integer.class, '+', ',');
    }

    public int getReqStage() {
        return reqStage;
    }

    public void setReqStage(int reqStage) {
        this.reqStage = reqStage;
    }

    public int getReqLevel() {
        return reqLevel;
    }

    public void setReqLevel(int reqLevel) {
        this.reqLevel = reqLevel;
    }

    public int getSweepConsume() {
        return sweepConsume;
    }

    public void setSweepConsume(int sweepConsume) {
        this.sweepConsume = sweepConsume;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public boolean matchLv(int level) {
        return level >= minLv && level <= maxLv;
    }

    public int getMinLv() {
        return minLv;
    }

    public int getMaxLv() {
        return maxLv;
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
