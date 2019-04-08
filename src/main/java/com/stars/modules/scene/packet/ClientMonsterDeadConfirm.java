package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuyuheng on 2017/1/21.
 */
public class ClientMonsterDeadConfirm extends PlayerPacket {
    private List<String> monsterUIdList = new LinkedList<>();// 死亡怪物唯一Id

    public ClientMonsterDeadConfirm() {
    }

    public ClientMonsterDeadConfirm(List<String> monsterUIdList) {
        this.monsterUIdList = monsterUIdList;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_MONSTER_DEAD_CONFIRM;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        short size = (short) (monsterUIdList == null ? 0 : monsterUIdList.size());
        buff.writeShort(size);
        if (size == 0)
            return;
        for (String monterUId : monsterUIdList) {
            buff.writeString(monterUId);
        }
    }
}
