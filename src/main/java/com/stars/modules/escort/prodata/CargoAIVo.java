package com.stars.modules.escort.prodata;

import com.stars.util.StringUtil;

import java.util.Map;
import java.util.Random;

/**
 * Created by wuyuxing on 2016/12/2.
 */
public class CargoAIVo {
    private int powerSection;           //匹配战力段    表示战力，用于分段
    private int minScoreSection;
    private String displayPower;        //显示战力
    private int minPower;               //显示最小战力
    private int maxPower;               //显示最大战力
    private int diffPower;              //战力差
    private byte carId;                 //镖车队列场景npc
    private int stageId;                //关卡id
    private String award;               //填itemid+数量，表示通关奖励
    private Map<Integer,Integer> awardMap;

    public int getPowerSection() {
        return powerSection;
    }

    public void setPowerSection(int powerSection) {
        this.powerSection = powerSection;
    }

    public boolean isInRange(int power){
        return power >= minScoreSection && power <= powerSection;
    }

    public int getDisplayRandomPower(){
        if(diffPower == 0) return minPower;
        Random random = new Random();
        int value = random.nextInt(diffPower);
        return minPower + value;
    }

    public String getDisplayPower() {
        return displayPower;
    }

    public void setDisplayPower(String displayPower) {
        this.displayPower = displayPower;
        String[] displayPowerStrData = displayPower.split("\\+");
        this.minPower = Integer.parseInt(displayPowerStrData[0]);
        this.maxPower = Integer.parseInt(displayPowerStrData[1]);
        this.diffPower = maxPower - minPower;
        if(diffPower < 0) diffPower = 0;
    }

    public byte getCarId() {
        return carId;
    }

    public void setCarId(byte carId) {
        this.carId = carId;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) throws Exception {
        this.award = award;
        this.awardMap = StringUtil.toMap(award, Integer.class, Integer.class, '+', '|');
    }

    public Map<Integer, Integer> getAwardMap() {
        return awardMap;
    }

    public void setMinScoreSection(int minScoreSection) {
        this.minScoreSection = minScoreSection;
    }
}
