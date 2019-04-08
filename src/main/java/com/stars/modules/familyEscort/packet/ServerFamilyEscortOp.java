package com.stars.modules.familyEscort.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.familyEscort.FamilyEscortModule;
import com.stars.modules.familyEscort.FamilyEscortPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerFamilyEscortOp extends PlayerPacket {
	
	private String id;
	
	private byte opType;
	
	public ServerFamilyEscortOp(){
	}

	@Override
	public short getType() {
		return FamilyEscortPacketSet.S_ESCORT_OP;
	}

	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		opType = buff.readByte();
		if (opType == 2) {//参加劫镖
			id = buff.readString();//家族ID
		}else if (opType == 3) {//劫镖
			id = buff.readString();//被劫的镖ID
		}else if(opType == 7){
			id = buff.readString();//被杀的劫镖者
		}
	}

	@Override
	public void execPacket() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void execPacket(Player player) {
		FamilyEscortModule fEscortModule = module(MConst.FamilyActEscort);
		if (opType == 0) {//参加运镖
			fEscortModule.joinActEscort();
		}else if (opType == 1) {//运镖
			fEscortModule.actEscort();
		}else if (opType == 2) {//参加劫镖
			fEscortModule.joinLootEscort(id);
		}else if (opType == 3) {//劫镖
			fEscortModule.lootEscort(id);
		}else if (opType == 4) {//解除障碍物
			fEscortModule.removeEscortBarrier();
		}else if (opType == 5) {//打开主运镖界面
			fEscortModule.openEscortUI();
		}else if (opType == 6) {//打开劫镖界面
			fEscortModule.showEscortList();
		}else if (opType == 7) {//杀人
			fEscortModule.killRole(id);
		}else if (opType == 8) {//战斗结果确认
			fEscortModule.confirmFightResult("");
		}
	}

}
