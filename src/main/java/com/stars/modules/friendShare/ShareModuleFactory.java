package com.stars.modules.friendShare;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;

import java.util.Map;

/**
 * Created by chenxie on 2017/6/7.
 */
public class ShareModuleFactory extends AbstractModuleFactory<ShareModule> {

    public ShareModuleFactory() {
        super(new SharePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        ShareManager.INVITEATR_REWARD_SHARE = DataManager.getCommConfig("inviteatr_reward_share", 0);
    }

    @Override
    public ShareModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ShareModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {

    }

}
