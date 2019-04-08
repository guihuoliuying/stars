package com.stars.modules.demologin.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.LoginPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by wuyuxing on 2017/1/12.
 */
public class ServerReconnect extends PlayerPacket {

    private byte opType;
    private String token;
    private long roleId;
    private String account;
    private int maxClientPacketId;

    @Override
    public void execPacket(Player player) {
        LoginModule loginModule = module(MConst.Login);
        if(opType == 0) {
            loginModule.handleReconnect(this);
        }else{
            loginModule.handleOfflineMsgFromGM();
        }
    }

    @Override
    public short getType() {
        return LoginPacketSet.S_RECONNECT;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.opType = buff.readByte();
        if(opType == 0) {
            this.token = buff.readString();
            this.roleId = Long.parseLong(buff.readString());
            this.account = buff.readString();
            this.maxClientPacketId = buff.readInt();
        }
    }

    public String getToken() {
        return token;
    }

    @Override
    public long getRoleId() {
        return roleId;
    }

    public String getAccount() {
        return account;
    }

    public int getMaxClientPacketId() {
        return maxClientPacketId;
    }
}
