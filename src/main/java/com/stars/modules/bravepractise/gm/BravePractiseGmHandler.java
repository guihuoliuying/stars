package com.stars.modules.bravepractise.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.bravepractise.BravePractiseModule;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.TaskModule;

import java.util.Map;

/**
 * Created by gaopeidian on 2016/11/17.
 */
public class BravePractiseGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        BravePractiseModule braceBravePractiseModule = (BravePractiseModule) moduleMap.get(MConst.BravePractise);
        TaskModule taskModule = (TaskModule) moduleMap.get(MConst.Task);
        switch (args[0]) {
            case "join":
            {
            	braceBravePractiseModule.joinBravePractise();
                break;
            } 
            case "sendInfo":
            {
            	braceBravePractiseModule.sendBravePageInfo();
                break;
            } 
            case "submit":
            {
            	int taskId = Integer.parseInt(args[1]);
            	taskModule.submitTask(taskId);
                break;
            }
            case "passdungeon":
            {
            	int stageid = Integer.parseInt(args[1]);
            	taskModule.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_PassGuanKa, String.valueOf(stageid)),1,false);
                break;
            }
        }
    }

}