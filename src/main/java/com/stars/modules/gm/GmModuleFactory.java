package com.stars.modules.gm;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.gm.event.GmRedpointEvent;
import com.stars.modules.gm.gmhandler.*;
import com.stars.modules.gm.listener.GmRedPointListener;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/1/15.
 */
public class GmModuleFactory extends AbstractModuleFactory<GmModule> {

    public GmModuleFactory() {
        super(new GmPacketSet());
    }

    @Override
    public void init() throws Exception {
        // 超级GM
        GmManager.reg("sudo", new SudoGmHandler());
        // 普通GM
        GmManager.reg("reload", new ReloadGmHandler());
        GmManager.reg("kickOff", new KickOffGmHandler());
        GmManager.reg("chat", new ChatGmHandler());
        GmManager.reg("rpc", new RpcGmHandler());
        GmManager.reg("i18n", new I18nGmHandler());
        GmManager.reg("gm", new GmGmHandler());
        GmManager.reg("rankemail", new NewServerRankGmHandler());
        GmManager.reg("pc", new PcGmHandler());
        GmManager.reg("ac", new AccessControlGmHandler());
        GmManager.reg("fight", new FightServerGmHandler());


        // 专项测试
        GmManager.reg("onekey", new OneKeyGmHandler());
        GmManager.reg("zrt", new OneKeyGmHandler());
//        GmManager.reg("unlimit", new UnlimitGmHandler());
//        GmManager.reg("lat", new UnlimitGmHandler());
    }

    @Override
    public GmModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new GmModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(GmRedpointEvent.class, new GmRedPointListener((GmModule) module));
    }
}
