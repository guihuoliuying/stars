package com.stars.modules.email.gm;

import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/3.
 */
public class ReadEmailGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException();
        }
        switch (args[0]) {
            case "self": // 自己的邮件
                ServiceHelper.emailService().read(roleId, Integer.parseInt(args[1]));
                break;
            default:
                ServiceHelper.emailService().read(Long.parseLong(args[0]), Integer.parseInt(args[1]));
                break;
        }
    }
}
