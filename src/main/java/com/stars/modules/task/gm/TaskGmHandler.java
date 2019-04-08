package com.stars.modules.task.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.task.TaskModule;
import com.stars.modules.task.packet.ClientTaskList;

import java.util.Map;

/**
 * 任务gm命令,提交任务并领取奖励
 * 1.只会执行任务Id小于等于输入的主线任务
 */
public class TaskGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        if (args[0].equals("0")) {
        	//接取任务
        	int taskId = Integer.parseInt(args[1]);
        	TaskModule tm = (TaskModule)moduleMap.get(MConst.Task);
        	ClientTaskList ct = new ClientTaskList();
        	ct.addCanAccept(taskId);
        	ct.setBravePractiseCount(tm.getBravePractiseCount());
        	tm.send(ct);
        	tm.acceptTask(taskId);
		}else if (args[0].equals("1")) {
			//完成任务
        	TaskModule tm = (TaskModule)moduleMap.get(MConst.Task);
        	tm.submitTask(tm.getRoleAcceptTask(Integer.parseInt(args[1])), false);
		}
    }
}
