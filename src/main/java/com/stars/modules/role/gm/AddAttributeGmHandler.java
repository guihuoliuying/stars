package com.stars.modules.role.gm;

import com.stars.core.attr.Attribute;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;

import java.util.Map;

/**
 * Created by huwenjun on 2017/9/14.
 */
public class AddAttributeGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        String arg = args[0];
        Attribute attribute = new Attribute(arg);
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        Role roleRow = roleModule.getRoleRow();
        Map<String, Attribute> attrMap = roleRow.getAttrMap();
        Attribute gmAttribute = attrMap.get("gm");
        if (gmAttribute != null) {
            attribute.addAttribute(gmAttribute);
        }
        roleModule.updatePartAttr("gm", attribute);
        roleModule.onSyncData();
        roleModule.warn("属性修改成功");
    }
}
