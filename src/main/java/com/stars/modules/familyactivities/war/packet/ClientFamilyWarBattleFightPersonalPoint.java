package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/12/8.
 */
public class ClientFamilyWarBattleFightPersonalPoint extends PlayerPacket {

    private long points;

    public ClientFamilyWarBattleFightPersonalPoint() {
    }

    public ClientFamilyWarBattleFightPersonalPoint(long points) {
        this.points = points;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_PERSONAL_POINTS;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(Long.toString(points));
        LogUtil.info("familywar|roleId:{},个人积分:{}", getRoleId(), points);
    }
}
