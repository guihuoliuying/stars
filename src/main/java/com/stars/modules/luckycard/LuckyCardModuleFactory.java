package com.stars.modules.luckycard;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.luckycard.gm.LuckyCardGm;
import com.stars.modules.luckycard.listenner.LuckyCardListenner;
import com.stars.modules.luckycard.prodata.LuckyCard;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class LuckyCardModuleFactory extends AbstractModuleFactory<LuckyCardModule> {
    public LuckyCardModuleFactory() {
        super(new LuckyCardPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, LuckyCard> luckyCardMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", LuckyCard.class, "select * from luckcard");
        LuckyCardManager.luckyCardMap = luckyCardMap;
        List<LuckyCard> normalCards = new ArrayList<>();
        List<LuckyCard> specialCards = new ArrayList<>();
        List<LuckyCard> allCards = new ArrayList<>();
        for (LuckyCard luckyCard : luckyCardMap.values()) {
            if (luckyCard.getType() == 1) {
                specialCards.add(luckyCard);
            } else if (luckyCard.getType() == 2) {
                normalCards.add(luckyCard);
            }
            allCards.add(luckyCard);
        }
        Collections.sort(normalCards);
        Collections.sort(specialCards);
        Collections.sort(allCards);
        LuckyCardManager.normalCards = normalCards;
        LuckyCardManager.specialCards = specialCards;
        LuckyCardManager.allCards = allCards;
        String luckcardPayPeyaward = DataManager.getCommConfig("luckcard_pay_peyaward");
        String[] group = luckcardPayPeyaward.split("\\|");
        int[] basicScale = StringUtil.toArray(group[0], int[].class, '+');
        int[] additionalScale = StringUtil.toArray(group[1], int[].class, '+');
        LuckyCardManager.luckyCardPayPayAward = luckcardPayPeyaward;
        LuckyCardManager.basicScale = basicScale;
        LuckyCardManager.additionalScale = additionalScale;
        String luckcardPayResolve = DataManager.getCommConfig("luckcard_pay_resolve");
        Map<Integer, Integer> resolveReward = StringUtil.toMap(luckcardPayResolve, Integer.class, Integer.class, '+', '+');
        LuckyCardManager.resolveReward = resolveReward;
        int luckyCardConsumeUnit = DataManager.getCommConfig("luckcard_consumeitem", 1);
        LuckyCardManager.luckyCardConsumeUnit = luckyCardConsumeUnit;
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("luckycard", new LuckyCardGm());
    }

    @Override
    public LuckyCardModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new LuckyCardModule("幸运卡牌", id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        LuckyCardListenner luckyCardListenner = new LuckyCardListenner((LuckyCardModule) module);
        eventDispatcher.reg(VipChargeEvent.class, luckyCardListenner);
        eventDispatcher.reg(OperateActivityEvent.class, luckyCardListenner);
        eventDispatcher.reg(RoleLevelUpEvent.class, luckyCardListenner);
        eventDispatcher.reg(AddToolEvent.class, luckyCardListenner);
    }
}
