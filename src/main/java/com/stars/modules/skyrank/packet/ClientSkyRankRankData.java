package com.stars.modules.skyrank.packet;

import com.stars.modules.skyrank.SkyRankPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.skyrank.SkyRankShowData;

import java.util.List;

/**
 * 天梯排行榜信息
 * 
 * @author xieyuejun
 *
 */
public class ClientSkyRankRankData extends Packet {

	private List<SkyRankShowData> skyRankList;
	private SkyRankShowData myRank;

	@Override
	public short getType() {
		return SkyRankPacketSet.ClientSkyRankRankData;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		int size = skyRankList == null?0:skyRankList.size();
		buff.writeInt(size);
		if(size >0){
			for(SkyRankShowData showData:skyRankList){
				showData.writeBuffer(buff);
			}
		}
		myRank.writeBuffer(buff);
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub

	}

	public List<SkyRankShowData> getSkyRankList() {
		return skyRankList;
	}

	public void setSkyRankList(List<SkyRankShowData> skyRankList) {
		this.skyRankList = skyRankList;
	}

	public SkyRankShowData getMyRank() {
		return myRank;
	}

	public void setMyRank(SkyRankShowData myRank) {
		this.myRank = myRank;
	}

}
