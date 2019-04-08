package com.stars.modules.popUp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.popUp.PopUpModule;
import com.stars.modules.popUp.PopUpPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class ServerPopUp extends PlayerPacket {

    public static final byte REQ_FORBIDDEN = 0x00;  // 禁用弹窗
    public static final byte REQ_RECORD = 0x01;     // 记录已经打开的弹窗id

    private byte subtype;
    private int popId;

    @Override
    public void execPacket(Player player) {
        PopUpModule popUpModule = module(MConst.PopUp);
        switch (subtype) {
            case REQ_FORBIDDEN:
                popUpModule.forbidden(popId);
                break;
            case REQ_RECORD:
                popUpModule.recordPopUp(popId);
                break;
        }
    }

    @Override
    public short getType() {
        return PopUpPacketSet.S_POPUP;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case REQ_FORBIDDEN:
                popId = buff.readInt();
                break;
            case REQ_RECORD:
                popId = buff.readInt();
                break;
        }
    }
}
