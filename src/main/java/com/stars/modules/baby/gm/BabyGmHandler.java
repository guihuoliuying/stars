package com.stars.modules.baby.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.baby.BabyModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by huwenjun on 2017/8/24.
 */
public class BabyGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        BabyModule babyModule = (BabyModule) moduleMap.get(MConst.Baby);
        String arg = args[0];
        String[] group = arg.split("=");
        String action = group[0];
        switch (action) {
            case "active": {
                int fashionId = Integer.parseInt(group[1]);
                babyModule.activeFashion(fashionId);
            }
            break;
        }
    }
}
