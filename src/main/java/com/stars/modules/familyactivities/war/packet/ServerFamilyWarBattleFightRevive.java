package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/12/8.
 */
public class ServerFamilyWarBattleFightRevive extends PlayerPacket {
	
	private byte reqType;

    @Override
    public void execPacket(Player player) {
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.S_FAMILY_WAR_BATTLE_FIGHT_REVIVE;
    }
    
    @Override
    public void readFromBuffer(NewByteBuffer buff) {
    	reqType = buff.readByte();
    }

	public byte getReqType() {
		return reqType;
	}

	public void setReqType(byte reqType) {
		this.reqType = reqType;
	}
}
