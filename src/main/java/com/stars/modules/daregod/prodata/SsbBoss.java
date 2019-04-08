package com.stars.modules.daregod.prodata;

import com.stars.modules.daregod.DareGodManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-08-23.
 */
public class SsbBoss {
    private int fightingType;
    private int stageId;
    private int fightingMin;
    private int fightingMax;
    private int showAward;
    private String sectionName;
    private String shape;
    private int hurtAward;
    private String targetList;
    private int plat;
    private int stageMonsterId;
    private int shapeSize;

    public void writeToBuff(NewByteBuffer buffer) {
        buffer.writeInt(fightingType);//战力段
        buffer.writeInt(fightingMin);//最小战力
        buffer.writeInt(fightingMax);//最大战力
        buffer.writeInt(showAward);//显示的奖励
        buffer.writeString(sectionName);//该战力段的名称
        buffer.writeString(shape);//图标
        buffer.writeInt(shapeSize);//大小
    }

    private Set<Integer> damageTargetSet = new HashSet<>();
    private Map<Integer, Integer> targetDropGroupMap = new HashMap<>();

//    private Map<Long, Integer> damageDropMap = new HashMap<>();

    public int getFightingType() {
        return fightingType;
    }

    public void setFightingType(int fightingType) {
        this.fightingType = fightingType;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public int getFightingMin() {
        return fightingMin;
    }

    public void setFightingMin(int fightingMin) {
        this.fightingMin = fightingMin;
    }

    public int getFightingMax() {
        return fightingMax;
    }

    public void setFightingMax(int fightingMax) {
        this.fightingMax = fightingMax;
    }

    public int getShowAward() {
        return showAward;
    }

    public void setShowAward(int showAward) {
        this.showAward = showAward;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public int getHurtAward() {
        return hurtAward;
    }

    public void setHurtAward(int hurtAward) {
        this.hurtAward = hurtAward;
    }

    public String getTargetList() {
        return targetList;
    }

    public void setTargetList(String targetList) {
        this.targetList = targetList;
//        this.damageDropMap = StringUtil.toMap(targetList, Long.class, Integer.class, '=', '&');
        try {
            this.damageTargetSet = StringUtil.toHashSet(targetList, Integer.class, '&');
            for (int targetId : damageTargetSet) {
                SsbBossTarget bossTarget = DareGodManager.ssbBossTargetMap.get(targetId);
                if (bossTarget == null)
                    throw new IllegalArgumentException("挑战女神数据错误|target=" + targetId + " 不存在");
                targetDropGroupMap.put(targetId, bossTarget.getAward());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPlat() {
        return plat;
    }

    public void setPlat(int plat) {
        this.plat = plat;
    }

    public boolean matchFightScore(int fightScore) {
        return fightScore >= fightingMin && fightScore <= fightingMax;
    }

    public Set<Integer> getDamageTargetSet() {
        return damageTargetSet;
    }

    public Map<Integer, Integer> getTargetDropGroupMap() {
        return targetDropGroupMap;
    }


    public Set<Integer> getDropIds(Set<Integer> targetSet) {
        Set<Integer> tmpSet = new HashSet<>();
        for (int targetId : targetSet) {
            if (damageTargetSet.contains(targetId)) {
                tmpSet.add(targetDropGroupMap.get(targetId));
            }
        }
        return tmpSet;
    }

    public int getStageMonsterId() {
        return stageMonsterId;
    }

    public void setStageMonsterId(int stageMonsterId) {
        this.stageMonsterId = stageMonsterId;
    }

    public int getShapeSize() {
        return shapeSize;
    }

    public void setShapeSize(int shapeSize) {
        this.shapeSize = shapeSize;
    }
}
