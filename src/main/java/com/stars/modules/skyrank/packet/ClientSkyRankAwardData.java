package com.stars.modules.skyrank.packet;

import com.stars.modules.skyrank.SkyRankPacketSet;
import com.stars.modules.skyrank.prodata.SkyRankAwardVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.List;

/**
 * 
 * 天梯奖励集合
 * @author xieyuejun
 *
 */
public class ClientSkyRankAwardData extends Packet {

	private List<SkyRankAwardVo> awardList;
	
	@Override
	public short getType() {
		return SkyRankPacketSet.ClientSkyRankAwardData;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		int size = awardList== null?0:awardList.size();
		buff.writeInt(size);
		if(size >0){
			for(SkyRankAwardVo awardVo:awardList){
				awardVo.writeBuffer(buff);
			}
		}
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub
		
	}

	public List<SkyRankAwardVo> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<SkyRankAwardVo> awardList) {
		this.awardList = awardList;
	}
	
}
