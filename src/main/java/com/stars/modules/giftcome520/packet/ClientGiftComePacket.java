package com.stars.modules.giftcome520.packet;

import com.stars.modules.giftcome520.GiftComePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * Created by huwenjun on 2017/4/15.
 */
public class ClientGiftComePacket extends Packet {
    private byte subType;
    public final static byte SEND_UI_RESOURCE = 1;
    public final static byte SEND_ROLE_ACT_STATE = 2;
    private String ruleDesc="";
    private String timeDesc="";
    private String tips="";
    private byte btnType=-1;
    private String btnText="";
    private String rewardShowItem="";
    private int dropId=-1;

    public ClientGiftComePacket(byte subType) {
        this.subType = subType;
    }

    public ClientGiftComePacket() {

    }

    @Override
    public short getType() {
        return GiftComePacketSet.C_GiftCome;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType) {
            case SEND_UI_RESOURCE: {
                writeUIResource(buff);
            }
            break;
            case SEND_ROLE_ACT_STATE: {
                writeActState(buff);
            }
            break;
        }
    }

    private void writeActState(NewByteBuffer buff) {
        buff.writeByte(btnType);
    }

    private void writeUIResource(NewByteBuffer buff) {
        buff.writeString(ruleDesc);
        buff.writeString(timeDesc);
        buff.writeString(tips);
        buff.writeInt(dropId);
        buff.writeString(rewardShowItem);
        buff.writeByte(btnType);
        buff.writeString(btnText);
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public byte getSubType() {
        return subType;
    }

    public void setSubType(byte subType) {
        this.subType = subType;
    }

    public String getRuleDesc() {
        return ruleDesc;
    }

    public void setRuleDesc(String ruleDesc) {
        this.ruleDesc = ruleDesc;
    }

    public String getTimeDesc() {
        return timeDesc;
    }

    public void setTimeDesc(String timeDesc) {
        this.timeDesc = timeDesc;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }


    public byte getBtnType() {
        return btnType;
    }

    public void setBtnType(byte btnType) {
        this.btnType = btnType;
    }

    public String getBtnText() {
        return btnText;
    }

    public void setBtnText(String btnText) {
        this.btnText = btnText;
    }

    public String getRewardShowItem() {
        return rewardShowItem;
    }

    public void setRewardShowItem(String rewardShowItem) {
        this.rewardShowItem = rewardShowItem;
    }

    public Integer getDropId() {
        return dropId;
    }

    public void setDropId(Integer dropId) {
        this.dropId = dropId;
    }
}
