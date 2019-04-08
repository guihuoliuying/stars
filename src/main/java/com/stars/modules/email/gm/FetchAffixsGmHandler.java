package com.stars.modules.email.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.email.EmailModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/3.
 */
public class FetchAffixsGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException();
        }
        EmailModule emailModule = (EmailModule) moduleMap.get(MConst.Email);
        emailModule.fetchAffixs(Integer.parseInt(args[0]));
    }
}
