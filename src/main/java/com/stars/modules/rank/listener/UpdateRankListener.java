package com.stars.modules.rank.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.gamecave.event.FinishAllTinyGameEvent;
import com.stars.modules.rank.RankModule;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.skytower.event.FinishSkyTowerLayerEvent;
import com.stars.services.rank.userdata.RoleRankPo;

/**
 * Created by liuyuheng on 2016/8/25.
 */
public class UpdateRankListener extends AbstractEventListener<RankModule> {
    public UpdateRankListener(RankModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        RoleRankPo roleRankPo = module().getCurRoleRankPo();
        if (roleRankPo == null) {
            return;
        }
        if (event instanceof FightScoreChangeEvent) {
            FightScoreChangeEvent fsChangeEvent = (FightScoreChangeEvent) event;
            roleRankPo.setFightScore(fsChangeEvent.getNewFightScore());
            roleRankPo.setRoleLevel(fsChangeEvent.getRoleLevel());
        }
        if (event instanceof FinishAllTinyGameEvent) {
            FinishAllTinyGameEvent tinyGameEvent = (FinishAllTinyGameEvent) event;
            roleRankPo.setGamecaveScore(tinyGameEvent.getTinyGameScore());
        }
        if (event instanceof FinishSkyTowerLayerEvent) {
            FinishSkyTowerLayerEvent finishSkyTowerLayerEvent = (FinishSkyTowerLayerEvent) event;
            roleRankPo.setSkyTowerLayerSerial(finishSkyTowerLayerEvent.getLayerSerial());
        }
        module().updateToRank(roleRankPo);
    }
}
