package com.stars.modules.friend.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.summary.Summary;

import java.util.Map;

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

    }

    // 装备
    private void writeEquipment(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {

    }

    private void writeFamily(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {

    }

    private void writeBuddy(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {

    }

    private void writeSkill(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {

    }

    private void writeFamliySkill(com.stars.network.server.buffer.NewByteBuffer buff, Summary summary) {

    }


    private void writeDeityWeapon(NewByteBuffer buff, Summary summary) {
    }
}
