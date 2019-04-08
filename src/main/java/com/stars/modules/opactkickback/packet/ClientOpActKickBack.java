package com.stars.modules.opactkickback.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.opactkickback.OpActKickBackPacketSet;
import com.stars.modules.opactkickback.userdata.ConsumGateDefineCatalog;
import com.stars.modules.opactkickback.userdata.ConsumeGateDefine;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientOpActKickBack extends PlayerPacket {

	/**
	 * create by likang
	 */

	private static final long serialVersionUID = 5813916408932307000L;
	private int consume;
	private Set<Integer> hasGet;

	@Override
	public void execPacket(Player player) {

	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		ConsumeGateDefine[] defines = ConsumGateDefineCatalog.instance.getGateDefines();
		int size = defines != null ? defines.length : 0;
		buff.writeInt(size);
		if (defines != null) {
			for (ConsumeGateDefine define : defines) {
				buff.writeInt(define.getDropId());
				buff.writeInt(define.getConsume());
				int state = !hasGet.contains(define.getId()) ? 1 : 2;
				buff.writeInt(state);
			}
		}
		buff.writeInt(consume);
	}
	@Override
	public short getType() {
		return OpActKickBackPacketSet.C_OpActKickBack;
	}

	public int getConsume() {
		return consume;
	}

	public void setConsume(int consume) {
		this.consume = consume;
	}

	public Set<Integer> getHasGet() {
		return hasGet;
	}

	public void setHashGet(List<Integer> hasGetList) {
		Set<Integer> set = new HashSet<Integer>();
		set.addAll(hasGetList);
		this.hasGet = set;
	}

}
