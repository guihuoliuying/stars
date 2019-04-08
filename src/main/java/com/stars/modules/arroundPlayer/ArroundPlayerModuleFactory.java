package com.stars.modules.arroundPlayer;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.arroundPlayer.listener.*;
import com.stars.modules.baby.event.BabyFashionChangeEvent;
import com.stars.modules.deityweapon.event.DeityWeaponChangeEvent;
import com.stars.modules.fashion.event.FashionChangeEvent;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.newequipment.event.DragonBallChangeEvent;
import com.stars.modules.ride.event.RideChangeEvent;
import com.stars.modules.scene.event.EnterSceneEvent;
import com.stars.modules.vip.event.VipLevelupEvent;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/12.
 */
public class ArroundPlayerModuleFactory extends AbstractModuleFactory<ArroundPlayerModule> {

    public ArroundPlayerModuleFactory() {
        super(new ArroundPlayerPacketSet());
    }

    @Override
    public ArroundPlayerModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ArroundPlayerModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(EnterSceneEvent.class, new ArroundPlayerEnterSceneListener((ArroundPlayerModule) module));
        eventDispatcher.reg(RideChangeEvent.class, new ArroundPlayerRideChangeListener((ArroundPlayerModule) module)); // 坐骑改变
        eventDispatcher.reg(FashionChangeEvent.class, new ArroundPlayerFashionChangeListener((ArroundPlayerModule) module));//时装改变
        eventDispatcher.reg(DragonBallChangeEvent.class, new ArroundPlayerDragonBallChangeListener((ArroundPlayerModule) module));//时装改变
        // vip等级提升
        eventDispatcher.reg(VipLevelupEvent.class, new ArroundPlayerVipLevelUpListener((ArroundPlayerModule) module));
        eventDispatcher.reg(DeityWeaponChangeEvent.class, new ArroundPlayerDeityWeaponChangeListener((ArroundPlayerModule) module));//神兵改变
        eventDispatcher.reg(RoleRenameEvent.class, new RoleChangeListenner((ArroundPlayerModule) module));
        eventDispatcher.reg(BabyFashionChangeEvent.class, new ArroundBabyFashionChangeListener((ArroundPlayerModule) module));
    }

}
