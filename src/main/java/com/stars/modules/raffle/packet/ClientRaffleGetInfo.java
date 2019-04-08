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

public class ClientRaffleGetInfo extends PlayerPacket {

	private static final long serialVersionUID = -5832122650235444686L;

	private int userTimes;
	private int dailyLeftTimes;
	private int position;
	private int totalMoney;
	private List<Integer> speed;
	private List<RaffleRewardEntry> rewardEntry;

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(userTimes);// 使用次数
		buff.writeInt(dailyLeftTimes);// 每天的剩余次数
		buff.writeInt(position);// 当前位置
		buff.writeInt(totalMoney);// 累计钻石
		// 种子
		buff.writeInt(speed.size());
		for (int i = 0; i < speed.size(); i++) {
			int step = speed.get(i);
			buff.writeInt(step);
		}
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
	}

	@Override
	public short getType() {
		return RafflePacketSet.C_RaffleGetInfo;
	}

	public int getDailyLeftTimes() {
		return dailyLeftTimes;
	}

	public void setDailyLeftTimes(int dailyLeftTimes) {
		this.dailyLeftTimes = dailyLeftTimes;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(int totalMoney) {
		this.totalMoney = totalMoney;
	}

	public List<Integer> getSpeed() {
		return speed;
	}

	public void setSpeed(List<Integer> speed) {
		this.speed = speed;
	}

	public List<RaffleRewardEntry> getRewardEntry() {
		return rewardEntry;
	}

	public void setRewardEntry(List<RaffleRewardEntry> rewardEntry) {
		this.rewardEntry = rewardEntry;
	}

	public int getUserTimes() {
		return userTimes;
	}

	public void setUserTimes(int userTimes) {
		this.userTimes = userTimes;
	}

}
