package com.stars.modules.raffle.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.raffle.RafflePacketSet;
import com.stars.modules.raffle.define.RaffleRewardEntry;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * 
 * @author likang by 2017/4/22
 * 
 */

public class ClientRaffleSelectReward extends PlayerPacket {

	private static final long serialVersionUID = 4297370623892080562L;
	public static final byte SELECT_REWARD_ONE_TIME = 1;
	public static final byte SELECT_REWARD_TEN_TIME = 2;

	private byte subType;
	private int money;
	private List<RaffleRewardEntry> rewardEntry;

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeByte(subType);
		switch(subType){
			case SELECT_REWARD_ONE_TIME:
				buff.writeInt(money);
				break;
			case SELECT_REWARD_TEN_TIME:
				buff.writeInt(money);
				// 奖励
				buff.writeInt(rewardEntry.size());
				for (int j = 0; j < rewardEntry.size(); j++) {
					RaffleRewardEntry entry = rewardEntry.get(j);
					List<Integer> items = entry.getItemRewardList();
					buff.writeInt(items.size());
					for (int item : items) {
						buff.writeInt(item);
					}
				}
				break;
			default:
				break;
		}

	}

	@Override
	public short getType() {
		return RafflePacketSet.C_RaffleSelectReward;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public byte getSubType() {
		return subType;
	}

	public void setSubType(byte subType) {
		this.subType = subType;
	}

	public List<RaffleRewardEntry> getRewardEntry() {
		return rewardEntry;
	}

	public void setRewardEntry(List<RaffleRewardEntry> rewardEntry) {
		this.rewardEntry = rewardEntry;
	}
}
