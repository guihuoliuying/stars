package com.stars.modules.refine.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.refine.RefineModule;
import com.stars.modules.refine.RefinePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-08-02.
 */
public class ServerRefine extends PlayerPacket {
    private static final byte view = 0x00;//打开界面
    private static final byte refine = 0x01;//炼化

    private byte subType;
    private int itemId;
    private int count;

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readByte();
        if (subType == refine) {
            itemId = buff.readInt();
            count = buff.readInt();
        }
    }

    @Override
    public void execPacket(Player player) {
        RefineModule refineModule = module(MConst.Refine);
        switch (subType) {
            case view:
                refineModule.view();
                break;
            case refine:
                refineModule.refine(itemId, count);
                break;
            default:
                break;
        }
    }

    @Override
    public short getType() {
        return RefinePacketSet.S_REFINE;
    }
}
