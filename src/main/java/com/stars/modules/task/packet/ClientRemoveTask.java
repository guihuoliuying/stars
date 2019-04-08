package com.stars.modules.task.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.task.TaskPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

public class ClientRemoveTask extends PlayerPacket {
	public static final byte Flag_Finish_Task = 0;
	public static final byte Flag_Delete_Task = 1;
	
	private int taskId;
	byte flag;
	public ClientRemoveTask(){}
	public ClientRemoveTask(int tId , byte flag){
		this.taskId = tId;
		this.flag = flag;
	}

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return TaskPacketSet.Client_Task_Remove;
	}
	
	@Override
    public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(taskId);
		buff.writeByte(flag);//标记，0为完成任务，1为删除任务
    }
}
