package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/12/20.
 */
public class ClientFamilyWarUiEnter extends PlayerPacket {

    public static final byte SUBTYPE_MATCHING = 0x10; // 匹配中（客户端收到这个后展示顺计时）
    public static final byte SUBTYPE_CANCEL_OK = 0x11; // 取消成功

    private byte subtype;

    public ClientFamilyWarUiEnter() {
    }

    public ClientFamilyWarUiEnter(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_UI_ENTER;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
    }
}
