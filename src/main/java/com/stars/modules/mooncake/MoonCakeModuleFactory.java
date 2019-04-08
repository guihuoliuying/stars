package com.stars.modules.mooncake;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.mooncake.listenner.MoonCakeListenner;
import com.stars.modules.mooncake.prodata.moonCakeRwdVo;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.services.actloopreset.event.ActLoopResetEvent;

import java.util.Map;

/**
 * Created by zhangerjiang on 2017/9/14.
 */
public class MoonCakeModuleFactory extends AbstractModuleFactory {


    public MoonCakeModuleFactory() {
        super(new MoonCakePacketSet());
    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new MoonCakeModule(MConst.MoonCake, id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        loadCommondefine();
    }

    private void loadCommondefine() throws Exception {


        String tmpStr = DataManager.getCommConfig("mooncake_time");
        String[] itemArr = tmpStr.split("\\+");

        MoonCakeManager.iLastTime = Integer.parseInt(itemArr[0]);
        MoonCakeManager.iRedTime = Integer.parseInt((itemArr[1]));
        MoonCakeManager.MAX_TIME = Integer.parseInt(itemArr[2]);

        MoonCakeManager.iCanGetMaxScore = Integer.parseInt(DataManager.getCommConfig("mooncake_maxmark"));

        String rwdStr = DataManager.getCommConfig("mooncake_scoreaward");
        String[] rwdArr = rwdStr.split("\\|");

        for (int i = 0; i < rwdArr.length; i++) {
            String[] dropStr = rwdArr[i].split("\\+");
            int iScore = Integer.parseInt(dropStr[0]);
            int iItemId = Integer.parseInt(dropStr[1]);
            int iCount = Integer.parseInt(dropStr[2]);
            moonCakeRwdVo rwdVo = new moonCakeRwdVo();
            rwdVo.setScore(iScore);
            rwdVo.setCount(iCount);
            rwdVo.setItemId(iItemId);
            MoonCakeManager.dayScoreRwdMap.put(rwdVo.getScore(), rwdVo);

        }
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        MoonCakeListenner moonCakeListenner = new MoonCakeListenner((MoonCakeModule)module);
        eventDispatcher.reg(ActLoopResetEvent.class, moonCakeListenner);
        eventDispatcher.reg(OperateActivityEvent.class, moonCakeListenner);
    }
}
