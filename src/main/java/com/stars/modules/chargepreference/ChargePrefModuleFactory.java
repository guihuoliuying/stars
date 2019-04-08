package com.stars.modules.chargepreference;

import com.stars.core.annotation.DependOn;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.chargepreference.listener.ChargePrefListener;
import com.stars.modules.chargepreference.prodata.ChargePrefVo;
import com.stars.modules.data.DataManager;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.push.PushManager;
import com.stars.modules.push.event.PushActivedEvent;
import com.stars.modules.push.event.PushLoginInitEvent;
import com.stars.modules.vip.event.VipChargeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/30.
 */
@DependOn({MConst.Data, MConst.Push})
public class ChargePrefModuleFactory extends AbstractModuleFactory<ChargePrefModule> {

    public ChargePrefModuleFactory() {
        super(new ChargePrefPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, ChargePrefVo> prefVoMap = DBUtil.queryMap(
                DBUtil.DB_PRODUCT, "prefid", ChargePrefVo.class, "select * from chargepreference");
        Map<Integer, ChargePrefVo> pushId2PrefVoMap = new HashMap<>();
        for (ChargePrefVo prefVo : prefVoMap.values()) {
            prefVo.init();
            pushId2PrefVoMap.put(prefVo.getPushId(), prefVo);
        }
        // 检查push表是否跟chargepreference表一一对应
        Set<Integer> pushIdSet = new HashSet<>(PushManager.getPushVoMapByActivityId(1).keySet());
        for (ChargePrefVo prefVo : prefVoMap.values()) {
            if (!pushIdSet.contains(prefVo.getPushId())) {
                throw new RuntimeException("充值特惠|push表缺失数据|prefId:" + prefVo.getPrefId() + "|pushId:" + prefVo.getPushId());
            }
        }
        for (Integer pushId : pushIdSet) {
            if (!pushId2PrefVoMap.containsKey(pushId)) {
                throw new RuntimeException("充值特惠|chargepreference表缺失数据|pushId:" + pushId);
            }
        }

        ChargePrefManager.countLimit = DataManager.getCommConfig("pushitem_buychance_num", 0);
//        ChargePrefManager.rebateEmailTemplateId = Integer.parseInt(DataManager.getCommConfig("pushitem_emailtemplate", "26001"));
        ChargePrefManager.rebateEmailTemplateId = Integer.parseInt(DataManager.getCommConfig("pushitem_emailtemplate", "26001+26002").split("\\+")[0]);
//        ChargePrefManager.normalEmailTemplateId = Integer.parseInt(DataManager.getCommConfig("pushitem_emailtemplate", "26001"));
        ChargePrefManager.normalEmailTemplateId = Integer.parseInt(DataManager.getCommConfig("pushitem_emailtemplate", "26001+26002").split("\\+")[1]);
        ChargePrefManager.prefVoMap = prefVoMap;
        ChargePrefManager.pushId2PrefVoMap = pushId2PrefVoMap;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        ChargePrefListener listener = new ChargePrefListener((ChargePrefModule) module);
        eventDispatcher.reg(ForeShowChangeEvent.class, listener);
        eventDispatcher.reg(PushLoginInitEvent.class, listener);
        eventDispatcher.reg(PushActivedEvent.class, listener);
        eventDispatcher.reg(VipChargeEvent.class, listener);
    }

    @Override
    public ChargePrefModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ChargePrefModule(id, self, eventDispatcher, map);
    }
}
