package com.stars.modules.familyTask.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.family.event.FamilyLeaveEvent;
import com.stars.modules.familyTask.FamilyTaskModule;
import com.stars.modules.familyTask.event.FamilyTaskEvent;
import com.stars.modules.task.event.SubmitTaskEvent;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.productdata.ItemVo;

import java.util.Map;

public class FamilyTaskListener extends AbstractEventListener<Module> {

	private FamilyTaskModule familyTaskModule;
	
	public FamilyTaskListener(Module module) {
		super(module);
		// TODO Auto-generated constructor stub
		familyTaskModule = (FamilyTaskModule)module;
	}
	
	@Override
	public void onEvent(Event event) {
		if(event instanceof FamilyLeaveEvent){
			familyTaskModule.leaveFamilyHandle(((FamilyLeaveEvent)event).getFamilyId());
		}else if(event instanceof FamilyTaskEvent){
			FamilyTaskEvent familyTaskEvent = ((FamilyTaskEvent)event);
			byte opType = familyTaskEvent.getOpType();
			if(opType==FamilyTaskEvent.SEND_HELP_LIST){				
				familyTaskModule.sendSeekHelpList(familyTaskEvent.getList());
			}else if(opType==FamilyTaskEvent.BE_HELP_COMMIT){
				familyTaskModule.beHelpCommitAward(familyTaskEvent.getTaskId(), familyTaskEvent.getName());
			}else if(opType==FamilyTaskEvent.FIX_STATE){
				familyTaskModule.fixTaskState(familyTaskEvent.getTaskId());
			}
		}else if(event instanceof SubmitTaskEvent){
//			SubmitTaskEvent subEvent = (SubmitTaskEvent)event;
//			TaskVo taskVo = TaskManager.getTaskById(subEvent.getTaskId());
//	    	if (taskVo != null && taskVo.getSort() == TaskManager.Task_Sort_Family) {
//	    		familyTaskModule.addDailyTaskTimes();
//			}
		}else if(event instanceof AddToolEvent){
			AddToolEvent addTollEvent = (AddToolEvent)event;
			Map<Integer, Integer> toolMap = addTollEvent.getToolMap();
			ItemVo itemVo = null;
			boolean checkRedPoint = false;
			for(Integer itemId : toolMap.keySet()){
				itemVo = ToolManager.getItemVo(itemId);
				if(itemVo.getType()==ToolManager.TYPE_FAMILY_TASK){
					checkRedPoint = true;
					break;
				}
			}
			if(checkRedPoint){
				familyTaskModule.setCheckCommit();
			}
		}
	}

}
