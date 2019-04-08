package com.stars.modules.fashioncard.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.fashioncard.FashionCardPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-10-16.
 */
public class ClientFashionCard extends PlayerPacket {
    public static final byte RES_VIEW = 0x00;
    public static final byte RES_SYNC_STATE = 0x01;
    public static final byte RES_ACTIVE = 0x02;

    private byte subType;

    private Map<Integer, Integer> roleData = new HashMap<>();
    private int fashionCardId;
    private int isPutOn;//0 未穿戴 or 1 穿戴

    public ClientFashionCard() {
    }

    public ClientFashionCard(byte subType) {
        this.subType = subType;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType) {
            case RES_VIEW:
                buff.writeByte((byte) roleData.size());
                for (Map.Entry<Integer, Integer> entry : roleData.entrySet()) {
                    buff.writeInt(entry.getKey());//fashionCardId
                    buff.writeInt(entry.getValue());// put on or take off
                }
                break;
            case RES_SYNC_STATE:
                buff.writeInt(fashionCardId);
                buff.writeInt(isPutOn);
                break;
            case RES_ACTIVE:
                buff.writeInt(fashionCardId);
                break;
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FashionCardPacketSet.C_FASHION_CARD;
    }

    public void setRoleData(Map<Integer, Integer> roleData) {
        this.roleData = roleData;
    }

    public void setFashionCardId(int fashionCardId) {
        this.fashionCardId = fashionCardId;
    }

    public void setIsPutOn(int isPutOn) {
        this.isPutOn = isPutOn;
    }
}
