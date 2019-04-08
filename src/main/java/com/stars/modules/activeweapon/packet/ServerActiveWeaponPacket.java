package com.stars.modules.activeweapon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.activeweapon.ActiveWeaponModule;
import com.stars.modules.activeweapon.ActiveWeaponPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/6/15.
 */
public class ServerActiveWeaponPacket extends PlayerPacket {
    private short subType;
    public static final short REQ_SEND_ACTIVE_WEAPON = 1;//请求活跃神兵产品数据
    public static final short REQ_SEND_TAKED_RECORD = 2;//发送领奖记录
    public static final short REQ_TAKE_REWARD = 3;//领奖
    public Integer conditionId;

    @Override
    public void execPacket(Player player) {
        ActiveWeaponModule activeWeaponModule = module(MConst.ActiveWeapon);
        switch (subType) {
            case REQ_SEND_ACTIVE_WEAPON: {
                activeWeaponModule.sendActiveWeaponList();
            }
            break;
            case REQ_SEND_TAKED_RECORD: {
                activeWeaponModule.sendTakedRecord();
            }
            break;
            case REQ_TAKE_REWARD: {
                activeWeaponModule.takeReward(conditionId);
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_TAKE_REWARD: {
                conditionId = buff.readInt();
            }
            break;
        }
    }

    @Override
    public short getType() {
        return ActiveWeaponPacketSet.S_ACTIVE_WEAPON;
    }
}
