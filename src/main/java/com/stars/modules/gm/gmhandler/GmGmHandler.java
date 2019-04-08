package com.stars.modules.gm.gmhandler;

import com.stars.core.gmpacket.tester.GmTester;
import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/1/10.
 */
public class GmGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        int index = Integer.parseInt(args[0]);
        String[] params = null;
        if (args.length > 1) {
            params = new String[args.length-1];
            System.arraycopy(args, 1, params, 0, params.length);
        }
        if (params == null) {
            GmTester.test(index);
        } else {
            GmTester.test(index, params);
        }
    }
}
