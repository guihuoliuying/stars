package com.stars.modules.buddy.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.buddy.BuddyPacketSet;
import com.stars.modules.buddy.prodata.*;
import com.stars.modules.buddy.userdata.RoleBuddy;
import com.stars.modules.buddy.userdata.RoleBuddyLineup;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/8/11.
 */
public class ClientAllBuddyData extends PlayerPacket {
    private Map<Integer, BuddyinfoVo> buddyinfoVoMap;
    private Map<Integer, Map<Integer, BuddyLevelVo>> levelVoMap;
    private Map<Integer, Map<Integer, BuddyStageVo>> stageVoMap;
    private Map<Integer, Map<Integer, BuddyArmsVo>> armVoMap;
    private Map<Byte, Map<Integer, BuddyLineupVo>> lineupVoMap;
    private Map<Integer, RoleBuddy> roleBuddyMap;
    private Map<Byte, RoleBuddyLineup> roleLineupMap;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BuddyPacketSet.C_ALL_BUDDY_DATA;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        writeBuddyinfoVo(buff);// 伙伴基本+等级+阶级+飞升 产品数据
        writeLineupVo(buff);// 阵型产品数据
        writeRoleBuddy(buff);// 伙伴玩家数据
        writeRoleLineup(buff);// 阵型玩家数据
    }

    private void writeBuddyinfoVo(com.stars.network.server.buffer.NewByteBuffer buff) {
        byte size = (byte) (buddyinfoVoMap == null ? 0 : buddyinfoVoMap.size());
        buff.writeByte(size);
        if (size > 0) {
            for (BuddyinfoVo buddyinfoVo : buddyinfoVoMap.values()) {
                buddyinfoVo.writeToBuff(buff);
                writeLevelVo(buddyinfoVo.getBuddyId(), buff);
                writeStageVo(buddyinfoVo.getBuddyId(), buff);
                writeArmVo(buddyinfoVo.getBuddyId(), buff);
            }
        }
    }

    private void writeLevelVo(int buddyId, com.stars.network.server.buffer.NewByteBuffer buff) {
        Map<Integer, BuddyLevelVo> map = levelVoMap.get(buddyId);
        short levelSize = (short) (map == null ? 0 : map.size());
        buff.writeShort(levelSize);
        if (levelSize == 0)
            return;
        for (BuddyLevelVo buddyLevelVo : map.values()) {
            buddyLevelVo.writeToBuff(buff);
        }
    }

    private void writeStageVo(int buddyId, com.stars.network.server.buffer.NewByteBuffer buff) {
        Map<Integer, BuddyStageVo> map = stageVoMap.get(buddyId);
        short size = (short) (map == null ? 0 : map.size());
        buff.writeShort(size);
        if (size == 0)
            return;
        for (BuddyStageVo stageVo : map.values()) {
            stageVo.writeToBuff(buff);
        }
    }

    private void writeArmVo(int buddyId, com.stars.network.server.buffer.NewByteBuffer buff) {
        Map<Integer, BuddyArmsVo> map = armVoMap.get(buddyId);
        short size = (short) (map == null ? 0 : map.size());
        buff.writeShort(size);
        if (size == 0)
            return;
        for (BuddyArmsVo vo : map.values()) {
            vo.writeToBuff(buff);
        }
    }

    private void writeLineupVo(com.stars.network.server.buffer.NewByteBuffer buff) {
        byte size = (byte) (lineupVoMap == null ? 0 : lineupVoMap.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (Map.Entry<Byte, Map<Integer, BuddyLineupVo>> entry : lineupVoMap.entrySet()) {
            buff.writeByte(entry.getKey());// lineupid
            short levelSize = (short) (entry.getValue() == null ? 0 : entry.getValue().size());
            buff.writeShort(levelSize);
            for (BuddyLineupVo lineupVo : entry.getValue().values()) {
                lineupVo.writeToBuff(buff);
            }
        }
    }

    private void writeRoleBuddy(com.stars.network.server.buffer.NewByteBuffer buff) {
        byte size = (byte) (roleBuddyMap == null ? 0 : roleBuddyMap.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (RoleBuddy roleBuddy : roleBuddyMap.values()) {
            roleBuddy.writeToBuff(buff);
        }
    }

    private void writeRoleLineup(NewByteBuffer buff) {
        byte size = (byte) (roleLineupMap == null ? 0 : roleLineupMap.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (RoleBuddyLineup roleBuddyLineup : roleLineupMap.values()) {
            roleBuddyLineup.writeToBuff(buff);
        }
    }

    public void setBuddyinfoVoMap(Map<Integer, BuddyinfoVo> buddyinfoVoMap) {
        this.buddyinfoVoMap = buddyinfoVoMap;
    }

    public void setLevelVoMap(Map<Integer, Map<Integer, BuddyLevelVo>> levelVoMap) {
        this.levelVoMap = levelVoMap;
    }

    public void setStageVoMap(Map<Integer, Map<Integer, BuddyStageVo>> stageVoMap) {
        this.stageVoMap = stageVoMap;
    }

    public void setLineupVoMap(Map<Byte, Map<Integer, BuddyLineupVo>> lineupVoMap) {
        this.lineupVoMap = lineupVoMap;
    }

    public void setRoleBuddyMap(Map<Integer, RoleBuddy> roleBuddyMap) {
        this.roleBuddyMap = roleBuddyMap;
    }

    public void setRoleLineupMap(Map<Byte, RoleBuddyLineup> roleLineupMap) {
        this.roleLineupMap = roleLineupMap;
    }

    public void setArmVoMap(Map<Integer, Map<Integer, BuddyArmsVo>> armVoMap) {
        this.armVoMap = armVoMap;
    }
}