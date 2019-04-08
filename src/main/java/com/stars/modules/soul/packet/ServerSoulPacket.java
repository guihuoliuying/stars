package com.stars.modules.soul.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.soul.SoulModule;
import com.stars.modules.soul.SoulPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class ServerSoulPacket extends PlayerPacket {
    private short subType;//
    public static final short REQ_MAIN_UI = 1;//打开主界面
    public static final short REQ_UPGRADE = 2;//请求升级
    public static final short REQ_ONEKEY_UPGRADE = 3;//请求一键升级
    public static final short REQ_BREAK = 4;//请求突破

    @Override
    public void execPacket(Player player) {
        SoulModule soulModule = module(MConst.Soul);
        switch (subType) {
            case REQ_MAIN_UI: {
                soulModule.reqMainUI();
            }
            break;
            case REQ_UPGRADE: {
                soulModule.reqUpgrade();
            }
            break;
            case REQ_ONEKEY_UPGRADE: {
                soulModule.reqOnekeyUpgrade();
            }
            break;
            case REQ_BREAK: {
                soulModule.reqBreak();
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
    }

    @Override
    public short getType() {
        return SoulPacketSet.S_SOUL;
    }
}
