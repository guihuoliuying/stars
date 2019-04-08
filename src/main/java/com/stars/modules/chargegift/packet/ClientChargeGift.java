package com.stars.modules.chargegift.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.chargegift.ChargeGiftPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenxie on 2017/5/18.
 */
public class ClientChargeGift extends PlayerPacket {

    /**
     * 礼包对应的dropid
     */
    private int chargeGiftDropId;

    /**
     * 玩家当日剩余的可获得的礼包数量
     */
    private int chargeGifSurplusGift;

    /**
     * 玩家当日可以获得的礼包总数量
     */
    private int chargeGiftMaxGift;

    public ClientChargeGift() {
    }

    /**
     *
     * @param chargeGiftDropId  礼包对应的dropid
     * @param chargeGifSurplusGift  玩家当日剩余的可获得的礼包数量
     * @param chargeGiftMaxGift 玩家当日可以获得的礼包总数量
     */
    public ClientChargeGift(int chargeGiftDropId, int chargeGifSurplusGift, int chargeGiftMaxGift) {
        this.chargeGiftDropId = chargeGiftDropId;
        this.chargeGifSurplusGift = chargeGifSurplusGift;
        this.chargeGiftMaxGift = chargeGiftMaxGift;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(chargeGiftDropId);
        buff.writeInt(chargeGifSurplusGift);
        buff.writeInt(chargeGiftMaxGift);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ChargeGiftPacketSet.C_CHARGEGIFT;
    }
}
