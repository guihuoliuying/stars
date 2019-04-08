package com.stars.modules.skill.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.skill.SkillModule;

/**
 * Created by zhanghaizhen on 2017/6/12.
 */
public class TokenLevelChangeListener extends AbstractEventListener<SkillModule> {
    public TokenLevelChangeListener(SkillModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        module().upAllRoleSkill();
    }
}
