package com.stars.modules.gem.userdata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.module.ModuleContext;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.gem.GemConstant;
import com.stars.modules.gem.GemManager;
import com.stars.modules.gem.GemModule;
import com.stars.modules.gem.prodata.GemHoleVo;
import com.stars.modules.gem.prodata.GemLevelVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色宝石表;
 * Created by panzhenfeng on 2016/7/25.
 */
public class RoleEquipmentGem extends DbRow {
    protected String TishenBaseName = "roleequipmentgem";
    private String roleId;
    private String type1;
    private String type2;
    private String type3;
    private String type4;
    private String type5;
    private String type6;
    protected ModuleContext moduleContext;
    protected GemModule gemModule;

    public  RoleEquipmentGem(){

    }
    public RoleEquipmentGem(String roleId_) {
        this.roleId = roleId_;
        String defaultValue;
        byte tmpIndex;
        for (byte i = 0; i < GemConstant.EQUIPMENT_MAX_COUNT; i++) {
            tmpIndex = (byte)(i+1);
            defaultValue = initPerTypeValue(tmpIndex);
            switch (tmpIndex) {
                case 1:
                    this.type1 = defaultValue;
                    break;
                case 2:
                    this.type2 = defaultValue;
                    break;
                case 3:
                    this.type3 = defaultValue;
                    break;
                case 4:
                    this.type4 = defaultValue;
                    break;
                case 5:
                    this.type5 = defaultValue;
                    break;
                case 6:
                    this.type6 = defaultValue;
                    break;
            }
        }
    }

    public RoleEquipmentGem(RoleEquipmentGem other) {
        this.roleId = other.roleId;
        this.type1 = other.type1;
        this.type2 = other.type2;
        this.type3 = other.type3;
        this.type4 = other.type4;
        this.type5 = other.type5;
        this.type6 = other.type6;
    }

    /**初始化空值,当新号对应装备位没有装备的时候进行设置;*/
    public String initPerTypeEmptyValue(int equipmentId_, byte equipmentType){
        return "0";
    }

    public void setModuleContext(ModuleContext context_, GemModule gemModule){
        this.moduleContext = context_;
        this.gemModule = gemModule;
    }

    public String initPerTypeValue(byte equipmentType_){
        int tmpCount = GemManager.getGemHoleCount(equipmentType_);
        StringBuilder sb = new StringBuilder();
        GemHoleVo gemHoleVo = null;
        for(byte i = 1 ; i<=tmpCount; i++){
            //TODO 这里之后还要判断玩家的当前vip类型;
            gemHoleVo = GemManager.getGemHoleVo(equipmentType_, i);
            sb.append("0");
            if(i+1<=tmpCount){
                sb.append("+");
            }
        }
        return sb.toString();
    }

    public String getEquipmentGemValue(byte equipmentType_, int gemHoleIndex_){
        if(gemHoleIndex_>=0){
            String str = getTypeFieldValue(equipmentType_);
            String[] strArr = str.split("\\+");
            return strArr[gemHoleIndex_];
        }else{
            return getTypeFieldValue(equipmentType_);
        }
    }

    public void setEquipmentGemValue(byte equipmentType_, int gemHoleIndex_, int gemLevelId_){
        String srcStr = getEquipmentGemValue(equipmentType_, -1);
        String[] gemArr = srcStr.split("\\+");
        String[] itemArr = gemArr[gemHoleIndex_].split(",");
        String newStr = "";
        if(gemLevelId_ < 0){
            gemLevelId_ = Integer.parseInt(itemArr[1]);
        }
        newStr = String.valueOf(gemLevelId_);
        StringBuffer tmpSb = new StringBuffer();
        //重新拼合字符串;
        for (int i = 0, len = gemArr.length; i<len; i++){
            if(i == gemHoleIndex_){
                tmpSb.append(newStr);
            }else{
                tmpSb.append(gemArr[i]);
            }
            if(i+1 < len){
                tmpSb.append("+");
            }
        }
        setTypeFieldValue(equipmentType_, tmpSb.toString());
    }

    public void checkGemLevel(byte equipmentType_){
        String srcStr = getEquipmentGemValue(equipmentType_, -1);
        String[] gemArr = srcStr.split("\\+");
        String newStr = "";
        StringBuffer tmpSb = new StringBuffer();
        int gemId;
        GemLevelVo gemLevelVo;
        //重新拼合字符串;
        for (int i = 0, len = gemArr.length; i<len; i++){
            gemId = Integer.parseInt(gemArr[i]);
            gemLevelVo = GemManager.getGemLevelVo(gemId);
//            if(gemLevelVo == null || gemLevelVo.getLevel() >= 6){
//                tmpSb.append(0);
//            }else{
                tmpSb.append(gemArr[i]);
//            }
            if(i+1 < len){
                tmpSb.append("+");
            }
        }
        setTypeFieldValue(equipmentType_, tmpSb.toString());
    }

    /**
     * 获取当前装备的镶嵌的宝石列表;
     * @param equipmentType
     * @return
     */
    public List<GemLevelVo> getEquipmentGemList(byte equipmentType){
        List<GemLevelVo> gemLevelVoList = new ArrayList<>();
        String srcStr = getEquipmentGemValue(equipmentType, -1);
        String[] gemArr = srcStr.split("\\+");
        String[] tmpArr ;
        int gemLevelId;
        GemLevelVo gemLevelVo;
        for(int i = 0, len = gemArr.length; i<len; i++){
            tmpArr = gemArr[i].split(",");
            if(tmpArr.length > 0){
                gemLevelId = Integer.parseInt(tmpArr[0]);
                gemLevelVo = GemManager.getGemLevelVo(gemLevelId);
                if(gemLevelVo != null){
                    gemLevelVoList.add(gemLevelVo);
                }
            }
        }
        return gemLevelVoList;
    }

    public List<GemLevelVo> getAllEquipmentGemList(){
        List<GemLevelVo> rtnList = new ArrayList<>();
        for (byte i = 1, len = GemConstant.EQUIPMENT_MAX_COUNT; i <= len; i++) {
            rtnList.addAll(getEquipmentGemList(i));
        }
        return rtnList;
    }

    public List<GemLevelVo> getAllEquipmentEmbedGemList(byte gemType){
        List<GemLevelVo> rtnlist = new ArrayList<>();
        List<GemLevelVo> allGemLevelVoList=  getAllEquipmentGemList();
        GemLevelVo gemLevelVo;
        for (int i = 0, len = allGemLevelVoList.size(); i<len; i++){
            gemLevelVo = allGemLevelVoList.get(i);
            if(gemLevelVo.getType() == gemType){
                rtnlist.add(gemLevelVo);
            }
        }
        return  rtnlist;
    }

    public Attribute getAttribute(byte equipmentType_){
        if(gemModule.isHasEquipment(equipmentType_)){
            String str = getEquipmentGemValue(equipmentType_, -1);
            String[] gemArr = str.split("\\+");
            String[] itemArr = null;
            int gemLevelId = 0;
            GemLevelVo gemLevelVo = null;
            Attribute attribute = new Attribute();
            for(int i = 0, len = gemArr.length; i<len; i++){
                itemArr = gemArr[i].split(",");
                gemLevelId = Integer.parseInt(itemArr[0]);
                if(gemLevelId>0){
                    gemLevelVo = GemManager.getGemLevelVo(gemLevelId);
                    if(gemLevelVo != null){
                        attribute.addAttribute(gemLevelVo.getAttributeattribute());
                    }
                }
            }
            return attribute;
        }
        return null;
    }

    public int getFightScore(){
        int totalScore = 0;
        for (byte i = 0; i< GemConstant.EQUIPMENT_MAX_COUNT; i++){
            totalScore += getFightScore(i);
        }
        return totalScore;
    }

    public int getFightScore(byte equipmentType_) {
        Attribute attribute = getAttribute(equipmentType_);
        if(attribute != null){
            return FormularUtils.calFightScore(attribute);
        }
        return 0;
    }


    public String getTypeFieldValue(byte type_) {
        switch (type_) {
            case 1:
                return type1;
            case 2:
                return type2;
            case 3:
                return type3;
            case 4:
                return type4;
            case 5:
                return type5;
            case 6:
                return type6;
        }
        return null;
    }

    public String setTypeFieldValue(byte type_, String value_) {
        switch (type_) {
            case 1:
                setType1(value_);
                break;
            case 2:
                setType2(value_);
                break;
            case 3:
                setType3(value_);
                break;
            case 4:
                setType4(value_);
                break;
            case 5:
                setType5(value_);
                break;
            case 6:
                setType6(value_);
                break;
        }
        if(moduleContext!=null){
            moduleContext.update(this);
        }
        return value_;
    }

    public void writeToBuff(NewByteBuffer buffer) {
        buffer.writeString(roleId);
        buffer.writeString(this.getType1());
        buffer.writeString(this.getType2());
        buffer.writeString(this.getType3());
        buffer.writeString(this.getType4());
        buffer.writeString(this.getType5());
        buffer.writeString(this.getType6());
    }

    public String makeString() {
        StringBuilder sb = new StringBuilder();
        sb.append(roleId).append(";");
        for(byte i = 1; i<=GemConstant.EQUIPMENT_MAX_COUNT; i++){
            sb.append(this.getTypeFieldValue(i));
            if(i+1 <= GemConstant.EQUIPMENT_MAX_COUNT){
                sb.append(";");
            }
        }
        return sb.toString();
    }

    public void parseString(String str){
        if(str == null) return;
        String[] strData = str.split(";");
        roleId = strData[0];
        int maxDataLength = strData.length;
        for (byte i = 1; i<=GemConstant.EQUIPMENT_MAX_COUNT; i++){
            if(i >= maxDataLength){
                LogUtil.error("解析宝石数据出错! 查看是否保存的数据格式有问题");
                break;
            }
            this.setTypeFieldValue(i, strData[i]);
        }
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, TishenBaseName, " roleid='" + this.getRoleId() + "'");
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql(TishenBaseName, " roleid='" + this.getRoleId() + "'");
    }


    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getType1() {
        return type1;
    }

    public void setType1(String type1) {
        this.type1 = type1;
    }

    public String getType2() {
        return type2;
    }

    public void setType2(String type2) {
        this.type2 = type2;
    }

    public String getType3() {
        return type3;
    }

    public void setType3(String type3) {
        this.type3 = type3;
    }

    public String getType4() {
        return type4;
    }

    public void setType4(String type4) {
        this.type4 = type4;
    }

    public String getType5() {
        return type5;
    }

    public void setType5(String type5) {
        this.type5 = type5;
    }

    public String getType6() {
        return type6;
    }

    public void setType6(String type6) {
        this.type6 = type6;
    }

}