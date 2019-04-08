package com.stars.modules.wordExchange;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.wordExchange.listener.WordExchangeListenner;
import com.stars.modules.wordExchange.prodata.CollectAwardVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class WordExchangeModuleFactory extends AbstractModuleFactory<WordExchangeModule> {
    public WordExchangeModuleFactory() {
        super(new WordExchangePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from collectaward";
        Map<Integer, CollectAwardVo> collectAwardVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", CollectAwardVo.class, sql);

        Map<Integer, List<CollectAwardVo>> listMap = new HashMap<>();
        List<CollectAwardVo> list;
        for (CollectAwardVo vo : collectAwardVoMap.values()) {
            list = listMap.get(vo.getOperateActId());
            if (list == null) {
                list = new ArrayList<>();
                listMap.put(vo.getOperateActId(), list);
            }
            list.add(vo);
        }

        WordExchangeManager.setCollectAwardMap(collectAwardVoMap);
        WordExchangeManager.setActivityCollectList(listMap);
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public WordExchangeModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new WordExchangeModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(AddToolEvent.class, new WordExchangeListenner((WordExchangeModule) module));
    }
}
