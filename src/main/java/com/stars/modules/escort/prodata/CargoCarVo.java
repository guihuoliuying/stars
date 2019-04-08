package com.stars.modules.escort.prodata;

import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/2.
 */
public class CargoCarVo {
    private int carId;          //镖车唯一标识
    private byte carType;       //类型,1表示普通车，2表示高级车，3表示稀有车，4表示特级车
    private String name;        //镖车名字
    private String carImage;    //镖车图片路径
    private int npcId;          //镖车队列场景npc
    private int stageMonsterId; //运镖战斗场景怪物

    private String carAward;    //运镖奖励  填itemid+数量，多个 | 隔开
    private Map<Integer,Integer> carAwardMap;

    private String escortsAward;//护卫奖励  填itemid+数量，多个 | 隔开
    private Map<Integer,Integer> escortsAwardMap;

    private String robAward;    //劫镖奖励  填itemid+数量，多个 | 隔开
    private Map<Integer,Integer> robAwardMap;

    private int odds;           //权值
    private String levelSection;   //等级要求
    private int minLevel;
    private int maxLevel;
    private String vipSection;     //vip等级要求
    private int minVipLevel;
    private int maxVipLevel;

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCarImage() {
        return carImage;
    }

    public void setCarImage(String carImage) {
        this.carImage = carImage;
    }

    public int getNpcId() {
        return npcId;
    }

    public void setNpcId(int npcId) {
        this.npcId = npcId;
    }

    public int getStageMonsterId() {
        return stageMonsterId;
    }

    public void setStageMonsterId(int stageMonsterId) {
        this.stageMonsterId = stageMonsterId;
    }

    public String getCarAward() {
        return carAward;
    }

    public void setCarAward(String carAward) throws Exception {
        this.carAward = carAward;
        this.carAwardMap = StringUtil.toMap(carAward, Integer.class, Integer.class, '+', '|');
    }

    public Map<Integer, Integer> getCarAwardMap() {
        return carAwardMap;
    }

    public String getEscortsAward() {
        return escortsAward;
    }

    public void setEscortsAward(String escortsAward) throws Exception {
        this.escortsAward = escortsAward;
        this.escortsAwardMap = StringUtil.toMap(escortsAward, Integer.class, Integer.class, '+', '|');
    }

    public Map<Integer, Integer> getEscortsAwardMap() {
        return escortsAwardMap;
    }

    public String getRobAward() {
        return robAward;
    }

    public void setRobAward(String robAward) throws Exception {
        this.robAward = robAward;
        this.robAwardMap = StringUtil.toMap(robAward, Integer.class, Integer.class, '+', '|');
    }

    public Map<Integer, Integer> getRobAwardMap() {
        return robAwardMap;
    }

    public byte getCarType() {
        return carType;
    }

    public void setCarType(byte carType) {
        this.carType = carType;
    }

    public int getOdds() {
        return odds;
    }

    public void setOdds(int odds) {
        this.odds = odds;
    }

    public String getVipSection() {
        return vipSection;
    }

    public void setVipSection(String vipSection) {
        this.vipSection = vipSection;
        if(StringUtil.isEmpty(vipSection) || "0".equals(vipSection)) return;
        String[] arr = vipSection.split("\\+");
        this.minVipLevel = Integer.parseInt(arr[0]);
        this.maxVipLevel = Integer.parseInt(arr[1]);
    }

    public String getLevelSection() {
        return levelSection;
    }

    public void setLevelSection(String levelSection) {
        this.levelSection = levelSection;
        if(StringUtil.isEmpty(levelSection) || "0".equals(levelSection)) return;
        String[] arr = levelSection.split("\\+");
        this.minLevel = Integer.parseInt(arr[0]);
        this.maxLevel = Integer.parseInt(arr[1]);
    }

    public boolean isMatch(int level,int vipLevel){
        return isMatchLevel(level) && isMatchVipLevel(vipLevel);
    }

    public boolean isMatchLevel(int level){
        if(minLevel == 0 && maxLevel ==0) return true;
        return minLevel <= level && level <= maxLevel;
    }

    public boolean isMatchVipLevel(int vipLevel){
        if(minVipLevel == 0 && maxVipLevel ==0) return true;
        return minVipLevel <= vipLevel && vipLevel <= maxVipLevel;
    }
}
