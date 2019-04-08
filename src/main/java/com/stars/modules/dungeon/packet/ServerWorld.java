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
public class ServerWorld extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    private int chapterId;

    @Override
    public void execPacket(Player player) {
        DungeonModule dungeonModule = (DungeonModule) module(MConst.Dungeon);
        switch (reqType) {
            case 1:// 打开章节列表
                dungeonModule.sendAllChapterData();
                break;
            case 2:// 领取章节集星奖励
                dungeonModule.chapterStarReward(chapterId);
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return DungeonPacketSet.S_WORLD;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case 1:
                break;
            case 2:// 领取章节集星奖励
                this.chapterId = buff.readInt();
                break;
            default:
                break;
        }
    }
}
