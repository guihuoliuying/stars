package com.stars.modules.skill.listener;

import com.stars.core.event.Event;
import com.stars.core.event.EventListener;
import com.stars.modules.MConst;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.skill.SkillModule;

/**
 * Created by chenkeyu on 2016/12/20.
 */
public class UseToolListener implements EventListener {
    private SkillModule module;
    public UseToolListener(SkillModule module){
        this.module = module;
    }
    @Override
    public void onEvent(Event event) {
        module.signCalRedPoint(MConst.Skill, RedPointConst.SKILL_LVUP);
    }
}
