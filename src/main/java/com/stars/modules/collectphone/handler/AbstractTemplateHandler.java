package com.stars.modules.collectphone.handler;

import com.stars.core.module.Module;
import com.stars.modules.collectphone.usrdata.RoleCollectPhone;

import java.util.Map;

/**
 * Created by huwenjun on 2017/9/20.
 */
public abstract class AbstractTemplateHandler {
    private RoleCollectPhone roleCollectPhone;
    private int step;
    private Map<String, Module> moduleMap;

    public AbstractTemplateHandler(int step, RoleCollectPhone roleCollectPhone, Map<String, Module> moduleMap) {
        this.roleCollectPhone = roleCollectPhone;
        this.step = step;
        this.moduleMap = moduleMap;
    }

    public abstract void submit(String answer);

    public abstract boolean isOver();

    public RoleCollectPhone getRoleCollectPhone() {
        return roleCollectPhone;
    }

    public int getStep() {
        return step;
    }

    public Map<String, Module> getModuleMap() {
        return moduleMap;
    }
}
