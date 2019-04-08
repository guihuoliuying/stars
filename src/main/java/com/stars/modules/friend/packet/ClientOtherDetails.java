package com.stars.modules.friend.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyManager;
import com.stars.modules.buddy.prodata.BuddyinfoVo;
import com.stars.modules.buddy.summary.BuddySummaryComponent;
import com.stars.modules.buddy.summary.BuddySummaryVo;
import com.stars.modules.deityweapon.summary.DeityWeaponSummaryComponent;
import com.stars.modules.family.summary.FamilySummaryComponent;
import com.stars.modules.fashion.summary.FashionSummaryComponent;
import com.stars.modules.fashion.userdata.RoleFashion;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.modules.newequipment.summary.NewEquipmentSummaryComponent;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.skill.summary.SkillSummaryComponent;
import com.stars.modules.title.TitleManager;
import com.stars.modules.title.prodata.TitleVo;
import com.stars.modules.title.summary.TitleSummaryComponent;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by zhaowenshuo on 2016/8/16.
 */
public class ClientOtherDetails extends PlayerPacket {

    private Summary selfSummary;
    private Summary otherSummary;
    private byte isFriend;
    private byte isFriendOpen;

    public ClientOtherDetails() {
    }

    public ClientOtherDetails(Summary selfSummary, Summary otherSummary, boolean isFriend, boolean isFriendOpen) {
        this.selfSummary = selfSummary;
        this.otherSummary = otherSummary;
        this.isFriend = isFriend ? (byte) 1 : (byte) 0;
        this.isFriendOpen = isFriendOpen ? (byte) 1 : (byte) 0;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FriendPacketSet.C_OTHER_DETAILS;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(isFriend);
        buff.writeByte(isFriendOpen);

        // 先写自己
        writeBaseInfo(buff, selfSummary); // 基础数据
        writeAttribute(buff, selfSummary); // 属性
        writeDeityWeapon(buff, selfSummary);//神兵
        writeFightScore(buff, selfSummary); // 战力构成
        writeTitle(buff, selfSummary); // 称号
        writeEquipment(buff, selfSummary); // 装备
        writeFamily(buff, selfSummary); // 家族
        writeBuddy(buff, selfSummary); // 伙伴
        writeSkill(buff, selfSummary); // 技能
        writeFamliySkill(buff, selfSummary); // 家族心法
        // 再写别人
        writeBaseInfo(buff, otherSummary); // 基础数据
        writeAttribute(buff, otherSummary); // 属性
        writeDeityWeapon(buff, otherSummary); //神兵
        writeFightScore(buff, otherSummary); // 战力构成
        writeTitle(buff, otherSummary); // 称号
        writeEquipment(buff, otherSummary); // 装备
        writeFamily(buff, otherSummary); // 家族
        writeBuddy(buff, otherSummary); // 伙伴
        writeSkill(buff, otherSummary); // 技能
        writeFamliySkill(buff, otherSummary); // 家族心法
    }

    // 基础数据
    private void writeBaseInfo(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {
        RoleSummaryComponent component = (RoleSummaryComponent) summary.getComponent(MConst.Role);
        buff.writeString(Long.toString(summary.getRoleId())); // roleId
        buff.writeString(component.getRoleName()); // 名字
        buff.writeInt(component.getRoleLevel()); // 等级
        buff.writeInt(component.getRoleJob()); // job id
        buff.writeInt(component.getFightScore()); // 总战力
        buff.writeInt(component.getTitleId()); // 当前使用称号
    }

    // 属性
    private void writeAttribute(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {
        RoleSummaryComponent component = (RoleSummaryComponent) summary.getComponent(MConst.Role);
        component.getTotalAttr().writeToBuffer(buff); // 属性
    }

    // 战力构成
    private void writeFightScore(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {
        RoleSummaryComponent component = (RoleSummaryComponent) summary.getComponent(MConst.Role);
        Map<String, Integer> fightScoreMap = component.getFightScoreMap();
        buff.writeByte((byte) fightScoreMap.size()); // 战力构成表的大小
        for (Map.Entry<String, Integer> entry : fightScoreMap.entrySet()) {
            buff.writeString(entry.getKey()); // 构成部分的名称
            buff.writeInt(entry.getValue()); // 构成部分的战力
        }
    }

    // 称号
    private void writeTitle(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {
        TitleSummaryComponent component = (TitleSummaryComponent) summary.getComponent("title");
        Map<Byte, Integer> topFightScoreTitleMap = component.getTopFightScoreTitleMap();
        Iterator<Entry<Byte, Integer>> iterator = topFightScoreTitleMap.entrySet().iterator();
        Entry<Byte, Integer> checkEntry = null;
        for (;iterator.hasNext();) {
        	checkEntry = iterator.next();
        	TitleVo vo = TitleManager.getTitleVo(checkEntry.getValue());
        	if(vo==null){
        		if(checkEntry.getValue()!=null){
        			LogUtil.error("Title Missing:"+checkEntry.getValue());
        		}
        		iterator.remove();
        	}
        }
        buff.writeByte((byte) topFightScoreTitleMap.size()); // 大小
        for (Map.Entry<Byte, Integer> entry : topFightScoreTitleMap.entrySet()) {
            TitleVo vo = TitleManager.getTitleVo(entry.getValue());
            buff.writeByte(entry.getKey()); // 称号的类别
            buff.writeInt(entry.getValue()); // 称号的id
            buff.writeInt(vo.getFightPower()); // 称号战力
        }
    }

    // 装备
    private void writeEquipment(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {

        //时装加到装备孔位8的处理
        FashionSummaryComponent fashionComponent = (FashionSummaryComponent) summary.getComponent(MConst.Fashion);
        RoleFashion roleFashion = null;
        byte hasFashionData = (byte)0;
        if (fashionComponent != null && fashionComponent.getRoleDressFashion() != null){
            roleFashion = fashionComponent.getRoleDressFashion();
            hasFashionData = (byte)1;
        }

        NewEquipmentSummaryComponent component = (NewEquipmentSummaryComponent) summary.getComponent(SummaryConst.C_NEW_EQUIPMENT);
        if (component == null || StringUtil.isEmpty(component.getEquipmentMap())) {
            buff.writeByte((byte) hasFashionData);
        } else {
            buff.writeByte((byte) (component.getEquipmentMap().size()+hasFashionData));
            for (RoleEquipment equipment : component.getEquipmentMap().values()) {
                equipment.writeToBuff(buff);
            }
        }
        if (roleFashion != null) {
            roleFashion.writeDressedFashionToBuff(buff);
        }
    }

    private void writeFamily(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {
        FamilySummaryComponent comp = (FamilySummaryComponent) summary.getComponent(SummaryConst.C_FAMILY);
        buff.writeString(Long.toString(comp.getFamilyId())); // 家族id
        buff.writeString(comp.getFamilyName()); // 家族名
        buff.writeByte(comp.getPostId()); // 家族职位
    }

    private void writeBuddy(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {
        BuddySummaryComponent comp = (BuddySummaryComponent) summary.getComponent(SummaryConst.C_BUDDY);
        Map<Integer, BuddySummaryVo> voMap = comp.getBuddySummaryVoMap();
        buff.writeByte((byte) voMap.size());
        BuddyinfoVo buddyinfoVo;
        for (BuddySummaryVo vo : voMap.values()) {
            buff.writeInt(vo.getBuddyId());
            buff.writeInt(vo.getLevel());
            buff.writeInt(vo.getStageLevel());
            buff.writeInt(vo.getFightScore());
            buff.writeByte(vo.getIsFight());
            buff.writeInt(vo.getMonsterId());

            buddyinfoVo = BuddyManager.getBuddyinfoVo(vo.getBuddyId());
            buff.writeString(buddyinfoVo.getName());
            buff.writeByte(buddyinfoVo.getQuality());
        }
    }

    private void writeSkill(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {
        SkillSummaryComponent comp = (SkillSummaryComponent) summary.getComponent(SummaryConst.C_SKILL);
        Map<Byte, Integer> skillPositionMap = comp.getSkillPositionMap();
        Map<Integer, Integer> skillLevelMap = comp.getSkillLevel();
        buff.writeByte((byte) skillPositionMap.size());
        SkillVo skillVo;
//        SkillvupVo skillLevelUpVo;
        Integer skillId;
        for (Map.Entry<Byte, Integer> entry : skillPositionMap.entrySet()) {
            buff.writeByte(entry.getKey());
            skillId = entry.getValue();
            skillVo = SkillManager.getSkillVo(skillId);
//            skillLevelUpVo = SkillManager.getSkillvupVo(skillId,skillLevelMap.get(skillId));
            buff.writeString(skillVo.getName());
            buff.writeString(skillVo.getIcon());
        }
    }

    private void writeFamliySkill(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {
        FamilySummaryComponent comp = (FamilySummaryComponent) summary.getComponent(SummaryConst.C_FAMILY);
        Map<String, Integer> skillLevelMap = comp.getSkillLevelMap();
        buff.writeByte((byte) skillLevelMap.size());
        for (Map.Entry<String, Integer> entry : skillLevelMap.entrySet()) {
            buff.writeString(entry.getKey());
            buff.writeInt(entry.getValue());
        }
    }


    private void writeDeityWeapon(NewByteBuffer buff, Summary summary) {
        DeityWeaponSummaryComponent comp = (DeityWeaponSummaryComponent) summary.getComponent(MConst.Deity);
        if (comp == null) {
            buff.writeInt(0);
        } else {
            buff.writeInt(comp.getCurRoleDeityWeapoonId());
        }
    }
}
