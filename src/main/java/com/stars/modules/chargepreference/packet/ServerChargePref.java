package com.stars.modules.chargepreference.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.chargepreference.ChargePrefModule;
import com.stars.modules.chargepreference.ChargePrefPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2017/3/30.
 */
public class ServerChargePref extends PlayerPacket {

    public static final int SUBTYPE_VIEW = 0x00;
    public static final int SUBTYPE_CHOOSE = 0x01;

    private byte subtype;
    private int chosenId;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case SUBTYPE_VIEW:
                break;
            case SUBTYPE_CHOOSE:
                chosenId = buff.readInt();
                break;
        }
    }

    @Override
    public short getType() {
        return ChargePrefPacketSet.S_CHARGE_PREF;
    }

    @Override
    public void execPacket(Player player) {
        ChargePrefModule module = module(MConst.ChargePref);
        switch (subtype) {
            case SUBTYPE_VIEW:
                module.view();
                break;
            case SUBTYPE_CHOOSE:
                module.choose(chosenId);
                break;
        }
    }
}
