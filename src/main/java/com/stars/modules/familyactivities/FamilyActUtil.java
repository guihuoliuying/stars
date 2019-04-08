package com.stars.modules.familyactivities;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.services.family.FamilyAuth;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/10/8.
 */
public class FamilyActUtil {

    public static final FamilyAuth getAuth(Map<String, Module> moduleMap) {
        FamilyModule module = (FamilyModule) moduleMap.get(MConst.Family);
        return module.getAuth();
    }

    public static final boolean hasFamily(FamilyAuth auth) {
        if (auth != null && auth.getFamilyId() > 0) {
            return true;
        }
        return false;
    }

    public static final boolean hasFamily(Map<String, Module> moduleMap) {
        return hasFamily(getAuth(moduleMap));
    }

}
