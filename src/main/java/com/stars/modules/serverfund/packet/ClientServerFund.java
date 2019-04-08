package com.stars.modules.serverfund.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.serverfund.ServerFundManager;
import com.stars.modules.serverfund.ServerFundPacketSet;
import com.stars.modules.serverfund.prodata.ServerFundVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;
import java.util.Map;

public class ClientServerFund extends PlayerPacket {
	
	public final static byte RESP_OPENUI = 1;	//打开ui
	public final static byte RESP_BUY = 2;	//购买成功
	public final static byte RESP_GET = 3;	//领取成功
	
	private byte respType;
	private int fundId;
	private int vipLevel;
	private List<ServerFundVo> fundVoList;
	private Map<Integer, Byte> recordStateMap;
	private byte hasBuy;
	
	public ClientServerFund() {
		// TODO Auto-generated constructor stub
	}
	
	public ClientServerFund(byte respType) {
		this.respType = respType;
	}
	
	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getType() {
		return ServerFundPacketSet.C_SERVER_FUND;
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		buff.writeByte(respType);
		if (respType == RESP_OPENUI) {
			buff.writeByte(hasBuy);//是否已购买；1=是，2=否
			buff.writeInt(ServerFundManager.minVipLevel);//最低vip等级
			buff.writeInt(ServerFundManager.moneyId);//货币id
			buff.writeInt(ServerFundManager.moneyCount);//货币数量
//			buff.writeString(DataManager.getGametext("serverfund_backdesc"));//返还描述
			buff.writeInt(fundVoList.size());//基金数据size
			Byte state;
			for (ServerFundVo fundVo : fundVoList) {
				fundVo.writeToBuff(buff);//基金数据
				state = recordStateMap.get(fundVo.getFundid());
				buff.writeByte(state == null ? ServerFundManager.FUND_STATE_OF_NOTBUY : state.byteValue());//领取状态
			}
		} else if (respType == RESP_BUY) {
			
		} else if (respType == RESP_GET) {
			buff.writeInt(fundId);
		}
	}
	
	public int getFundId() {
		return fundId;
	}

	public void setFundId(int fundId) {
		this.fundId = fundId;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	public Map<Integer, Byte> getRecordStateMap() {
		return recordStateMap;
	}

	public void setRecordStateMap(Map<Integer, Byte> recordStateMap) {
		this.recordStateMap = recordStateMap;
	}

	public List<ServerFundVo> getFundVoList() {
		return fundVoList;
	}

	public void setFundVoList(List<ServerFundVo> fundVoList) {
		this.fundVoList = fundVoList;
	}

	public byte getHasBuy() {
		return hasBuy;
	}

	public void setHasBuy(byte hasBuy) {
		this.hasBuy = hasBuy;
	}

}
