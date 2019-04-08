package com.stars.modules.task.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.scene.event.TalkWithNpcEvent;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.TaskModule;

public class TalkWithNpcListener extends AbstractEventListener<Module> {
	public TalkWithNpcListener(Module tm){
		super(tm);
	}
	@Override
	public void onEvent(Event event) {
		// TODO Auto-generated method stub
		TalkWithNpcEvent tne = (TalkWithNpcEvent)event;
		TaskModule tm = (TaskModule)this.module();
		tm.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_TalkWithNpc, 
				String.valueOf(tne.getNpcId())), 1,false);
	}
}
