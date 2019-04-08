package com.stars.server.login.packet;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.server.login.LoginConstant;
import com.stars.server.login.bean.LoginInfo;

/**
 * Created by liuyuheng on 2016/1/5.
 */
public class ServerLoginCheck extends Packet {
    private LoginInfo loginInfo;

    @Override
    public short getType() {
        return LoginConstant.SERVER_LOGINCHECK;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        String account = buff.readString();
        String password = buff.readString();
//        String channelId = buff.readString();// 渠道码
//        String channelId_sub = buff.readString();// 子渠道码
//        boolean isChannelChecked = buff.readByte() == 1 ? true : false;// 渠道已验证
//        String authcode = buff.readString();//验证码
//        String mac = buff.readString();
        loginInfo = new LoginInfo(account, password);
//        loginInfo.setChannelId(channelId);
//        loginInfo.setChannelId_sub(channelId_sub);
        loginInfo.setChannelId("");
        loginInfo.setChannelId_sub("");
        loginInfo.setChannelChecked(true);
//        loginInfo.setAuthcode(authcode);
//        loginInfo.setMac(mac);
    }

    @Override
    public void execPacket() {
//        ValidateManager.manager.checkInterval(this.session, loginInfo.getMac());
//        LoginManager.manager.login(this.session, loginInfo);
    }
}
