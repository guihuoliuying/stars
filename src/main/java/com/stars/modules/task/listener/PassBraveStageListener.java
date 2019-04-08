package com.stars.modules.task.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.scene.event.PassBraveStageEvent;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.TaskModule;

public class PassBraveStageListener extends AbstractEventListener<Module> {
	
	public PassBraveStageListener(Module module){
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		// TODO Auto-generated method stub
		PassBraveStageEvent pse = (PassBraveStageEvent)event;
		TaskModule tm = (TaskModule) this.module();
		tm.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_PassBraveStage, String.valueOf(pse.getStageId())),1,false);
	}

}
