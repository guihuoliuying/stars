package com.stars.modules.dungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.DungeonPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/6/21.
 */
public class ServerDungeon extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    private int dungeonId;// 关卡Id
    private byte sweepTimes;// 扫荡次数

    @Override
    public void execPacket(Player player) {
        DungeonModule dungeonModule = (DungeonModule) module(MConst.Dungeon);
        switch (reqType) {
            case 1:// 关卡扫荡请求
                dungeonModule.sweepDungeon(dungeonId, sweepTimes);
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return DungeonPacketSet.S_DUNGEON;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case 1:// 根据章节Id请求关卡玩家数据
                this.dungeonId = buff.readInt();
                this.sweepTimes = buff.readByte();
                break;
            default:
                break;
        }
    }
}
