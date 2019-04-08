package com.stars.modules.vip.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.vip.VipPacketSet;
import com.stars.modules.vip.userdata.RoleVip;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/12/7.
 */
public class ClientVipData extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte SEND_UPDATE_DATA = 1;// 下发更新数据
    public static final byte MONTH_CARD_DAILY_REWARD = 2;// 更新月卡每日奖励领取状态

    /* 参数 */
    private RoleVip roleVip;
    
    private int vipExp;

    public ClientVipData() {
    }

    public ClientVipData(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return VipPacketSet.C_VIPDATA;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case SEND_UPDATE_DATA:
            	buff.writeInt(vipExp);
                roleVip.writeToBuff(buff);
                break;
            case MONTH_CARD_DAILY_REWARD:
                buff.writeInt(roleVip.getMonthCardRest());// 月卡奖励领取剩余天数
                buff.writeByte(roleVip.getMonthCardRewardStatus());// 月卡奖励每日领取状态
                break;
        }
    }

    public void setRoleVip(RoleVip roleVip) {
        this.roleVip = roleVip;
    }

	public void setVipExp(int vipExp) {
		this.vipExp = vipExp;
	}
}
