package com.stars.modules.fashioncard;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.fashion.event.FashionChangeEvent;
import com.stars.modules.fashioncard.effect.imp.AddItemEffectImpl;
import com.stars.modules.fashioncard.effect.imp.AddPasskillEffectImpl;
import com.stars.modules.fashioncard.effect.imp.DelSkillCdEffectImpl;
import com.stars.modules.fashioncard.effect.imp.OlAnnounceEffectImpl;
import com.stars.modules.fashioncard.listener.FashionCardEventListner;
import com.stars.modules.fashioncard.prodata.FashionCard;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-10-13.
 */
public class FashionCardModuleFactory extends AbstractModuleFactory<FashionCardModule> {
    public FashionCardModuleFactory() {
        super(new FashionCardPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, FashionCard> fashionCardMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "fashioncardid", FashionCard.class, "select * from fashioncard");
        FashionCardManager.fashionCardMap = fashionCardMap;
    }

    @Override
    public void init() throws Exception {
        FashionCardManager.regEffect(FashionCardManager.ADD_PASSKILL, AddPasskillEffectImpl.class);
        FashionCardManager.regEffect(FashionCardManager.ADD_ITEM, AddItemEffectImpl.class);
        FashionCardManager.regEffect(FashionCardManager.DEL_SKILL_CD, DelSkillCdEffectImpl.class);
        FashionCardManager.regEffect(FashionCardManager.OL_ANNOUNCE, OlAnnounceEffectImpl.class);
    }

    @Override
    public FashionCardModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new FashionCardModule(MConst.FashionCard, id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(FashionChangeEvent.class, new FashionCardEventListner(module));
    }
}
