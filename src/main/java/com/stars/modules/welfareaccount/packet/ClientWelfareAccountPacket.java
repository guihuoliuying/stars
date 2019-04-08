package com.stars.modules.welfareaccount.packet;

import com.stars.modules.welfareaccount.WelfareAccountPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

/**
 * Created by huwenjun on 2017/4/11.
 */
public class ClientWelfareAccountPacket extends Packet {

    private byte subType;
    private int virtualMoney;
    private int isWelfareAccount;
    public static final byte IS_WELFARE_ACCOUNT = 1;//查询是否是福利账户
    public static final byte QUERY_VIRTUAL_MONEY = 2;//查询虚拟币

    public ClientWelfareAccountPacket() {

    }

    public ClientWelfareAccountPacket(byte subType) {
        this.subType = subType;
    }

    @Override
    public short getType() {
        return WelfareAccountPacketSet.C_VIRTUALMONERY;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType) {
            case IS_WELFARE_ACCOUNT: {
                buff.writeByte((byte) isWelfareAccount);
            }
            break;
            case QUERY_VIRTUAL_MONEY: {
                buff.writeInt(virtualMoney);

            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public int getVirtualMoney() {
        return virtualMoney;
    }

    public void setVirtualMoney(int virtualMoney) {
        this.virtualMoney = virtualMoney;
    }

    public int getIsWelfareAccount() {
        return isWelfareAccount;
    }

    public void setIsWelfareAccount(int isWelfareAccount) {
        this.isWelfareAccount = isWelfareAccount;
    }
}
