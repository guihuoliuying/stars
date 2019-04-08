package com.stars.modules.wordExchange.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.wordExchange.WordExchangeModule;
import com.stars.modules.wordExchange.WordExchangePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class ServerWordExchange extends PlayerPacket {

    public static final byte REQ_VIEW = 0x01; // 查看集字活动界面
    public static final byte REQ_EXCHANGE = 0x02; // 请求兑换

    private byte subtype;
    private int id;
    private int count;

    @Override
    public void execPacket(Player player) {
        WordExchangeModule wordExchangeModule = module(MConst.WordExchange);
        switch (subtype) {
            case REQ_VIEW:
                wordExchangeModule.viewMainUI();
                break;
            case REQ_EXCHANGE:
                wordExchangeModule.exchange(id,count);
                break;
        }
    }

    @Override
    public short getType() {
        return WordExchangePacketSet.S_WORDEXCHANGE;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case REQ_VIEW:

                break;
            case REQ_EXCHANGE:
                id = buff.readInt();
                count = buff.readInt();
                break;
        }
    }
}
