package com.stars.modules.optionalbox;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.gm.GmManager;
import com.stars.modules.optionalbox.gm.OptionalBoxGm;
import com.stars.modules.optionalbox.prodata.OptionalBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class OptionalBoxModuleFactory extends AbstractModuleFactory<OptionalBoxModule> {
    public OptionalBoxModuleFactory() {
        super(new OptionalBoxPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, OptionalBox> optionalBoxMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", OptionalBox.class, "select * from optionalbox;");
        OptionalBoxManager.optionalBoxMap = optionalBoxMap;
        Map<Integer, List<OptionalBox>> optionBoxListMap = new HashMap<>();
        for (OptionalBox optionalBox : optionalBoxMap.values()) {
            int group = optionalBox.getGroup();
            List<OptionalBox> optionalBoxes = optionBoxListMap.get(group);
            if (optionalBoxes == null) {
                optionalBoxes = new ArrayList<>();
                optionBoxListMap.put(group, optionalBoxes);
            }
            optionalBoxes.add(optionalBox);
        }
        OptionalBoxManager.optionBoxListMap = optionBoxListMap;
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("optionalbox", new OptionalBoxGm());
    }

    @Override
    public OptionalBoxModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new OptionalBoxModule("自选礼包", id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        super.registerListener(eventDispatcher, module);
    }
}
