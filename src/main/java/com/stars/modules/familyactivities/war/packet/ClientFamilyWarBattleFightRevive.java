package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/12/8.
 */
public class ClientFamilyWarBattleFightRevive extends PlayerPacket {
	
	public static byte TYPE_OF_COUNT = 1;//复活次数
	public static byte TYPE_OF_TIME = 2;//复活时间
	
	private byte type;
	private int reviveCount = 0;
	private int reviveTime = 0;
	
    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_REVIVE;
    }
    
    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeByte(type);
    	if (type == TYPE_OF_COUNT) {	//type==1
    		buff.writeInt(FamilyActWarManager.totalPayRevive - reviveCount);//剩余复活次数
	//    	buff.writeInt(FamilyActWarManager.totalPayRevive);//最多复活次数
	//    	buff.writeInt(FamilyActWarManager.revivePay);//复活消耗
    	} else if (type == TYPE_OF_TIME) {//type==2
			buff.writeInt(reviveTime);	//复活剩余时间
		}
    }

	public int getReviveTime() {
		return reviveTime;
	}

	public void setReviveTime(int reviveTime) {
		this.reviveTime = reviveTime;
	}

	public int getReviveCount() {
		return reviveCount;
	}

	public void setReviveCount(int reviveCount) {
		this.reviveCount = reviveCount;
	}

	public void setType(byte type) {
		this.type = type;
	}
	
}
