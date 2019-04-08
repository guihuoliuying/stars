package com.stars.modules.daily5v5.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.daily5v5.Daily5v5PacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

public class ClientDaily5v5BattleStat extends PlayerPacket {
	
	private int myKillCount;
    private int myDeadCount;
    private int myAssistCount;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(myKillCount);
        buff.writeInt(myDeadCount);
        buff.writeInt(myAssistCount);
        LogUtil.info("daily5v5|杀人数:{},死亡数:{},助攻数:{}", myKillCount, myDeadCount, myAssistCount);
    }

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return Daily5v5PacketSet.Client_Daily5v5BattleStat;
	}

	public int getMyKillCount() {
		return myKillCount;
	}

	public void setMyKillCount(int myKillCount) {
		this.myKillCount = myKillCount;
	}

	public int getMyDeadCount() {
		return myDeadCount;
	}

	public void setMyDeadCount(int myDeadCount) {
		this.myDeadCount = myDeadCount;
	}

	public int getMyAssistCount() {
		return myAssistCount;
	}

	public void setMyAssistCount(int myAssistCount) {
		this.myAssistCount = myAssistCount;
	}

}
