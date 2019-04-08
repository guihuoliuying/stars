package com.stars.modules.vip.packet;

import com.stars.modules.vip.VipPacketSet;
import com.stars.modules.vip.prodata.ChargeVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.Collection;
import java.util.Map;

public class ClientChargeData extends Packet {

	private Map<Integer, ChargeVo>chargeMap;
	
	public ClientChargeData(Map<Integer, ChargeVo>map){
		this.chargeMap = map;
	}
	public ClientChargeData(){
		
	}
	@Override
	public short getType() {
		return VipPacketSet.C_CHARGEDATA;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		int size = 0;
		if (chargeMap != null) {
			size = chargeMap.size();
		}
		buff.writeInt(size);
		if (size > 0) {
			Collection<ChargeVo>col = chargeMap.values();
			for (ChargeVo chargeVo : col) {
//				private int chargeId;// '唯一标识'
//			    private String channel;// '渠道'
//			    private int reqRmb;// '额度'
//			    private String onceAward;// '首次额外赠送'
//			    private String award;// '奖励'
//			    private String image;// '图片'
				buff.writeInt(chargeVo.getChargeId());
				buff.writeInt(chargeVo.getReqRmb());
				buff.writeString(chargeVo.getOnceAward());
				buff.writeString(chargeVo.getAward());
				buff.writeString(chargeVo.getImage());
				buff.writeString(chargeVo.getIosChargeId());
				buff.writeString(chargeVo.getName());
				buff.writeString(chargeVo.getDescribe());
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

}
