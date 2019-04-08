package com.stars.modules.familyEscort.packet;

import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.services.familyEscort.FamilyEscortMap;
import com.stars.services.familyEscort.FamilyEscortServiceActor;

import java.util.List;

/**
 * 
 * 家族运镖列表
 * @author xieyuejun
 *
 */
public class ClientFamilyEscortList extends Packet {

	private List<FamilyEscortMap> feList;
	
	@Override
	public short getType() {
		return FamilyEscortPacketSet.C_ESCORT_LIST;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		int size = feList == null?0:feList.size();
		size = size > FamilyEscortServiceActor.SHOW_MAX_LIST?FamilyEscortServiceActor.SHOW_MAX_LIST:size;
		buff.writeByte((byte) size);
		if(size >0){
			int i = 0;
			for( FamilyEscortMap fem : feList){
				fem.writeToBuffer(buff);
				i++;
				if(i >= size){
					break;
				}
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

	public List<FamilyEscortMap> getFeList() {
		return feList;
	}

	public void setFeList(List<FamilyEscortMap> feList) {
		this.feList = feList;
	}

}
