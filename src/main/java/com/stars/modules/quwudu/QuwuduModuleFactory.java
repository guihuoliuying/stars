package com.stars.modules.quwudu;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;

import java.util.Map;

/**
 * Created by huwenjun on 2017/5/18.
 */
public class QuwuduModuleFactory extends AbstractModuleFactory<QuwuduModule> {
    public QuwuduModuleFactory() {
        super(new QuwuduPacketSet());
    }

    @Override
    public QuwuduModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new QuwuduModule("驱五毒", id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        QuwuduManager.dailytimes = DataManager.getCommConfig("dragonboat_dungeon_dailytimes", 0);
    }
}
