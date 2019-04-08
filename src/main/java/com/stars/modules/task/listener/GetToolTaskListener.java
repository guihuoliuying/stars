package com.stars.modules.task.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.TaskModule;
import com.stars.modules.tool.event.AddToolEvent;

import java.util.Iterator;
import java.util.Map;

public class GetToolTaskListener extends AbstractEventListener<Module> {
	
	public GetToolTaskListener(Module tm){
		super(tm);
	}

	@Override
	public void onEvent(Event event) {
		// TODO Auto-generated method stub
		AddToolEvent ate = (AddToolEvent)event;
		Map<Integer,Integer> toolMap = ate.getToolMap();
		if (toolMap == null || toolMap.size() <= 0) {
			return;
		}
		Iterator<Integer> it = toolMap.keySet().iterator();
		TaskModule tm = (TaskModule)this.module();
		while (it.hasNext()) {
			int id = it.next();
			int toolCount = toolMap.get(id);
			tm.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_CollectTool, 
					String.valueOf(id)),toolCount,false);
			tm.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_CollectTool_NotDel,
					String.valueOf(id)),toolCount,false);
		}
	}

}
