package com.stars.modules.email.gm;

import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/3.
 */
public class RecvEmailGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException();
        }
        switch (args[0]) {
            case "self": // 自己的邮件
                ServiceHelper.emailService().sendMailListToRole(roleId);
                break;
            default:
                ServiceHelper.emailService().sendMailListToRole(Long.parseLong(args[0]));
                break;
        }
    }
}
