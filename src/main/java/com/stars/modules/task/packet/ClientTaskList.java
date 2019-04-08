package com.stars.modules.task.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.bravepractise.BravePractiseManager;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.TaskPacketSet;
import com.stars.modules.task.prodata.TaskVo;
import com.stars.modules.task.userdata.RoleAcceptTask;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.LinkedList;

/**
 * @author dengzhou
 * @用于登录的时候发送数据到客户端|任务从不可接变成已可接的时候发到客户端
 *
 */
public class ClientTaskList extends PlayerPacket {

	private static final long serialVersionUID = 1L;


	LinkedList<Integer> canAcceptList;

	LinkedList<RoleAcceptTask>acceptList;

	private int bravePractiseCount = 0;


	public ClientTaskList(){

	}

	public ClientTaskList addCanAccept(int taskId){
		if (canAcceptList == null) {
			canAcceptList = new LinkedList<Integer>();
		}
		canAcceptList.add(taskId);
		return this;
	}

	public ClientTaskList addAccpt(RoleAcceptTask rat){
		if (acceptList == null) {
			acceptList = new LinkedList<RoleAcceptTask>();
		}
		acceptList.add(rat);
		return this;
	}

	public void setBravePractiseCount(int count){
		bravePractiseCount = count;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		int size = acceptList == null?0:acceptList.size();
		size = size + (canAcceptList == null?0:canAcceptList.size());
		buff.writeByte((byte)size);
		if (size == 0) {
			return;
		}
		TaskVo tv = null;
		if (acceptList != null && acceptList.size() > 0) {
			for (RoleAcceptTask rat : acceptList) {
				tv = TaskManager.getTaskById(rat.getTaskId());
				writeTaskVo(buff,tv);
				if (rat.getProcess() >= tv.getTargetCount()) {
					buff.writeByte(TaskManager.Task_State_CanSubmit);//已完成
				}else {
					buff.writeByte(TaskManager.Task_State_Accept);//已接受但未完成
				}

				if (tv.getSort() == TaskManager.Task_Sort_HuoDong) {//勇者试炼的任务，这里需要的是试炼次数，所以特殊处理
					buff.writeInt(bravePractiseCount);
				}else{
					buff.writeInt(rat.getProcess());
				}
			}
		}
		if (canAcceptList != null && canAcceptList.size() > 0) {
			for (int taskId : canAcceptList) {
				tv = TaskManager.getTaskById(taskId);
				writeTaskVo(buff,tv);
				buff.writeByte(TaskManager.Task_State_CanAccept);

				if (tv.getSort() == TaskManager.Task_Sort_HuoDong) {//勇者试炼的任务，这里需要的是试炼次数，所以特殊处理
					buff.writeInt(bravePractiseCount);
				}else{
					buff.writeInt(0);
				}
			}
		}

	}

	private void writeTaskVo(NewByteBuffer buff, TaskVo tv){
		buff.writeInt(tv.getId());
		buff.writeString(tv.getName());
		buff.writeString(tv.getIcon());
		buff.writeString(tv.getDesc());
		buff.writeByte(tv.getSort());
		buff.writeInt(tv.getAcceptnpc());
		buff.writeString(tv.getAccepttalk());
		buff.writeInt(tv.getSubmitnpc());
		buff.writeString(tv.getSubmittalk());
		buff.writeByte(tv.getType());
		buff.writeString(tv.getTarget());
		if (tv.getSort() == TaskManager.Task_Sort_HuoDong) {//勇者试炼的任务，这里需要的是总试炼次数，所以特殊处理
			buff.writeInt(BravePractiseManager.bravePractiseCount);
		}else{
			buff.writeInt(tv.getTargetCount());
		}
		buff.writeString(tv.getAward());
		buff.writeInt(tv.getPrior());
		buff.writeString(tv.getDrama());
		buff.writeString(tv.getCg());
		buff.writeString(tv.getCompleteDesc());
	}

	@Override
	public void execPacket(Player player) {

	}

	@Override
	public short getType() {
		return TaskPacketSet.Client_Task_List;
	}

}
