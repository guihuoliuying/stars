package com.stars.modules.newservermoney;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.newservermoney.event.NSMoneyRewardEvent;
import com.stars.modules.newservermoney.listener.ActivityListenner;
import com.stars.modules.newservermoney.listener.NSMoneyRewardListener;
import com.stars.modules.newservermoney.prodata.NewServerMoneyVo;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.util.DateUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class NewServerMoneyModuleFactory extends AbstractModuleFactory<NewServerMoneyModule> {
    public NewServerMoneyModuleFactory() {
        super(new NewServerMoneyPacketSet());
    }

    @Override
    public NewServerMoneyModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new NewServerMoneyModule(id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        loadGrantMoneyVo();
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(NSMoneyRewardEvent.class, new NSMoneyRewardListener((NewServerMoneyModule) module));
        eventDispatcher.reg(OperateActivityEvent.class, new ActivityListenner(module));
    }

    private void loadGrantMoneyVo() throws SQLException {
        String sql = "select * from `newservermoney`; ";
        List<NewServerMoneyVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, NewServerMoneyVo.class, sql);
        Map<Integer, Map<Integer, NewServerMoneyVo>> map = new HashMap<>();
        Map<Integer, long[]> timeMap = new HashMap<>();
        for (NewServerMoneyVo vo : list) {
            Map<Integer, NewServerMoneyVo> voMap = map.get(vo.getOperateActId());
            if (voMap == null) {
                voMap = new HashMap<>();
                map.put(vo.getOperateActId(), voMap);
            }
            voMap.put(vo.getType(), vo);
            long startTime = DateUtil.hourStrTimeToDateTime(vo.getStartTime()).getTime();
            timeMap.put(vo.getType(), new long[]{startTime,
                    startTime + vo.getInterval() * (vo.getExtractTimes() - 1) * 1000L});
        }

        NewServerMoneyManager.execTimeMap = timeMap;
        NewServerMoneyManager.moneyVoMap = map;
    }
}
