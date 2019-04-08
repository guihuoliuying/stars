package com.stars.modules.dungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.dungeon.DungeonPacketSet;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.dungeon.userdata.RoleDungeon;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/6/21.
 */
public class ClientDungeon extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte SEND_ALL_DUNGEON = 1;// 下发所有关卡数据
    public static final byte UPDATE_DUNGEON = 3;// 更新关卡玩家数据
    public static final byte SWEEP_RESULT = 4;// 关卡扫荡结果

    private Map<Integer, DungeoninfoVo> dungeonVoMap;// 关卡产品数据
    private Map<Integer, RoleDungeon> roleDungeonMap;// 关卡玩家数据
    private List<Map<Integer, Integer>> sweepResult;

    public ClientDungeon() {
    }

    public ClientDungeon(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return DungeonPacketSet.C_DUNGEON;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case SEND_ALL_DUNGEON:
                sendDungeonVo(buff);
                sendRoleDungeon(buff);
                break;
            case UPDATE_DUNGEON:
                sendRoleDungeon(buff);
                break;
            case SWEEP_RESULT:
                byte size = (byte) (sweepResult == null ? 0 : sweepResult.size());
                buff.writeByte(size);
                if (size != 0) {
                    for (Map<Integer, Integer> map : sweepResult) {
                        buff.writeByte((byte) map.size());
                        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                            buff.writeInt(entry.getKey());
                            buff.writeInt(entry.getValue());
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private void sendDungeonVo(com.stars.network.server.buffer.NewByteBuffer buff) {
        short size = (short) (dungeonVoMap == null ? 0 : dungeonVoMap.size());
        buff.writeShort(size);
        if (size != 0) {
            for (DungeoninfoVo dungeonVo : dungeonVoMap.values()) {
                dungeonVo.writeToBuff(buff);
            }
        }
    }

    private void sendRoleDungeon(NewByteBuffer buff) {
        short size = (short) (roleDungeonMap == null ? 0 : roleDungeonMap.size());
        buff.writeShort(size);
        if (size != 0) {
            for (RoleDungeon roleDungeon : roleDungeonMap.values()) {
                roleDungeon.writeToBuff(buff);
            }
        }
    }

    public void setDungeonVoMap(Map<Integer, DungeoninfoVo> dungeonVoMap) {
        this.dungeonVoMap = dungeonVoMap;
    }

    public void setRoleDungeonMap(Map<Integer, RoleDungeon> roleDungeonMap) {
        this.roleDungeonMap = roleDungeonMap;
    }

    public void setSweepResult(List<Map<Integer, Integer>> sweepResult) {
        this.sweepResult = sweepResult;
    }
}
