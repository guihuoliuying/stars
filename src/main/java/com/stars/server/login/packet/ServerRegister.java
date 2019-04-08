package com.stars.server.login.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.server.login.LoginConstant;
import com.stars.server.login.manager.RegisterManager;

/**
 * Created by liuyuheng on 2016/1/6.
 */
public class ServerRegister extends Packet {
    private String account;
    private String password;
    private String mac;

    @Override
    public short getType() {
        return LoginConstant.SERVER_REGISTER;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.account = buff.readString();
        this.password = buff.readString();
        this.mac = buff.readString();
    }

    @Override
    public void execPacket() {
        RegisterManager.manager.register(this.session, account, password, mac);
    }
}
