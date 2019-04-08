package com.stars.modules.popUp;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.popUp.listener.PopupListener;
import com.stars.modules.popUp.prodata.PopUpInfo;
import com.stars.modules.push.event.PushLoginDoneEvent;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class PopUpModuleFactory extends AbstractModuleFactory<PopUpModule> {
    public PopUpModuleFactory() {
        super(new PopUpPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from popup";
        Map<Integer, PopUpInfo> popUpInfoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "popupid", PopUpInfo.class, sql);
        PopUpManager.POP_UP_INFO_MAPS = popUpInfoMap;
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public PopUpModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new PopUpModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        PopupListener listener = new PopupListener((PopUpModule) module);
        eventDispatcher.reg(PushLoginDoneEvent.class, listener);
    }
}
