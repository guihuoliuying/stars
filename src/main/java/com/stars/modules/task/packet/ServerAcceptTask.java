package com.stars.modules.task.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.task.TaskModule;
import com.stars.modules.task.TaskPacketSet;
import com.stars.modules.task.userdata.RoleAcceptTask;
import com.stars.network.server.buffer.NewByteBuffer;

public class ServerAcceptTask extends PlayerPacket {
	
	private int taskId;

	@Override
	public void execPacket(Player player) {
		// TODO Auto-generated method stub
		TaskModule tm = (TaskModule)this.module(MConst.Task);
//		if (!tm.getCanAcceptTaskList().contains(taskId)) {
//			return;
//		}
		if (tm.getCanAcceptTaskTable().getCanAcceptTaskRaw(taskId) == null) {
			return;
		}
		RoleAcceptTask ratb = tm.acceptTask(taskId);
		if (ratb != null) {
			PlayerUtil.send(getRoleId(), new ClientTaskProcess(ratb.getTaskId(), ratb.getProcess(),ratb.getState()));
		}
	}

	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return TaskPacketSet.Client_Task_Accept;
	}

	
	@Override
    public void readFromBuffer(NewByteBuffer buff) {
		this.taskId = buff.readInt();
    }
}
