package com.stars.modules.pk;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;
import com.stars.modules.pk.event.*;
import com.stars.modules.pk.listener.PkListener;
import com.stars.modules.scene.SceneManager;

import java.util.Map;

public class PKModuleFacotry extends AbstractModuleFactory<PKModule> {

    public PKModuleFacotry() {
        super(new PKPacketSet());
    }

    @Override
    public PKModule newModule(long id, Player self,
                              EventDispatcher eventDispatcher, Map<String, Module> map) {
        // TODO Auto-generated method stub
        return new PKModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
        // TODO Auto-generated method stub
        super.init();
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        PkListener listener = new PkListener((PKModule) module);

        eventDispatcher.reg(EnterPkEvent.class, listener);
        eventDispatcher.reg(InvitePkEvent.class, listener);
        eventDispatcher.reg(BackCityEvent.class, listener);
        eventDispatcher.reg(PermitPkEvent.class, listener);
        eventDispatcher.reg(FinishPkEvent.class, listener);
        eventDispatcher.reg(UpdateReceiveInviteEvent.class, listener);
    }

    @Override
    public void loadProductData() throws Exception {
        PKManager.recordMax = Integer.parseInt(DataManager.getCommConfig("personalpk_record_nummax"));
        PKManager.pkStageId = Integer.parseInt(DataManager.getCommConfig("personalpk_stageid"));
        PKManager.inviteAvailableTime = Long.parseLong(DataManager.getCommConfig("personalpk_invite_holdtime")) * 1000;
        PKManager.pvpLimitTime = SceneManager.getStageVo(PKManager.pkStageId).getVictoryConMap().get(SceneManager.VICTORY_CONDITION_TIME) +
                Long.parseLong(DataManager.getCommConfig("personalpk_prepartime")) * 1000;
    }
}
