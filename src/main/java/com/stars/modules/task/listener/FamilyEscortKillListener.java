package com.stars.modules.task.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.familyEscort.event.FamilyEscortFightEvent;
import com.stars.modules.familyEscort.event.FamilyEscortKillEvent;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.TaskModule;

public class FamilyEscortKillListener implements EventListener {
	
	private TaskModule module;
	
	public FamilyEscortKillListener(TaskModule module) {
        this.module = module;
    }
	
	@Override
	public void onEvent(Event event) {
		if(event instanceof FamilyEscortFightEvent){
			module.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_FamEscortKill, "fight"), 1, false);
		}
		if(event instanceof FamilyEscortKillEvent){
			module.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_FamEscortKill, "kill"), 1, false);
		}
	}
}
