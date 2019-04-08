package com.stars.modules.camp.activity;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.module.ModuleContext;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.camp.usrdata.RoleCampTimesPo;

import java.util.Map;

/**
 * Created by huwenjun on 2017/7/20.
 */
public class AbstractCampActivity extends AbstractModule implements CampActivity {
    public AbstractCampActivity(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap, RoleCampPo roleCampPo, RoleCampTimesPo roleCampTimesPo) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public ModuleContext context() {
        CampModule module = module(MConst.Camp);
        return module.context();
    }
    public RoleCampPo getRoleCamp() {
        CampModule campModule = module(MConst.Camp);
        return campModule.getRoleCamp();
    }

    public RoleCampTimesPo getRoleCampTimes() {
        CampModule campModule = module(MConst.Camp);
        return campModule.getRoleCampTimes();
    }
}
