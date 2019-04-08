package com.stars.modules.welfareaccount.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.welfareaccount.WelfareAccountModule;
import com.stars.modules.welfareaccount.WelfareAccountPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/4/11.
 */
public class ServerWelfareAccountPacket extends PlayerPacket {
    public static final byte IS_WELFARE_ACCOUNT = 1;//查询是否是福利账户
    public static final byte QUERY_VIRTUAL_MONEY_INNER = 2;//（内部）查询虚拟币,无警告
    public static final byte QUERY_VIRTUAL_MONEY_OUTTER = 3;//（外部）查询虚拟币
    public static final byte VIRTUAL_CHARGE = 4;//虚拟充值
    byte subType;
    int chargeId;
    byte payPoint;

    @Override
    public short getType() {
        return WelfareAccountPacketSet.S_VIRTUALMONERY;
    }


    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readByte();
        switch (subType) {
            case VIRTUAL_CHARGE: {
                this.chargeId = buff.readInt();
                this.payPoint = buff.readByte();
            }
        }

    }

    @Override
    public void execPacket(Player player) {
        WelfareAccountModule module = module(MConst.WelfareAccount);
        switch (subType) {
            case IS_WELFARE_ACCOUNT: {
                module.queryWelfareAccount();
            }
            break;
            case QUERY_VIRTUAL_MONEY_INNER: {
                module.queryAccountMoneyInner();
            }
            break;
            case QUERY_VIRTUAL_MONEY_OUTTER: {
                module.queryAccountMoneyOutter();
            }
            break;
            case VIRTUAL_CHARGE: {
                module.charge(chargeId, payPoint);
            }
            break;
        }
    }

}
