package com.stars.modules.skill.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.skill.SkillModule;

/**
 * Created by huwenjun on 2017/6/8.
 */
public class SkillChangeJobListenner extends AbstractEventListener<SkillModule> {
    public SkillChangeJobListenner(SkillModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
    }
}
