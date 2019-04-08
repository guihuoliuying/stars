package com.stars.modules.gm.gmhandler;

import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;
import com.stars.multiserver.MainRpcHelper;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/11/3.
 */
public class RpcGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        MainRpcHelper.echoService().println(8001, roleId, "你好");
    }

}
