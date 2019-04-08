package com.stars.modules.demologin.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.demologin.LoginPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2017/1/17.
 */
public class ClientBlockAccount extends PlayerPacket {
    private int expireTime;// 解封时间戳(秒)
    private String reason;// 封号原因

    public ClientBlockAccount() {
    }

    public ClientBlockAccount(int expireTime, String reason) {
        this.expireTime = expireTime;
        this.reason = reason;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return LoginPacketSet.C_BLOCK_ACCOUNT;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(expireTime);// 解封时间戳(秒)
        buff.writeString(reason);// 封号原因
        buff.writeInt((int) (System.currentTimeMillis() / 1000));// 当前时间戳
    }
}
