package com.stars.modules.task.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.task.TaskModule;
import com.stars.modules.task.TaskPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerSubmitTask extends PlayerPacket {
	
	private int taskId;
	
	public ServerSubmitTask(){
		
	}

	@Override
	public void execPacket(Player player) {
		((TaskModule) module(MConst.Task)).submitTask(taskId);
	}
	
	@Override
    public void readFromBuffer(NewByteBuffer buff) {
		this.taskId = buff.readInt();
    }

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return TaskPacketSet.Server_Task_Submit;
	}

}
