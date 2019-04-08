package com.stars.modules.newequipment.userdata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.newequipment.prodata.EquipmentVo;
import com.stars.modules.newequipment.prodata.TokenLevelVo;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.tool.userdata.ExtraAttrVo;
import com.stars.modules.tool.userdata.RoleTokenEquipmentHolePo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 角色装备表;
 * Created by wuyuxing on 2016/11/9
 */
public class RoleEquipment extends DbRow implements Cloneable {
    private long roleId;
    private byte type;              //装备类型, 123456对应六个装备位
    private int equipId;            //装备id, 也就是itemid, 装备的存贮走item表
    private int starLevel;          //装备星级
    private int strengthLevel;      //装备强化等级
    private Map<Byte,ExtraAttrVo> extraAttrMap; //附加属性,key:index value:attrVo
    private String tokenHoleStr; //符文装备上的符文孔信息
    private String tokenSkillStr; //符文装备上的符文技能信息
    private Map<Byte,RoleTokenEquipmentHolePo> roleTokenHoleInfoMap = new HashMap<>(); //装备符文孔信息 key:holeId(孔位) value：RoleTokenEquipmentHolePo
    private int tokenSkillId; //符文技能id
    private int tokenSKillLevel; //符文技能等级

    /***********************以下缓存变量不入库************************/
    private int strengthPercent;    //强化等级加成百分比
    private int strengthAttrAdd;    //强化固定加成
    private int starPercent;        //星级加成百分比
    private int fighting;           //装备战力
    private int basicFighting;      //基础属性战力
    private int equipLevel;         //装备等级
    private String washCost;        //洗练消耗货币
    private String switchCost;      //转移属性消耗货币
    private String starIcon;


    private int minExtAttrFighting;   //最小的额外属性的战力值
    private int extraAttrFighting;  //额外属性战力
    private Attribute baseAttr;     //装备基础属性
    private Attribute strengthAttr; //强化加成属性
    private Attribute extraAttr;    //额外属性
    private Attribute totalAttr;    //单件装备总属性:
                                    // 基础属性 = 原始基础属性*(1+强化等级加成百分比)*(1+星级加成百分比)
                                    // 附加属性 = 原始附加属性*(1+星级加成百分比)

    private boolean canOperate;   //标识符：能否进行操作.  用于一键强化/一键升星功能
    private boolean isActiveTokenSkill;

    public RoleEquipment() {
    }

    public RoleEquipment(long roleId, byte type, int equipId) {
        this.roleId = roleId;
        this.type = type;
        this.equipId = equipId;
    }

    public void writeToBuff(NewByteBuffer buffer) {
        buffer.writeByte(type);             //部位
        buffer.writeLong(roleId);
        buffer.writeInt(equipId);           //装备id & itemid
        if(equipId == 0) return;
        buffer.writeInt(strengthLevel);     //强化等级
        buffer.writeInt(starLevel);         //星级
        buffer.writeInt(strengthPercent);   //强化等级加成百分比
        buffer.writeInt(strengthAttrAdd);   //强化固定加成
        buffer.writeInt(starPercent);       //星级加成百分比
        buffer.writeString(StringUtil.isEmpty(starIcon)?"0":starIcon);
        buffer.writeInt(fighting);          //装备战力
        buffer.writeInt(equipLevel);        //装备等级
        buffer.writeString(washCost);       //洗练消耗货币
        buffer.writeString(switchCost);     //转移属性消耗货币
        baseAttr.writeToBuffer(buffer);     //基础属性

        //额外属性
        if(StringUtil.isEmpty(extraAttrMap)) {
            buffer.writeByte((byte) 0);
        }else{
            buffer.writeByte((byte) extraAttrMap.size());
            for(ExtraAttrVo extraAttrVo:extraAttrMap.values()){
                extraAttrVo.writeToBuffer(buffer);
            }
        }
        //符文信息
        if (isTokenEquip()) {//不是符文下面的
            buffer.writeByte((byte) 1); //是否符文技能
            Map<Integer,Integer> tokenWashCostMap = getWashTokenCost();
            int tokenWashCostSize = tokenWashCostMap.size();
            buffer.writeInt(tokenWashCostSize); //洗练符文材料
            for(Map.Entry<Integer,Integer> entry : tokenWashCostMap.entrySet()){
                buffer.writeInt(entry.getKey());
                buffer.writeInt(entry.getValue());
            }
            EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(equipId);
            int totalHoldCount = NewEquipmentManager.getTokenMaxNumIndex(equipmentVo.getTokenNumIndex());
            buffer.writeInt(totalHoldCount); //当前已开启装备符文孔
            for(byte i = 1; i<= totalHoldCount; i++){
                RoleTokenEquipmentHolePo holePo = roleTokenHoleInfoMap.get(i);
                if(holePo != null){
                    holePo.writeToBuffer(buffer);
                }else{
                    buffer.writeByte(i);
                    buffer.writeInt(0);
                    buffer.writeInt(0);
                }
            }

            buffer.writeInt(tokenSkillId); //符文技能id
            buffer.writeInt(tokenSKillLevel); //符文技能等级

            if(tokenSkillId != 0){
                int maxLevel = SkillManager.getMaxSkillLevel(tokenSkillId);
                buffer.writeInt(maxLevel);
            }else{
                buffer.writeInt(0);
            }
        }else{
            buffer.writeByte((byte)0);
        }

    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolenewequip", "`type`=" + type + " and `roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolenewequip", "`type`=" + type + " and `roleid`=" + roleId);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getEquipId() {
        return equipId;
    }
    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }

    public Map<Byte, ExtraAttrVo> getExtraAttrMap() {
        return extraAttrMap;
    }

    public void setExtraAttrMap(Map<Byte, ExtraAttrVo> extraAttrMap) {
        this.extraAttrMap = extraAttrMap;
    }

    public String getExtraAttrStr() {
        if(StringUtil.isEmpty(extraAttrMap)) return "";
        StringBuilder sb = new StringBuilder();
        for(ExtraAttrVo vo:extraAttrMap.values()){
            sb.append(vo.toString()).append("&");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }


    public void setExtraAttrStr(String extraAttrStr) {
        extraAttrMap = new HashMap<>();
        if(StringUtil.isEmpty(extraAttrStr)) return;
        String[] array = extraAttrStr.split("&");
        ExtraAttrVo vo;
        for(String tmp:array){
            vo = new ExtraAttrVo(tmp);
            extraAttrMap.put(vo.getIndex(),vo);
        }
    }


    public int getStarLevel() {
        return starLevel;
    }

    public void setStarLevel(int starLevel) {
        this.starLevel = starLevel;
    }

    public int getStrengthLevel() {
        return strengthLevel;
    }

    public void setStrengthLevel(int strengthLevel) {
        this.strengthLevel = strengthLevel;
    }

    public void addStrengthLevel(){
        this.strengthLevel ++;
    }

    public Attribute getBaseAttr() {
        return baseAttr;
    }

    public void setBaseAttr(Attribute baseAttr) {
        this.baseAttr = baseAttr;
        this.basicFighting = FormularUtils.calFightScore(baseAttr);
    }

    public Attribute getStrengthAttr() {
        return strengthAttr;
    }

    public void setStrengthAttr(Attribute strengthAttr) {
        this.strengthAttr = strengthAttr;
    }

    public Attribute getExtraAttr() {
        return extraAttr;
    }

    public void setExtraAttr(Attribute extraAttr) {
        this.extraAttr = extraAttr;
    }

    public Attribute getTotalAttr() {
        return totalAttr;
    }

    public void setTotalAttr(Attribute totalAttr) {
        this.totalAttr = totalAttr;
    }

    public boolean isCanOperate() {
        return canOperate;
    }

    public void setCanOperate(boolean canOperate) {
        this.canOperate = canOperate;
    }

    public void setStrengthPercent(int strengthPercent) {
        this.strengthPercent = strengthPercent;
    }

    public void setStarPercent(int starPercent) {
        this.starPercent = starPercent;
    }

    public void setFighting(int fighting) {
        this.fighting = fighting;
    }

    public int getFighting() {
        return fighting;
    }

    public void setEquipLevel(int equipLevel) {
        this.equipLevel = equipLevel;
    }

    public int getEquipLevel() {
        return equipLevel;
    }

    public void setStarIcon(String starIcon) {
        this.starIcon = starIcon;
    }

    public void addStarLevel(){
        this.starLevel++;
    }

    public void reduceStarLevel(){
        if(starLevel>=1) {
            this.starLevel--;
        }
    }

    public String gettokenHoleStr() {
        if (StringUtil.isEmpty(roleTokenHoleInfoMap))
            return "";
        StringBuffer sBuffer = new StringBuffer();
        for(RoleTokenEquipmentHolePo po:roleTokenHoleInfoMap.values()){
            sBuffer.append(po.toString()).append("&");
        }
        if (sBuffer.length() > 0) {
            sBuffer.deleteCharAt(sBuffer.length() - 1);
        }
        return sBuffer.toString();
    }

    public int getTokenSkillId() {
        return tokenSkillId;
    }

    public void setTokenSkillId(int tokenSkillId) {
        this.tokenSkillId = tokenSkillId;
    }

    public int getTokenSKillLevel() {
        return tokenSKillLevel;
    }

    public void setTokenSKillLevel(int tokenSKillLevel) {
        this.tokenSKillLevel = tokenSKillLevel;
    }

    public Map<Byte, RoleTokenEquipmentHolePo> getRoleTokenHoleInfoMap() {
        if (roleTokenHoleInfoMap == null)
            return new HashMap<>();
        return roleTokenHoleInfoMap;
    }

    public void setRoleTokenHoleInfoMap(Map<Byte, RoleTokenEquipmentHolePo> roleTokenHoleInfoMap) {
        this.roleTokenHoleInfoMap = roleTokenHoleInfoMap;
    }

    public void setTokenHoleStr(String tokenHoleStr) {
        this.tokenHoleStr = tokenHoleStr;
        roleTokenHoleInfoMap = new HashMap<>();
        if(StringUtil.isEmpty(tokenHoleStr)) {
            return;
        }
        String[] array = tokenHoleStr.split("&");
        RoleTokenEquipmentHolePo roleTokenEquipmentHolePo;
        for (String tokenHoleInfo: array){
            roleTokenEquipmentHolePo = new RoleTokenEquipmentHolePo(tokenHoleInfo);
            roleTokenHoleInfoMap.put(roleTokenEquipmentHolePo.getHoleId(),roleTokenEquipmentHolePo);
        }
    }

    public int getTokenEquipFight(){
        //符文等级加成
        Map<Byte,RoleTokenEquipmentHolePo> holeInfoMap = getRoleTokenHoleInfoMap();
        int tokenFight = 0;
        for(RoleTokenEquipmentHolePo holeInfo:holeInfoMap.values()){
            String key = holeInfo.getTokenId() + "_" + holeInfo.getTokenLevel();
            TokenLevelVo tokenLevelVo = NewEquipmentManager.getTokenLevelVo(key);
            if (tokenLevelVo != null){
                tokenFight += tokenLevelVo.getTokenFight();
            }
        }
        return tokenFight;
    }

    public String getTokenSkillStr() {
        if (tokenSkillId == 0)
            return "";
        StringBuffer sBuff = new StringBuffer();
        sBuff.append(tokenSkillId).append("=").append(tokenSKillLevel);
        return sBuff.toString();
    }

    public void setTokenSkillStr(String tokenSkillStr) {
        this.tokenSkillStr = tokenSkillStr;
        if (StringUtil.isEmpty(tokenSkillStr)) {
            return;
        }
        String[] array = tokenSkillStr.split("=");
        this.tokenSkillId = Integer.parseInt(array[0]);
        this.tokenSKillLevel = Integer.parseInt(array[1]);
    }

    public void setWashCost(String washCost) {
        this.washCost = washCost;
    }

    public void setSwitchCost(String switchCost) {
        this.switchCost = switchCost;
    }

    public int getBasicFighting() {
        return basicFighting;
    }

    public ExtraAttrVo getExtarAttrByIndex(byte index){
        if(StringUtil.isEmpty(extraAttrMap)){
            return null;
        }else{
            return extraAttrMap.get(index);
        }
    }

    public String makeString() {
        StringBuilder sb = new StringBuilder();
        sb.append(roleId).append(";").append(type).append(";").append(equipId).append(";");
        sb.append(starLevel).append(";").append(strengthLevel).append(";").append(getExtraAttrStr()).append(";");
        sb.append(tokenSkillId).append(";").append(tokenSKillLevel).append(";").append(gettokenHoleStr());
        return sb.toString();
    }

    public void parseString(String str){
        String[] strData = str.split(";");
        if(strData == null || strData.length < 5) return;
        roleId = Long.parseLong(strData[0]);
        type = Byte.parseByte(strData[1]);
        equipId = Integer.parseInt(strData[2]);
        starLevel = Integer.parseInt(strData[3]);
        strengthLevel = Integer.parseInt(strData[4]);
        if(strData.length==6) {
            setExtraAttrStr(strData[5]);
        }
        if(strData.length>6){
            setExtraAttrStr(strData[5]);
            tokenSkillId = Integer.parseInt(strData[6]);
            tokenSKillLevel = Integer.parseInt(strData[7]);
            if(strData.length>8) {
                setTokenHoleStr(strData[8]);
            }
        }
    }

    public int getMinExtAttrFighting(){
        return minExtAttrFighting;
    }

    public void setMinExtAttrFighting(int minExtAttrFighting) {
        this.minExtAttrFighting = minExtAttrFighting;
    }

    public int getExtraAttrFighting() {
        return extraAttrFighting;
    }

    public void setExtraAttrFighting(int extraAttrFighting) {
        this.extraAttrFighting = extraAttrFighting;
    }

    public int getStrengthPercent() {
        return strengthPercent;
    }

    public int getStarPercent() {
        return starPercent;
    }

    public int getStrengthAttrAdd() {
        return strengthAttrAdd;
    }

    public void setStrengthAttrAdd(int strengthAttrAdd) {
        this.strengthAttrAdd = strengthAttrAdd;
    }

    public RoleEquipment copy(){
        try {
            return (RoleEquipment) this.clone();
        }catch (CloneNotSupportedException e) {
            LogUtil.error("clone failed.", e);
        }
        return null;
    }

    public boolean isTokenEquip(){
        return NewEquipmentManager.isTokenEquipment(equipId);
    }

    public Map<Integer,Integer> getWashTokenCost(){
        return NewEquipmentManager.getEquipmentVo(equipId).getTokenWashCostMap();
    }


}
