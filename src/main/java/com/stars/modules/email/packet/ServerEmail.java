package com.stars.modules.email.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.email.EmailModule;
import com.stars.modules.email.EmailPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/8/2.
 */
public class ServerEmail extends PlayerPacket {

    public static final byte S_GET_LIST = 1; // 获取邮件列表
    public static final byte S_READ = 2; // 阅读邮件
    public static final byte S_DELETE = 3; // 删除邮件
    public static final byte S_FETCH_AFFIXS = 4; // 提取附件
    public static final byte S_ALL_DELETE = 5; // 全部删除
    public static final byte S_ALL_FETCH = 6; // 全部提取

    private byte subtype;
    private int emailId;

    @Override
    public void execPacket(Player player) {
        EmailModule emailModule = (EmailModule) module(MConst.Email);
        emailModule.handleRequest(this);
    }

    @Override
    public short getType() {
        return EmailPacketSet.S_EMAIL;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.subtype = buff.readByte();
        switch (this.subtype) {
            case S_READ:
            case S_DELETE:
            case S_FETCH_AFFIXS:
                this.emailId = buff.readInt();
                break;
        }
    }

    public byte getSubtype() {
        return subtype;
    }

    public int getEmailId() {
        return emailId;
    }
}
