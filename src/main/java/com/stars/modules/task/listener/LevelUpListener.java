package com.stars.modules.task.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.TaskModule;

public class LevelUpListener extends AbstractEventListener<Module> {
	public LevelUpListener(Module module) {
		super(module);
	}

	@Override
	public void onEvent(Event event) {
		RoleLevelUpEvent levent = (RoleLevelUpEvent) event;
		if (levent.getPreLevel() == levent.getNewLevel()) {
			return;
		}
		TaskModule tm = (TaskModule) this.module();
		
		//检查升级后是否需要添加主线任务/支线任务
		tm.checkAllTaskBySort(TaskManager.Task_Sort_ZhuXian,false);
		tm.checkAllTaskBySort(TaskManager.Task_Sort_Daily,false);
		tm.checkAllTaskBySort(TaskManager.Task_Sort_AutoDaily, false);
		for (int level = levent.getPreLevel() + 1; level <= levent.getNewLevel(); level++) {
			tm.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_UpLevel, "lv"),
					1,false);
		}
		tm.flushNewTask2Client();
	}

}
