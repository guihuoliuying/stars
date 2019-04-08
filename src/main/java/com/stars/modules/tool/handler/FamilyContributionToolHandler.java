package com.stars.modules.tool.handler;

import com.stars.core.SystemRecordMap;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.tool.userdata.RoleToolRow;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/13.
 */
public class FamilyContributionToolHandler implements ToolHandler {

    private long roleId;

    @Override
    public void updateToolRow(RoleToolRow toolRow) {

    }

    private Map<String, Module> moduleMap;

    public FamilyContributionToolHandler(long roleId, Map<String, Module> moduleMap) {
        this.roleId = roleId;
        this.moduleMap = moduleMap;
    }

    @Override
    public Map<Integer,Integer> add(int itemId, int count,short eventType) {
        ServiceHelper.familyRoleService().addAndSendContribution(roleId, count);
        if (count > 0) {
            FamilyAuth auth = ((FamilyModule) moduleMap.get(MConst.Family)).getAuth();
            if (auth.getFamilyId() > 0) {
                ServiceHelper.familyMainService().addMoneyAndUpdateContribution(
                        auth, roleId, 0, count, SystemRecordMap.dateVersion, 0);
            }
        }
        Map<Integer,Integer> resultMap = new HashMap<>();
        resultMap.put(itemId,count);
        return resultMap;
    }

    @Override
    public boolean deleteByItemId(int itemId, int count,short eventType) {
        return ServiceHelper.familyRoleService().addAndSendContribution(roleId, -count);
    }

    @Override
    public boolean deleteByToolId(long toolId, int count) {
        return false;
    }

    @Override
    public void add(RoleToolRow toolRow) {

    }

    @Override
    public int getNullGrid() {
        return 0;
    }

    @Override
    public long getCountByItemId(int itemId) {
        return ServiceHelper.familyRoleService().getContribution(roleId);
    }

    @Override
    public int canAdd(int itemId, int count) {
        return count;
    }

    @Override
    public void sort() {

    }

    @Override
    public boolean canAdd(Map<Integer, Integer> toolMap) {
        return true;
    }
}
