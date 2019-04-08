package com.stars.modules.callboss;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.callboss.event.CallBossStatusChangeEvent;
import com.stars.modules.callboss.listener.CallBossEventListener;
import com.stars.modules.callboss.prodata.CallBossVo;
import com.stars.modules.data.DataManager;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/9/2.
 */
public class CallBossModuleFactory extends AbstractModuleFactory<CallBossModule> {
    public CallBossModuleFactory() {
        super(new CallBossPacketSet());
    }

    @Override
    public CallBossModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new CallBossModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
    	eventDispatcher.reg(CallBossStatusChangeEvent.class, new CallBossEventListener(module));
    }
    
    @Override
    public void loadProductData() throws Exception {
        loadCallBossVo();
    }

    /**
     * 加载callboss
     *
     * @throws SQLException
     */
    private void loadCallBossVo() throws SQLException {
        String sql = "select * from `callboss`; ";
        Map<Integer, CallBossVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "bossid", CallBossVo.class, sql);

        String callBossTime = DataManager.getCommConfig("callboss_time").trim();
        if (callBossTime.isEmpty() || !callBossTime.contains("+"))
            throw new IllegalArgumentException("commondefine表callboss_time字段配置格式错误");
        String[] temp = callBossTime.split("\\+");

        CallBossManager.callBossVoMap = map;
        CallBossManager.startTime = temp[0].trim() + ":00";
        CallBossManager.endTime = temp[1].trim() + ":00";
    }
}
