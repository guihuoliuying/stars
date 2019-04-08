package com.stars.server.login.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.server.login.LoginConstant;

/**
 * Created by liuyuheng on 2016/1/5.
 */
public class ClientLoginCheck extends Packet {
    private byte isSuc;
    private String account;
    private long roleId;
    private String ip;
    private int port;

    public ClientLoginCheck(byte isSuc) {
        this.isSuc = isSuc;
        this.account = null;
        this.roleId = 0;
        this.ip = null;
        this.port = 0;
    }

    public ClientLoginCheck(byte isSuc, String account, long roleId, String ip, int port) {
        this.isSuc = isSuc;
        this.account = account;
        this.roleId = roleId;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public short getType() {
        return LoginConstant.CLIENT_LOGINCHECK;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(isSuc);
        buff.writeString(account);
        buff.writeLong(roleId);
        buff.writeString(ip);
        buff.writeInt(port);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }
}
