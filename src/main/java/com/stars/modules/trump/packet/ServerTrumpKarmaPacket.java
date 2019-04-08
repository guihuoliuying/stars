package com.stars.modules.trump.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.trump.TrumpModule;
import com.stars.modules.trump.TrumpPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/10/18.
 */
public class ServerTrumpKarmaPacket extends PlayerPacket {
    private short subType;
    public static final short REQ_KARMA_LIST = 1;//请求仙缘列表
    public static final short REQ_ACTIVE_KARMA = 2;//请求激活仙缘
    private int karmaId;
    private boolean includeProduct;//是否包含产品数据

    @Override
    public void execPacket(Player player) {
        TrumpModule trumpModule = module(MConst.Trump);
        switch (subType) {
            case REQ_KARMA_LIST: {
                trumpModule.reqKarmaList(includeProduct);
            }
            break;
            case REQ_ACTIVE_KARMA: {
                trumpModule.activeKarma(karmaId);
            }
        }
    }

    @Override
    public short getType() {
        return TrumpPacketSet.SERVER_TRUMP_KARMA;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_KARMA_LIST: {
                includeProduct = 1 == buff.readInt();
            }
            break;
            case REQ_ACTIVE_KARMA: {
                karmaId = buff.readInt();
            }
            break;
        }
    }
}
