package com.stars.modules.newequipment.summary;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.newequipment.prodata.EquipStarVo;
import com.stars.modules.newequipment.prodata.EquipStrengthVo;
import com.stars.modules.newequipment.prodata.EquipmentVo;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.tool.userdata.ExtraAttrVo;
import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.services.summary.SummaryConst;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/8.
 */
public class NewEquipmentSummaryComponentImpl extends AbstractSummaryComponent implements NewEquipmentSummaryComponent {

    private Map<Byte, RoleEquipment> roleEquipMap;
    private List<String> dragonBallList = new ArrayList<>();

    public NewEquipmentSummaryComponentImpl() {
    }

    public NewEquipmentSummaryComponentImpl(Map<Byte, RoleEquipment> roleEquipMap,List<String> dragonBallList) {
        this.roleEquipMap = roleEquipMap;
        this.dragonBallList = dragonBallList;
    }

    @Override
    public String getName() {
        return SummaryConst.C_NEW_EQUIPMENT;
    }

    @Override
    public int getLatestVersion() {
        return 1;
    }

    @Override
    public void fromString(int version, String str) {
        try {
            switch (version) {
                case 1:
                    parseVer1(str);
                    break;
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    @Override
    public String makeString() {
        Map<Byte, String> tmp = new HashMap<>();
        for (Map.Entry<Byte, RoleEquipment> entry : roleEquipMap.entrySet()) {
            tmp.put(entry.getKey(), entry.getValue().makeString());
        }

        StringBuffer sb = new StringBuffer(StringUtil.makeString2(tmp, '=', '#'));
        if(StringUtil.isNotEmpty(dragonBallList)){
            sb.deleteCharAt(sb.length()-1);
            sb.append("^");
            for(String dragonBallId:dragonBallList){
                sb.append(dragonBallId).append(",");
            }
            if(sb.length() == sb.lastIndexOf(",")+1){
                sb.deleteCharAt(sb.length()-1);
            }
            sb.append("}");
        }

        return sb.toString();
    }

    @Override
    public Map<Byte,RoleEquipment> getEquipmentMap() {
        return roleEquipMap;
    }

    private void parseVer1(String str) throws Exception {
        roleEquipMap = new HashMap<>();

        String[] array = str.split("\\^");
        String str1;
        if(array.length == 2) {
            str1 = array[0] + "}";
        }else{
            str1 = str;
        }
        RoleEquipment equipmentData;
        Map<Byte, String> tmp = StringUtil.toMap(str1, Byte.class, String.class, '=', '#');
        for (Map.Entry<Byte, String> entry : tmp.entrySet()) {
            equipmentData = new RoleEquipment();
            equipmentData.parseString(entry.getValue());
            updateRoleEquipmentAttr(equipmentData);
            roleEquipMap.put(entry.getKey(), equipmentData);
        }

        if(array.length ==2 ){
            if(StringUtil.isEmpty(array[1])) return;
            String str2 = array[1].replaceAll("}","");
            String[] dragonBall = str2.split(",");
            for(String dragonBallId: dragonBall){
                dragonBallList.add(dragonBallId);
            }
        }
    }

    /**
     * 更新单件装备属性
     */
    private void updateRoleEquipmentAttr(RoleEquipment roleEquipment){
        if(roleEquipment == null || roleEquipment.getEquipId() == 0) return; //没有穿上装备
        EquipmentVo equipment = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
        if(equipment == null) return;
        //基础属性
        roleEquipment.setBaseAttr(equipment.getAttributePacked());
        roleEquipment.setEquipLevel(equipment.getEquipLevel());
        roleEquipment.setWashCost(equipment.getWashCost());
        roleEquipment.setSwitchCost(equipment.getSwitchCost());

        //强化属性
        roleEquipment.setStrengthPercent(0);
        roleEquipment.setStrengthAttrAdd(0);
        EquipStrengthVo equipStrengthVo = NewEquipmentManager.getEquipStrengthVo(roleEquipment);
        if(equipStrengthVo != null &&
                (equipStrengthVo.getAttrPencent() > 0 || equipStrengthVo.getAttrAdd() > 0)) {
            Attribute strengthAttr = new Attribute();
            strengthAttr.addAttribute(equipment.getAttributePacked(), equipStrengthVo.getAttrPencent(), 100);
            strengthAttr.addSingleAttr(equipment.getAttributePacked().getFirstNotZeroAttrIndex(), equipStrengthVo.getAttrAdd());
            roleEquipment.setStrengthAttr(strengthAttr);
            roleEquipment.setStrengthPercent(equipStrengthVo.getAttrPencent());
            roleEquipment.setStrengthAttrAdd(equipStrengthVo.getAttrAdd());
        }

        //额外属性
        if(StringUtil.isNotEmpty(roleEquipment.getExtraAttrMap())){
            Attribute extraAttr = new Attribute();
            int minExtraAttrFighting = 0;
            for(ExtraAttrVo extraAttrVo:roleEquipment.getExtraAttrMap().values()){
                extraAttr.addSingleAttr(extraAttrVo.getAttrName(),extraAttrVo.getAttrValue());

                if(minExtraAttrFighting == 0 || minExtraAttrFighting > extraAttrVo.getFighting()){
                    minExtraAttrFighting = extraAttrVo.getFighting();
                }
            }
            roleEquipment.setExtraAttr(extraAttr);
            roleEquipment.setMinExtAttrFighting(minExtraAttrFighting);
            roleEquipment.setExtraAttrFighting(FormularUtils.calFightScore(extraAttr));
        }

        Attribute totalAttr = new Attribute();
        totalAttr.addAttribute(roleEquipment.getBaseAttr());      //基础属性
        totalAttr.addAttribute(roleEquipment.getStrengthAttr());  //强化属性
        totalAttr.addAttribute(roleEquipment.getExtraAttr());     //额外属性

        //升星属性加成
        roleEquipment.setStarPercent(0);
        EquipStarVo equipStarVo = NewEquipmentManager.getEquipStarVo(roleEquipment);
        if(equipStarVo != null && equipStarVo.getEnhanceAttr() > 0){
            Attribute tmpAttr = new Attribute();
            tmpAttr.addAttribute(totalAttr, 100 + equipStarVo.getEnhanceAttr(), 100);
            totalAttr = tmpAttr;
            roleEquipment.setStarPercent(equipStarVo.getEnhanceAttr());
            roleEquipment.setStarIcon(equipStarVo.getStarShow());
        }

        //符文加战力
        int tokenEquipFightAdd = roleEquipment.getTokenEquipFight();

        //单件装备全部属性
        roleEquipment.setTotalAttr(totalAttr);
        roleEquipment.setFighting(FormularUtils.calFightScore(totalAttr) + tokenEquipFightAdd);
    }

    public RoleEquipment getEquipInfoByType(Byte type){
        if(roleEquipMap == null) return null;
        return roleEquipMap.get(type);
    }

    public List<String> getDragonBallList() {
        return dragonBallList;
    }

    public void setDragonBallList(List<String> dragonBallList) {
        this.dragonBallList = dragonBallList;
    }
}
