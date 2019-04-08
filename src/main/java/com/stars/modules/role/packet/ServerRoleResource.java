package com.stars.modules.role.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.RolePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/8/9.
 */
public class ServerRoleResource extends PlayerPacket {

    public static final byte SUBTYPE_RECOVERY = 0; // 空置
    public static final byte BUY_VIGOR = 1;// 购买体力
    public static final byte BUY_MONEY_ONCE = 2;// 单次购买金币
    public static final byte BUY_MONEY_MULTI = 3;// 批量购买金币

    private byte subtype;

    @Override
    public short getType() {
        return RolePacketSet.S_ROLE_RESOURCE;
    }

    @Override
    public void execPacket(Player player) {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        switch (subtype) {
            case BUY_VIGOR:
                roleModule.buyVigor();
                break;
            case BUY_MONEY_ONCE:
                roleModule.buyMoneyOnce();
                break;
            case BUY_MONEY_MULTI:
                roleModule.buyMoneyMulti();
                break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
    }
}
