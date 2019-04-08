package com.stars.modules.family.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-03-06 10:48
 */
public class FamilyEmailGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        String text = args[0];
        FamilyModule family = (FamilyModule) moduleMap.get(MConst.Family);
        family.sendEmailToMember(text);
    }
}
