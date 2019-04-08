package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/7.
 */
public class ClientSpawnMonster extends PlayerPacket {
    private Map<String, Byte> blockStatusMap;// 动态阻挡状态
    private Map<String, FighterEntity> spawnMonsterMap;// 刷怪数据
    private List<String> destroyTrapMonsterList = new ArrayList<String>();//销毁的陷阱怪uidList
    private int spawinId;

    public void setSpawinId(int spawinId) {
        this.spawinId = spawinId;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_SPAWNMONSTER;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(spawinId);
        writeBlockStatus(buff);
        writeSpawnMonster(buff);
        writeDestroyTrapMonster(buff);
    }

    private void writeBlockStatus(com.stars.network.server.buffer.NewByteBuffer buff) {
        short size = (short) (blockStatusMap == null ? 0 : blockStatusMap.size());
        buff.writeShort(size);
        if (blockStatusMap != null) {
            for (Map.Entry<String, Byte> entry : blockStatusMap.entrySet()) {
                buff.writeString(entry.getKey());
                buff.writeByte(entry.getValue());
            }
        }
    }

    private void writeSpawnMonster(com.stars.network.server.buffer.NewByteBuffer buff) {
        short size = (short) (spawnMonsterMap == null ? 0 : spawnMonsterMap.size());
        buff.writeShort(size);
        if (spawnMonsterMap != null) {
            for (FighterEntity monsterEntity : spawnMonsterMap.values()) {
                monsterEntity.writeToBuff(buff);
            }
        }
    }
    
    private void writeDestroyTrapMonster(NewByteBuffer buff) {
        short size = (short) (destroyTrapMonsterList == null ? 0 : destroyTrapMonsterList.size());
        buff.writeShort(size);
        if (destroyTrapMonsterList != null) {
            for (String trapMonsterUid : destroyTrapMonsterList) {
                buff.writeString(trapMonsterUid);
            }
        }
    }

    public void setBlockStatusMap(Map<String, Byte> blockStatusMap) {
        this.blockStatusMap = blockStatusMap;
    }

    public void setSpawnMonsterMap(Map<String, FighterEntity> spawnMonsterMap) {
        this.spawnMonsterMap = spawnMonsterMap;
    }
    
    public void setDestroyTrapMonsterList(List<String> destroyTrapMonsterList) {
        this.destroyTrapMonsterList = destroyTrapMonsterList;
    }
}
