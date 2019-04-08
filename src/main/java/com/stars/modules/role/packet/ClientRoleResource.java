package com.stars.modules.role.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.role.RolePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/8/9.
 */
public class ClientRoleResource extends PlayerPacket {

    public static final byte VIGOR_RECOVERY = 0;// 体力恢复
    public static final byte VIGOR_BUY = 1;// 体力购买更新
    public static final byte MONEY_BUY = 2;// 金币购买更新

    private byte subtype;
    private int recoveryRemainingTime; // 下次恢复的剩余时间
    private int currentBuyCount; // 当前购买次数

    private byte usedFreeBuyMoneyCount;// 已使用免费购买金币次数
    private byte usedPayBuyMoneyCount;// 已使用付费购买金币次数
    private byte isDouble;// 是否触发翻倍
    private int buyMoneyGainCount;// 购买金币获得数量

    public ClientRoleResource() {
    }

    public ClientRoleResource(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public short getType() {
        return RolePacketSet.C_ROLE_RESOURCE;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case VIGOR_RECOVERY:
                buff.writeInt(recoveryRemainingTime);
                break;
            case VIGOR_BUY:
                buff.writeInt(currentBuyCount);
                break;
            case MONEY_BUY:
                buff.writeByte(isDouble);// 是否触发翻倍
                buff.writeInt(buyMoneyGainCount);// 购买金币获得数量
                buff.writeByte(usedFreeBuyMoneyCount);// 已使用免费购买金币次数
                buff.writeByte(usedPayBuyMoneyCount);// 已使用付费购买金币次数
                break;
        }
    }

    public void setRecoveryRemainingTime(int recoveryRemainingTime) {
        this.recoveryRemainingTime = recoveryRemainingTime;
    }

    public void setCurrentBuyCount(int currentBuyCount) {
        this.currentBuyCount = currentBuyCount;
    }

    public void setUsedFreeBuyMoneyCount(byte usedFreeBuyMoneyCount) {
        this.usedFreeBuyMoneyCount = usedFreeBuyMoneyCount;
    }

    public void setUsedPayBuyMoneyCount(byte usedPayBuyMoneyCount) {
        this.usedPayBuyMoneyCount = usedPayBuyMoneyCount;
    }

    public void setIsDouble(byte isDouble) {
        this.isDouble = isDouble;
    }

    public void setBuyMoneyGainCount(int buyMoneyGainCount) {
        this.buyMoneyGainCount = buyMoneyGainCount;
    }
}
