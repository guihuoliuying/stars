package com.stars.modules.raffle.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.raffle.RaffleModule;
import com.stars.modules.raffle.RafflePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 
 * @author likang by 2017/4/22
 * 
 */

public class ServerRaffleSelectReward extends PlayerPacket {

	private static final long serialVersionUID = -8648123824830310513L;

	private static final byte ONE_TIME_RAFFLE_SELECT = 1; //单抽
	private static final byte TEN_TIME_RAFFLE_SELECT = 2; //十连抽
	private int selectId;//选择的DropId
	private byte selectType; //选择类型  一次抽和十连抽不一样

	@Override
	public void execPacket(Player player) {
		RaffleModule raffleModule = module(MConst.Raffle);
		if (raffleModule == null) {
			return;
		}
		if(selectType == ONE_TIME_RAFFLE_SELECT){
			raffleModule.checkAndSendSuperReward(selectId);
		}else if(selectType == TEN_TIME_RAFFLE_SELECT){
			raffleModule.checkAndSendSuperRewardForTenTime(selectId);
		}
	}

	@Override
	public short getType() {
		return RafflePacketSet.S_RaffleSelectReward;
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		this.selectType = buff.readByte();
		this.selectId = buff.readInt();
	}

}
