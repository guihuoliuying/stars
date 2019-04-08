package com.stars.modules.newequipment.prodata;

import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by wuyuxing on 2016/11/8.
 */
public class ExtEquipAttrVo {
    private int extAttrId;                 //附加属性id
    private byte type;              //装备类型, 配置1~6
    private byte quality;           //装备品质, 配置1~5
    private short level;             //装备等级

    private String randomQualityStr;//装备可以随机出的品质id和权重, 格式为 id1+权重1,id2+权重2
    private Map<Byte,Integer> qualityWeightMap;//key:quality value:weight
    private int qualityTotalWeight;//品质总权重

    private String attrMax;      //装备可以随机的属性, 格式为 属性名称1+满值+权重,属性名称2+满值+权重2
    private List<ExtAttrWeightVo> weightList;
    private int attrTotalWeight;    //属性值总权重

    public int getExtAttrId() {
        return extAttrId;
    }

    public void setExtAttrId(int extAttrId) {
        this.extAttrId = extAttrId;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getQuality() {
        return quality;
    }

    public void setQuality(byte quality) {
        this.quality = quality;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public String getRandomQualityStr() {
        return randomQualityStr;
    }

    public void setRandomQualityStr(String randomQualityStr) throws Exception {
        this.randomQualityStr = randomQualityStr;
        if(StringUtil.isEmpty(randomQualityStr)) return;
        qualityWeightMap = StringUtil.toMap(randomQualityStr,Byte.class,Integer.class,'+',',');
        qualityTotalWeight = 0;
        for(Integer value:qualityWeightMap.values()){
            qualityTotalWeight += value;
        }
    }

    public String getAttrMax() {
        return attrMax;
    }

    public void setAttrMax(String attrMax) {
        this.attrMax = attrMax;
        if(StringUtil.isEmpty(attrMax)) return;
        weightList = new ArrayList<>();
        attrTotalWeight = 0;
        String[] array = attrMax.split(",");
        ExtAttrWeightVo weightVo;
        for(String tmp:array){
            weightVo = new ExtAttrWeightVo(tmp);
            weightList.add(weightVo);
            attrTotalWeight += weightVo.getWeight();
        }
    }

    /**
     * 按权重随机获得属性满值
     */
    public ExtAttrWeightVo getRandomWeightVo(){
        if(StringUtil.isEmpty(weightList)) return null;
        Random random = new Random();
        int value = random.nextInt(attrTotalWeight);
        int curValue = 0;
        for(ExtAttrWeightVo vo:weightList){
            curValue += vo.getWeight();
            if(curValue >= value) return vo;
        }
        return weightList.get(0);
    }

    /**
     * 按权重随机获得额外属性的品质
     */
    public Byte getRandomQuality(){
        if(StringUtil.isEmpty(qualityWeightMap)) return 0;
        Random random = new Random();
        int value = random.nextInt(qualityTotalWeight);
        int curValue = 2;
        for(Map.Entry<Byte,Integer> entry:qualityWeightMap.entrySet()){
            curValue += entry.getValue();
            if(curValue >= value) return entry.getKey();
        }
        return 2;
    }

    public String getKey(){
        return type+"_"+level+"_"+quality;
    }

    public ExtAttrWeightVo getExtAttrWeightVoByAttrName(String attrName){
        if(StringUtil.isEmpty(weightList)) return null;
        for(ExtAttrWeightVo vo:weightList){
            if(vo.getAttrName().equals(attrName)) return vo;
        }
        return null;
    }
}
