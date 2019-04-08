package com.stars.modules.buddy.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.buddy.BuddyModule;
import com.stars.modules.buddy.BuddyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/8/11.
 */
public class ServerLineup extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    private byte lineupId;// 阵型Id
    private int buddyId;// 伙伴Id

    @Override
    public void execPacket(Player player) {
        BuddyModule buddyModule = (BuddyModule) module(MConst.Buddy);
        switch (reqType) {
            case 1:// 配置伙伴
                buddyModule.configLineup(lineupId, buddyId);
                break;
            case 2:// 一键配置
                buddyModule.autoConfigLineup();
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return BuddyPacketSet.S_LINEUP;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
            case 1:// 配置伙伴
                this.lineupId = buff.readByte();
                this.buddyId = buff.readInt();
                break;
            case 2:// 一键配置
                break;
        }
    }
}
