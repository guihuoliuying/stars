package com.stars.services.family.role;

import com.stars.services.family.role.userdata.FamilyRoleApplicationPo;
import com.stars.services.family.role.userdata.FamilyRolePo;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/24.
 */
public class FamilyRoleData {

    private FamilyRolePo rolePo;
    private Map<Long, FamilyRoleApplicationPo> applicationPoMap;

    public FamilyRoleData() {
    }

    public FamilyRoleData(FamilyRolePo rolePo, Map<Long, FamilyRoleApplicationPo> applicationPoMap) {
        this.rolePo = rolePo;
        this.applicationPoMap = applicationPoMap;
    }

    public FamilyRolePo getRolePo() {
        return rolePo;
    }

    public Map<Long, FamilyRoleApplicationPo> getApplicationPoMap() {
        return applicationPoMap;
    }

}
