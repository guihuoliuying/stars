package com.stars.modules.guardofficial;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.prodata.CampMissionVo;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-12.
 */
public class GuardOfficialModuleFactory extends AbstractModuleFactory {
    public GuardOfficialModuleFactory() {
        super(new GuardOfficialPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        CampMissionVo missionVo = CampManager.campMissionMap.get(GuardOfficalManager.campMissionId);
        GuardOfficalManager.dungeonType = (byte) missionVo.getTargetId();
        GuardOfficalManager.timesLimit = missionVo.getTargetTime();
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new GuardOfficialModule(MConst.GuardOfficial, id, self, eventDispatcher, map);
    }
}
