package com.stars.modules.title;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.title.listenner.LevelUpListenner;
import com.stars.modules.title.prodata.TitleVo;
import com.stars.modules.title.summary.TitleSummaryComponentImpl;
import com.stars.modules.vip.event.VipLevelupEvent;
import com.stars.services.summary.Summary;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/21.
 */
public class TitleModuleFactory extends AbstractModuleFactory<TitleModule> {
    public TitleModuleFactory() {
        super(new TitlePacketSet());
    }

    @Override
    public TitleModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new TitleModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() {
        Summary.regComponentClass("title", TitleSummaryComponentImpl.class);
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from `title`; ";
        Map<Integer, TitleVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "titleid", TitleVo.class, sql);
        TitleManager.titleVoMap = map;
        String typeSql = "select type from `title` group by type order by type;";
        TitleManager.typeList = DBUtil.queryList(DBUtil.DB_PRODUCT, Byte.class, typeSql);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(RoleLevelUpEvent.class, new LevelUpListenner((TitleModule) module));
        eventDispatcher.reg(VipLevelupEvent.class, new LevelUpListenner((TitleModule) module));
    }
}
