package com.stars.modules.guardofficial.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.guardofficial.GuardOfficialModule;
import com.stars.modules.guardofficial.GuardOfficialPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-07-12.
 */
public class ServerGuardOfficial extends PlayerPacket {
    private byte subtype;
    private static final byte REQ_LIMIT_TIME = 1; // 请求副本次数
    private static final byte REQ_ENTER_SCENE = 2; // 请求进入副本

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
    }
    @Override
    public void execPacket(Player player) {
        GuardOfficialModule officialModule = module(MConst.GuardOfficial);
        switch (subtype) {
            case REQ_LIMIT_TIME:
//                officialModule.sendTimes();
                break;
            case REQ_ENTER_SCENE:
                officialModule.enterScene();
                break;
        }
    }

    @Override
    public short getType() {
        return GuardOfficialPacketSet.S_GUARDOFFICIAL;
    }
}
