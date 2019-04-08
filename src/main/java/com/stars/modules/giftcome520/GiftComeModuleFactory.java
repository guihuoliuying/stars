package com.stars.modules.giftcome520;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;
import com.stars.modules.giftcome520.listenner.ActivityListenner;
import com.stars.modules.operateactivity.event.OperateActivityEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by huwenjun on 2017/4/15.
 */
public class GiftComeModuleFactory extends AbstractModuleFactory<GiftComeModule> {
    public GiftComeModuleFactory() {
        super(new GiftComePacketSet());
    }

    @Override
    public GiftComeModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new GiftComeModule("520礼尚往来", id, self, eventDispatcher, map);
    }


    @Override
    public void loadProductData() throws Exception {
        String beginDate = null;
        String endDate = null;
        Date benginDateTime = null;
        Date endDateTime = null;

        String npcLoveGiftDate = DataManager.getCommConfig("npclovegift_date");
        String[] dateTuple = npcLoveGiftDate.split("\\,");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /**
         * 配置可能一个时间也可能两个
         */
        if (dateTuple.length == 1) {
            beginDate = dateTuple[0] + " 00:00:00";
            endDate = dateTuple[0] + " 23:59:59";
        } else if (dateTuple.length == 2) {
            beginDate = dateTuple[0] + " 00:00:00";
            endDate = dateTuple[1] + " 23:59:59";
        }
        benginDateTime = sdf.parse(beginDate);
        endDateTime = sdf.parse(endDate);

        GiftComeManager.npcLoveGiftRewardDropGroupId = DataManager.getCommConfig("npclovegift_reward", 0);
        GiftComeManager.beginDate = beginDate;
        GiftComeManager.endDate = endDate;
        GiftComeManager.benginDateTime = benginDateTime;
        GiftComeManager.endDateTime = endDateTime;
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(OperateActivityEvent.class, new ActivityListenner(module));
    }
}
