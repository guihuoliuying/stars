package com.stars.modules.luckydraw1;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.luckydraw.LuckyDrawModule;
import com.stars.modules.operateactivity.NotSendActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;

import java.util.Map;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDraw1Module extends LuckyDrawModule implements NotSendActivityModule {

    public LuckyDraw1Module(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }
    public int getActType() {
        return OperateActivityConstant.ActType_LuckyDraw1;
    }
}
