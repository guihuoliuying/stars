package com.stars.core.gmpacket.specialaccount;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-03-23 20:59
 */
public class SpecialAccountUtil {
    public static String getAccount(Map<String, Module> moduleMap) {
        LoginModule loginModule = (LoginModule) moduleMap.get(MConst.Login);
        return loginModule.getAccount();
    }
}
