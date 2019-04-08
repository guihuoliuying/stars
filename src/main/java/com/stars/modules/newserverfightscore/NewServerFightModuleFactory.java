package com.stars.modules.newserverfightscore;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.drop.DropManager;
import com.stars.modules.drop.prodata.DropVo;
import com.stars.modules.newserverfightscore.event.NSFSHistoryRankUpdateEvent;
import com.stars.modules.newserverfightscore.listener.NSFightScoreListener;
import com.stars.modules.newserverfightscore.prodata.NewServerFightScoreVo;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.role.event.FightScoreChangeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class NewServerFightModuleFactory extends AbstractModuleFactory<NewServerFightModule> {
    public NewServerFightModuleFactory() {
        super(new NewServerFightPacketSet());
    }

    @Override
    public NewServerFightModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new NewServerFightModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        NSFightScoreListener listener = new NSFightScoreListener((NewServerFightModule) module);

        eventDispatcher.reg(FightScoreChangeEvent.class, listener);
        eventDispatcher.reg(NSFSHistoryRankUpdateEvent.class, listener);
        eventDispatcher.reg(OperateActivityFlowEvent.class, listener);
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from `newserverfightscore`; ";
        List<NewServerFightScoreVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, NewServerFightScoreVo.class, sql);
        Map<Integer, Map<Integer, NewServerFightScoreVo>> map = new HashMap<>();
        for (NewServerFightScoreVo vo : list) {
            Map<Integer, NewServerFightScoreVo> voMap = map.get(vo.getOperateActId());
            if (voMap == null) {
                voMap = new HashMap<>();
                map.put(vo.getOperateActId(), voMap);
            }
            /**
             * 3.21 战力冲冲冲活动showitem通过读取drop表
             */
            DropVo dropVo = DropManager.getDropVo(vo.getReward());
            String showItem = dropVo.getShowItem();
            vo.setShowItem(showItem);

            voMap.put(vo.getRewardId(), vo);

        }
        NewServerFightManager.NSFightScoreVoMap = map;
    }
}
