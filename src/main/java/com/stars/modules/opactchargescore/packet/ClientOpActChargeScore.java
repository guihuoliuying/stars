package com.stars.modules.opactchargescore.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.opactchargescore.OpActChargeScorePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.rank.userdata.OpActChargeRankPo;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by likang
 */
public class ClientOpActChargeScore extends PlayerPacket {
	private static final long serialVersionUID = 5794336641067638636L;

	private Map<String, OpActChargeRankPo> rankPoMap;
	private OpActChargeRankPo selfRank;

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		int size = rankPoMap != null ? rankPoMap.entrySet().size() : 0;
		buff.writeInt(size);
		if (rankPoMap != null) {
			for (Entry<String, OpActChargeRankPo> entry : rankPoMap.entrySet()) {
				OpActChargeRankPo po = entry.getValue();
				po.writeToBuffer(buff);
			}
		}
		buff.writeInt(selfRank != null ? selfRank.getRank() : 0);
		buff.writeInt(selfRank != null ? selfRank.getTotalCharge() : 0);
	}
	@Override
	public void execPacket(Player player) {

	}

	@Override
	public short getType() {
		return OpActChargeScorePacketSet.C_OpActChargeScore;
	}

	public Map<String, OpActChargeRankPo> getRankPoMap() {
		return rankPoMap;
	}

	public void setRankPoMap(Map<String, OpActChargeRankPo> rankPoMap) {
		this.rankPoMap = rankPoMap;
	}

	public OpActChargeRankPo getSelfRank() {
		return selfRank;
	}

	public void setSelfRank(OpActChargeRankPo selfRank) {
		this.selfRank = selfRank;
	}

}
