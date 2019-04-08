package com.stars.modules.bravepractise.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.bravepractise.BravePractiseModule;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.event.SubmitTaskEvent;
import com.stars.modules.task.prodata.TaskVo;

public class BraveListener extends AbstractEventListener<Module> {
	public BraveListener(Module module) {
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		SubmitTaskEvent subEvent = (SubmitTaskEvent)event;
		BravePractiseModule bpModule = (BravePractiseModule)module();
		TaskVo taskVo = TaskManager.getTaskById(subEvent.getTaskId());
    	if (taskVo != null && taskVo.getSort() == TaskManager.Task_Sort_HuoDong) {
    		bpModule.submitTask(subEvent.getTaskId());
		}
	}
}
