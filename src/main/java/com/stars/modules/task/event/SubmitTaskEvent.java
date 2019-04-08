package com.stars.modules.task.event;

import com.stars.core.event.Event;

public class SubmitTaskEvent extends Event {
	private int taskId;
	
	public SubmitTaskEvent(int taskId){
		this.taskId = taskId;
	}

	public int getTaskId() {
		return taskId;
	}
}
