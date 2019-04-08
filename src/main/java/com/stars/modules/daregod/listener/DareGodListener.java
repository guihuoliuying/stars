package com.stars.modules.daregod.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.daregod.DareGodModule;
import com.stars.modules.daregod.event.DareGodEnterFightEvent;
import com.stars.modules.daregod.event.DareGodGetAwardEvent;
import com.stars.modules.fashion.event.FashionChangeEvent;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.role.event.FightScoreChangeEvent;

/**
 * Created by chenkeyu on 2017-08-24.
 */
public class DareGodListener extends AbstractEventListener {
    public DareGodListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        DareGodModule dareGodModule = (DareGodModule) module();
        if (event instanceof DareGodGetAwardEvent) {
            dareGodModule.addAward(((DareGodGetAwardEvent) event).getItemMap());
        }
        if (event instanceof DareGodEnterFightEvent) {
            DareGodEnterFightEvent enterFightEvent = (DareGodEnterFightEvent) event;
            dareGodModule.enterFight(enterFightEvent.getStageId(), enterFightEvent.getFightType(), enterFightEvent.getMonsterId());
        }
        if (event instanceof FightScoreChangeEvent) {
            FightScoreChangeEvent changeEvent = (FightScoreChangeEvent) event;
            dareGodModule.changeFightScore(changeEvent.getNewFightScore());
        }
        if (event instanceof RoleRenameEvent) {
            RoleRenameEvent renameEvent = (RoleRenameEvent) event;
            dareGodModule.changeName(renameEvent.getNewName());
        }
        if (event instanceof FashionChangeEvent) {
            FashionChangeEvent changeEvent = (FashionChangeEvent) event;
            dareGodModule.changeFashion(changeEvent.getCurFashionId());
        }
        if (event instanceof ChangeJobEvent) {
            ChangeJobEvent jobEvent = (ChangeJobEvent) event;
            dareGodModule.changeJob(jobEvent.getNewJobId());
        }
    }
}
