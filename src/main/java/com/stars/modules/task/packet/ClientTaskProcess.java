package com.stars.modules.task.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.task.TaskPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * @author dengzhou
 * @用于已接任务&可接任务有更新的时候|可接任务变成已接任务的时候 更新到客户端
 *
 */
public class ClientTaskProcess extends PlayerPacket {

	private static final long serialVersionUID = 1L;
	
	private int taskId;
	private int process;
	private byte state;
	public ClientTaskProcess(){}
	public ClientTaskProcess(int tId,int pro,byte state){
		this.taskId = tId;
		this.process = pro;
		this.state = state;
	}
	
	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public short getType() {
		return TaskPacketSet.Client_Task_Process;
	}
	
	@Override
    public void writeToBuffer(NewByteBuffer buff) {
		buff.writeInt(taskId);
		buff.writeInt(process);
		buff.writeByte(state);
	}

}
