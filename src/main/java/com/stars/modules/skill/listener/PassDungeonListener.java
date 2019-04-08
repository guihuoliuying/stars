package com.stars.modules.skill.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.scene.event.PassStageEvent;
import com.stars.modules.skill.SkillModule;

/**
 * Created by chenkeyu on 2017/1/21 10:44
 */
public class PassDungeonListener extends AbstractEventListener<SkillModule> {
    public PassDungeonListener(SkillModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        PassStageEvent passStageEvent = (PassStageEvent) event;
        module().upAllRoleSkill();
    }
}
