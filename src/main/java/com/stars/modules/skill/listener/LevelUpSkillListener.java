package com.stars.modules.skill.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.book.event.BookActiveEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.skill.SkillModule;
import com.stars.modules.tool.event.AddToolEvent;

/**
 * 监听玩家升级 更新被动技能效果
 * Created by daiyaorong on 2016/8/9.
 */
public class LevelUpSkillListener extends AbstractEventListener<Module> {
    public LevelUpSkillListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof RoleLevelUpEvent) {
            RoleLevelUpEvent levent = (RoleLevelUpEvent) event;
            if (levent.getPreLevel() == levent.getNewLevel()) {
                return;
            }
            SkillModule skillModule = (SkillModule) this.module();
            skillModule.upAllRoleSkill();
            skillModule.signCalRedPoint(MConst.Skill, RedPointConst.SKILL_LVUP);
        } else if (event instanceof AddToolEvent) {
            SkillModule skillModule = (SkillModule) this.module();
            skillModule.signCalRedPoint(MConst.Skill, RedPointConst.SKILL_LVUP);
        }else if (event instanceof BookActiveEvent) {
            SkillModule skillModule = (SkillModule) this.module();
            skillModule.signCalRedPoint(MConst.Skill, RedPointConst.SKILL_LVUP);
        }
    }
}
