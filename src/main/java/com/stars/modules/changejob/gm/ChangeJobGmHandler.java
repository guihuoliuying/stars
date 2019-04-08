package com.stars.modules.changejob.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.changejob.ChangeJobModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by huwenjun on 2017/5/25.
 */
public class ChangeJobGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {

        ChangeJobModule changeJobModule = (ChangeJobModule) moduleMap.get(MConst.ChangeJob);
        String arg = args[0];
        String[] actionStr = arg.split("=");
        String action = actionStr[0];
        switch (action) {
            case "job": {
                String jobIdStr = actionStr[1];
                Integer jobId = Integer.parseInt(jobIdStr);
                changeJobModule.changeJob(jobId);
                changeJobModule.warn("转职：" + jobIdStr);
            }
            break;
            case "activejob": {
                String jobIdStr = actionStr[1];
                Integer jobId = Integer.parseInt(jobIdStr);
                changeJobModule.isActivedJob(jobId);
                changeJobModule.warn("激活职业："+jobIdStr);
            }
            break;
            case "gotoactive":{
                String jobIdStr = actionStr[1];
                Integer jobId = Integer.parseInt(jobIdStr);
                changeJobModule.gotoActiveView(jobId);
            }break;
        }

    }
}
