package com.stars.modules.skill.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.skill.SkillModule;

import java.util.Map;

/**
 * Created by chenkeyu on 2017/1/17 20:06
 */
public class upRoleSkilllvGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        SkillModule module = (SkillModule) moduleMap.get(MConst.Skill);
        if (args != null) {
            module.upRoleSkill(Integer.parseInt(args[0]));
        } else {
            module.upAllRoleSkillLv();
        }
    }
}
