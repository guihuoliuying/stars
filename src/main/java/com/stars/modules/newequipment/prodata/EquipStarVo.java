package com.stars.modules.newequipment.prodata;

import com.stars.util.StringUtil;

import java.util.Map;

/**
 * 装备升星VO;
 * Created by wuyuxing on 2016/11/8.
 */
public class EquipStarVo implements Comparable{
    private int level;          //装备星级
    private byte type;          //装备类型
    private String sucRate;     //显示用的成功率+实际成功率,百分数
    private int realSuccess;    //实际成功率
    private int displaySuccess; //展示成功率
    private int enhanceAttr;    //属性加成的百分比
    private String material;    //itemid+数量, itemid+数量, 表示由前一星级升级到当前星级需要消耗的材料. 比如 1+10,2+50
    private Map<Integer,Integer> materialMap;
    private String luckItem;    //itemid+数量, 如果升星时消耗该材料, 在升星失败的情况下星级不会掉
    private Map<Integer,Integer> luckyItemMap;
    private String starShow;    //显示的星星数量, 格式为: 图片编号+数量, 图片编号+数量. 配0代表不显示任何图片,数组最少为1, 做多为2
    private int levelLimit;     //提升到当前星级需要的玩家战力


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) throws Exception {
        this.material = material;
        this.materialMap = StringUtil.toMap(material, Integer.class, Integer.class, '+', ',');
    }

    public int getLevelLimit() {
        return levelLimit;
    }

    public void setLevelLimit(int levelLimit) {
        this.levelLimit = levelLimit;
    }

    public String getSucRate() {
        return sucRate;
    }

    public void setSucRate(String sucRate) {
        this.sucRate = sucRate;
        if(StringUtil.isNotEmpty(sucRate)){
            String[] strData = sucRate.split("\\+");
            this.displaySuccess = Integer.parseInt(strData[0]);
            this.realSuccess = Integer.parseInt(strData[1]);
        }
    }

    public int getEnhanceAttr() {
        return enhanceAttr;
    }

    public void setEnhanceAttr(int enhanceAttr) {
        this.enhanceAttr = enhanceAttr;
    }

    public String getLuckItem() {
        return luckItem;
    }

    public void setLuckItem(String luckItem) throws Exception {
        this.luckItem = luckItem;
        this.luckyItemMap = StringUtil.toMap(luckItem, Integer.class, Integer.class, '+', ',');
    }

    public String getStarShow() {
        return starShow;
    }

    public void setStarShow(String starShow) {
        this.starShow = starShow;
    }

    public Map<Integer, Integer> getMaterialMap() {
        return materialMap;
    }

    public Map<Integer, Integer> getLuckyItemMap() {
        return luckyItemMap;
    }

    public int getRealSuccess() {
        return realSuccess;
    }

    public int getDisplaySuccess() {
        return displaySuccess;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof EquipStarVo){
            EquipStarVo tmpVo = (EquipStarVo)o;
            if(tmpVo.level > this.level){
                return -1;
            }else if(tmpVo.level == this.level){
                return  0;
            }
        }
        return  1;
    }
}
