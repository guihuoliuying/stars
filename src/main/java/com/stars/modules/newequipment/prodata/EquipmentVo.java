package com.stars.modules.newequipment.prodata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 装备产品数据
 * Created by wuyuxing on 2016/11/8.
 */
public class EquipmentVo {
    private int equipId;             //装备id, 也就是itemid, 装备的存贮走item表
    private byte type;          //装备类型, 123456对应六个装备位
    private byte job;           //职业id, 配0代表全职业通用
    private String originAttr;   //装备基础属性
    private short equipLevel;    //装备等级
    private byte extAttrNum;    //初始附加属性的条目数量
    private int effectPlay;    //获取时是否播放提示特效, 0代表不播放任何特效, 1代表只播放特效, 如果是＞1, 代表需要播放特效, 并且播放特效的同时触发该id的引导
    private String effectDesc;  //特效描述, 只有在effectplay≠0的情况下有效
    private String washCost;    //洗练消耗货币, 格式为 物品id+数量, 固定一种
    private Map<Integer, Integer> washMap;
    private String switchCost;  //转移属性消耗货币, 格式为itemid+数量, 固定一种
    private Map<Integer, Integer> switchMap;
    private String produceAdd;  //通关产出加成
    private byte isTokenEquip; //是否符文装备
    private int tokenNumIndex; //符文数量索引
    private String tokenWashCost; //符文装备洗练材料
    private int levelDown;//可以降多少级穿戴
    private Map<Integer, Integer> tokenWashCostMap;
    private EquipmentProduceAdd equipmentProduceAdd = new EquipmentProduceAdd();
    private Integer changeMap;//转职映射
    // 属性对象
    private Attribute attributePacked = new Attribute();
    private int basicFighting;  //基础属性战力

    public Attribute getAttributePacked() {
        return attributePacked;
    }


    public int getEquipId() {
        return equipId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getOriginAttr() {
        return originAttr;
    }

    public void setOriginAttr(String originAttr) {
        this.originAttr = originAttr;
        attributePacked.strToAttribute(originAttr);
        basicFighting = FormularUtils.calFightScore(attributePacked);
    }

    public byte getJob() {
        return job;
    }

    public void setJob(byte job) {
        this.job = job;
    }

    public short getEquipLevel() {
        return equipLevel;
    }

    /**
     * 穿戴等级
     * @return
     */
    public short getPutOnLevel() {
        return (short) (equipLevel - levelDown);
    }

    public void setEquipLevel(short equipLevel) {
        this.equipLevel = equipLevel;
    }

    public byte getExtAttrNum() {
        return extAttrNum;
    }

    public void setExtAttrNum(byte extAttrNum) {
        this.extAttrNum = extAttrNum;
    }

    public int getEffectPlay() {
        return effectPlay;
    }

    public void setEffectPlay(int effectPlay) {
        this.effectPlay = effectPlay;
    }

    public String getEffectDesc() {
        return effectDesc;
    }

    public void setEffectDesc(String effectDesc) {
        this.effectDesc = effectDesc;
    }

    public String getWashCost() {
        return washCost;
    }

    public void setWashCost(String washCost) throws Exception {
        this.washCost = washCost;
        this.washMap = StringUtil.toMap(washCost, Integer.class, Integer.class, '+', ',');
    }

    public String getSwitchCost() {
        return switchCost;
    }

    public void setSwitchCost(String switchCost) throws Exception {
        this.switchCost = switchCost;
        this.switchMap = StringUtil.toMap(switchCost, Integer.class, Integer.class, '+', ',');
    }

    public String getProduceAdd() {
        return produceAdd;
    }

    public void setProduceAdd(String produceAdd) {
        this.produceAdd = produceAdd;

        if (produceAdd == null || produceAdd.equals("") || produceAdd.equals("0")) {
            return;
        }

        String[] sts = produceAdd.split("\\,");

        if (sts.length < 3) {
            return;
        }

        //set targetIds
        String[] competitionStrs = sts[0].split("\\+");
        List<Integer> tempTargetIds = new ArrayList<Integer>();
        for (String tmp : competitionStrs) {
            tempTargetIds.add(Integer.parseInt(tmp));
        }
        equipmentProduceAdd.targetIds = tempTargetIds;

        //set itemId
        equipmentProduceAdd.itemId = Integer.parseInt(sts[1]);

        //set percent
        equipmentProduceAdd.percent = Integer.parseInt(sts[2]);
    }

    public byte getIsTokenEquip() {
        return isTokenEquip;
    }

    public void setIsTokenEquip(byte isTokenEquip) {
        this.isTokenEquip = isTokenEquip;
    }

    public int getTokenNumIndex() {
        return tokenNumIndex;
    }

    public void setTokenNumIndex(int tokenNumIndex) {
        this.tokenNumIndex = tokenNumIndex;
    }

    public String getTokenWashCost() {
        return tokenWashCost;
    }

    public void setTokenWashCost(String tokenWashCost) {
        this.tokenWashCost = tokenWashCost;
        this.tokenWashCostMap = new HashMap<>();
        if (StringUtil.isNotEmpty(tokenWashCost) && !tokenWashCost.equals("0")) {
            tokenWashCostMap = StringUtil.toMap(tokenWashCost, Integer.class, Integer.class, '+', ',');

        }
    }

    public Map<Integer, Integer> getTokenWashCostMap() {
        return tokenWashCostMap;
    }

    public Map<Integer, Integer> getWashMap() {
        return washMap;
    }

    public Map<Integer, Integer> getSwitchMap() {
        return switchMap;
    }

    public int getBasicFighting() {
        return basicFighting;
    }

    public EquipmentProduceAdd getEquipmentProduceAdd() {
        return equipmentProduceAdd;
    }

    public Integer getColor() {
        ItemVo itemVo = ToolManager.getItemVo(equipId);
        return (int) itemVo.getColor();
    }

    public Integer getChangeMap() {
        return changeMap;
    }

    public void setChangeMap(Integer changeMap) {
        this.changeMap = changeMap;
    }

    public int getLevelDown() {
        return levelDown;
    }

    public void setLevelDown(int levelDown) {
        this.levelDown = levelDown;
    }
}
