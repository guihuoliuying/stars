package com.stars.modules.gm.gmhandler;

import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceUtil;
import com.stars.util.I18n;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/11/14.
 */
public class I18nGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        ServiceUtil.sendText(roleId, I18n.get(args[0]));
    }
}
