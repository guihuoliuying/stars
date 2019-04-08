package com.stars.modules.discountgift.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.discountgift.DiscountGiftPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenxie on 2017/5/26.
 */
public class ClientDiscountGift extends PlayerPacket {

    /**
     * 商品组id
     */
    private int giftGroupId;

    /**
     * 商品对应的充值额度
     */
    private int charge;

    /**
     * 商品id对应的dropid
     */
    private int dropId;

    /**
     * 累计充值额度（对当前选择的商品有效）
     */
    private int totalCharge;

    /**
     * 界面背景图资源名
     */
    private String img;

    private int originalPrice;

    /**
     * 活动时间
     */
    private String date;

    @Override
    public short getType() {
        return DiscountGiftPacketSet.C_DICOUNT_GIFT;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(giftGroupId);
        buff.writeInt(charge);
        buff.writeInt(dropId);
        buff.writeInt(totalCharge);
        buff.writeString(img);
        buff.writeString(date);
        buff.writeInt(originalPrice);
    }

    public int getGiftGroupId() {
        return giftGroupId;
    }

    public void setGiftGroupId(int giftGroupId) {
        this.giftGroupId = giftGroupId;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public int getDropId() {
        return dropId;
    }

    public void setDropId(int dropId) {
        this.dropId = dropId;
    }

    public int getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(int totalCharge) {
        this.totalCharge = totalCharge;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setOriginalPrice(int originalPrice) {
        this.originalPrice = originalPrice;
    }
}
