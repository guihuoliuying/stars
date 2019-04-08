package com.stars.modules.discountgift;

import com.stars.core.annotation.DependOn;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.discountgift.listener.DiscountGiftListener;
import com.stars.modules.discountgift.prodata.DiscountGiftVo;
import com.stars.modules.push.event.PushActivedEvent;
import com.stars.modules.push.event.PushInactivedEvent;
import com.stars.modules.push.event.PushLoginInitEvent;
import com.stars.modules.vip.event.VipChargeEvent;

import java.util.*;

/**
 * Created by chenxie on 2017/5/26.
 */
@DependOn({MConst.Data, MConst.Push})
public class DiscountGiftModuleFactory extends AbstractModuleFactory<DiscountGiftModule> {

    public DiscountGiftModuleFactory() {
        super(new DiscountGiftPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from discountgift";
        List<DiscountGiftVo> dicGiftVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, DiscountGiftVo.class, sql);
        Collections.sort(dicGiftVoList);
        DiscountGiftManager.dicGiftVoList = dicGiftVoList;
        Map<Integer, List<DiscountGiftVo>> dicGiftVoMap = new HashMap<>();
        for (DiscountGiftVo vo : dicGiftVoList) {
            if (dicGiftVoMap.containsKey(vo.getGiftGroupId())) {
                dicGiftVoMap.get(vo.getGiftGroupId()).add(vo);
            } else {
                List<DiscountGiftVo> discountGiftVoGroupList = new ArrayList<>();
                discountGiftVoGroupList.add(vo);
                dicGiftVoMap.put(vo.getGiftGroupId(), discountGiftVoGroupList);
            }
        }
        DiscountGiftManager.dicGiftVoMap = dicGiftVoMap;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        DiscountGiftListener discountGiftListener = new DiscountGiftListener((DiscountGiftModule) module);
        eventDispatcher.reg(VipChargeEvent.class, discountGiftListener);
        eventDispatcher.reg(PushLoginInitEvent.class, discountGiftListener);
        eventDispatcher.reg(PushActivedEvent.class, discountGiftListener);
        eventDispatcher.reg(PushInactivedEvent.class, discountGiftListener);
    }

    @Override
    public DiscountGiftModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new DiscountGiftModule(id, self, eventDispatcher, map);
    }

}
