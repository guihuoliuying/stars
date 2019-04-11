package com.stars.modules.vip.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.vip.VipPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/12/7.
 */
public class ServerVipOption extends PlayerPacket {
    private byte reqType;// 请求类型(子协议)

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return VipPacketSet.S_VIPOPTION;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.reqType = buff.readByte();
        switch (reqType) {
        }
    }
}
