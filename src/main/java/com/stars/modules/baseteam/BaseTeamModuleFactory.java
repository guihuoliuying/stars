package com.stars.modules.baseteam;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.baseteam.event.BaseTeamEvent;
import com.stars.modules.baseteam.listener.BaseTeamListener;
import com.stars.modules.deityweapon.event.DeityWeaponChangeEvent;
import com.stars.modules.role.event.FightScoreChangeEvent;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/11/9.
 */
public class BaseTeamModuleFactory extends AbstractModuleFactory<BaseTeamModule> {
    public BaseTeamModuleFactory() {
        super(new BaseTeamPacketSet());
    }

    @Override
    public BaseTeamModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new BaseTeamModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        BaseTeamListener listener = new BaseTeamListener((BaseTeamModule) module);

        eventDispatcher.reg(BaseTeamEvent.class, listener);
        eventDispatcher.reg(FightScoreChangeEvent.class, listener);
        eventDispatcher.reg(DeityWeaponChangeEvent.class, listener);
    }
}
