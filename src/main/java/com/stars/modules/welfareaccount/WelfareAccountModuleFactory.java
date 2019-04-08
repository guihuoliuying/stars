package com.stars.modules.welfareaccount;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.welfareaccount.event.VirtualMoneyChangeEvent;
import com.stars.modules.welfareaccount.listenner.VirtualMoneyListenner;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/4/11.
 */
public class WelfareAccountModuleFactory extends AbstractModuleFactory<WelfareAccountModule> {
    public WelfareAccountModuleFactory() {
        super(new WelfareAccountPacketSet());
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void loadProductData() throws Exception {
        super.loadProductData();
        String sql = "select * from welfareaccount";
        List<String> welfareAccounts = DBUtil.queryList(DBUtil.DB_COMMON, String.class, sql);
        WelfareAccountManager.welfareAccountSet.addAll(welfareAccounts);
    }

    @Override
    public WelfareAccountModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new WelfareAccountModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(VirtualMoneyChangeEvent.class, new VirtualMoneyListenner((WelfareAccountModule) module));
    }
}
