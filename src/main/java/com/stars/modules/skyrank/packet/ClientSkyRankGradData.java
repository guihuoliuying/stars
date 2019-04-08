package com.stars.modules.skyrank.packet;

import com.stars.modules.skyrank.SkyRankPacketSet;
import com.stars.modules.skyrank.prodata.SkyRankGradVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.Map;

/**
 * 
 * 天梯段位产品数据
 * @author xieyuejun
 *
 */
public class ClientSkyRankGradData extends Packet {

	private Map<Integer, SkyRankGradVo> skyRankGradMap;
	
	@Override
	public short getType() {
		return SkyRankPacketSet.ClientSkyRankGradData;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		int size = skyRankGradMap == null ?0:skyRankGradMap.size();
		buff.writeInt(size);
		if(size >0){
			for(SkyRankGradVo gradVo:skyRankGradMap.values()){
				gradVo.writeBuffer(buff);
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

	public Map<Integer, SkyRankGradVo> getSkyRankGradMap() {
		return skyRankGradMap;
	}

	public void setSkyRankGradMap(Map<Integer, SkyRankGradVo> skyRankGradMap) {
		this.skyRankGradMap = skyRankGradMap;
	}
	
}
