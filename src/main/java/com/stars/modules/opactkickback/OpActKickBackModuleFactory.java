package com.stars.modules.opactkickback;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.opactkickback.listen.KickBackListenner;
import com.stars.modules.opactkickback.listen.UseToolListener;
import com.stars.modules.opactkickback.userdata.ConsumGateDefineCatalog;
import com.stars.modules.tool.event.UseToolEvent;
import com.stars.services.actloopreset.event.ActLoopResetEvent;

import java.util.Map;

public class OpActKickBackModuleFactory extends AbstractModuleFactory<OpActKcikBackModule> {

    public OpActKickBackModuleFactory() {
        super(new OpActKickBackPacketSet());
    }

    @Override
    public OpActKcikBackModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new OpActKcikBackModule("消费返利", id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        KickBackListenner kickBackListenner = new KickBackListenner((OpActKcikBackModule) module);
        eventDispatcher.reg(UseToolEvent.class, new UseToolListener((OpActKcikBackModule) module));
        eventDispatcher.reg(ActLoopResetEvent.class,kickBackListenner);
    }

    @Override
    public void loadProductData() throws Exception {
        ConsumGateDefineCatalog.instance.reload();
    }

}
