package com.stars.modules.task;

import com.stars.modules.task.userdata.RoleAcceptTask;

public interface TaskInterface {
	/**
	 * 完成任务
	 * @param ratb
	 * @param login
	 * @return
	 */
	public boolean submitTask(RoleAcceptTask ratb, boolean login);
	/**
	 * 自动接取任务
	 * @param login
	 */
	public void autoCheckAllTaskBySort(boolean login);
	/**
	 * 日常重置
	 */
	public void clearTaskOndaily();
	
}
