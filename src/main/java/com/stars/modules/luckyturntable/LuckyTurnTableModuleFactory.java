package com.stars.modules.luckyturntable;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.luckyturntable.event.InitLuckyEvent;
import com.stars.modules.luckyturntable.listener.ChargeForLuckyTurnTableListener;
import com.stars.modules.luckyturntable.prodata.LuckyTurnTableVo;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-12.
 */
public class LuckyTurnTableModuleFactory extends AbstractModuleFactory {
    public LuckyTurnTableModuleFactory() {
        super(new LuckyTurnTablePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from luckyaward";
        LuckyTurnTableManager.luckyTurnTableMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", LuckyTurnTableVo.class, sql);
        LuckyTurnTableManager.luckWard_Worth = DataManager.getCommConfig("luckyward_worth", 10);
        LuckyTurnTableManager.luckyward_List = DataManager.getCommConfig("luckyward_list", 10);
        String[] tmp = DataManager.getCommConfig("luckyward_recycle").split("\\+");
        LuckyTurnTableManager.recycle_ItemId = Integer.parseInt(tmp[0]);
        LuckyTurnTableManager.recycle_Count = Integer.parseInt(tmp[1]);
        LuckyTurnTableManager.luckWard_TicketSnumber = StringUtil.toMap(DataManager.getCommConfig("luckyward_ticketsnumber"), Integer.class, Integer.class, '+', ',');
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new LuckyTurnTableModule(MConst.LuckyTurnTable, id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(VipChargeEvent.class, new ChargeForLuckyTurnTableListener(module));
        eventDispatcher.reg(InitLuckyEvent.class, new ChargeForLuckyTurnTableListener(module));
        eventDispatcher.reg(ForeShowChangeEvent.class, new ChargeForLuckyTurnTableListener(module));
    }
}
