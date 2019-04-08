package com.stars.modules.newofflinepvp;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.newofflinepvp.event.OfflinePvpClientStageFinishEvent;
import com.stars.modules.newofflinepvp.event.OfflinePvpMatchEvent;
import com.stars.modules.newofflinepvp.event.OfflinePvpSendRankEvent;
import com.stars.modules.newofflinepvp.listener.*;
import com.stars.modules.newofflinepvp.prodata.OfflineAwardVo;
import com.stars.modules.newofflinepvp.prodata.OfflineInitializeVo;
import com.stars.modules.newofflinepvp.prodata.OfflineMatchVo;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-03-08 15:35
 */
public class NewOfflinePvpModuleFactory extends AbstractModuleFactory<NewOfflinePvpModule> {
    public NewOfflinePvpModuleFactory() {
        super(new NewOfflinePvpPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        List<OfflineMatchVo> offlineMatchVos = DBUtil.queryList(DBUtil.DB_PRODUCT, OfflineMatchVo.class, "select * from offlinematch");
//        NewOfflinePvpManager.setOfflineMatchVos(offlineMatchVos);

        Map<Long, OfflineInitializeVo> offlineInitializeVos = DBUtil.queryMap(DBUtil.DB_PRODUCT, "initializeid", OfflineInitializeVo.class, "select * from offlineinitialize");
//        NewOfflinePvpManager.setOfflineInitializeVos(offlineInitializeVos);

        Map<Integer, OfflineAwardVo> offlineAwardVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "offlinerankid", OfflineAwardVo.class, "select * from offlineaward");
//        NewOfflinePvpManager.setOfflineAwardVoMap(offlineAwardVoMap);

        int maxFightCount = Integer.parseInt(DataManager.getCommConfig("offline_fightcount"));
//        NewOfflinePvpManager.setMaxFightCount(maxFightCount);
        String maxBuyCount = DataManager.getCommConfig("offline_buyfightcount");
        String[] maxBuyCountStr = maxBuyCount.split("\\+");
//        NewOfflinePvpManager.setMaxBuyCount(Integer.parseInt(maxBuyCountStr[0]));
//        NewOfflinePvpManager.setBuyCountItemId(Integer.parseInt(maxBuyCountStr[1]));
//        NewOfflinePvpManager.setBuyCountItemCount(Integer.parseInt(maxBuyCountStr[2]));

        String rankSection = DataManager.getCommConfig("offline_ranksection");
        String[] tmpStr = rankSection.split("\\|");
//        NewOfflinePvpManager.setGod(Integer.parseInt(tmpStr[0].split("\\+")[0]));
//        NewOfflinePvpManager.setLand(Integer.parseInt(tmpStr[1].split("\\+")[1]));
        Map<Integer, Integer> rankSectionMap = StringUtil.toMap(rankSection, Integer.class, Integer.class, '+', '|');
//        NewOfflinePvpManager.setRankSectionMap(rankSectionMap);

        String winloseaward = DataManager.getCommConfig("offline_winloseaward");
        String[] winloseawardStr = winloseaward.split("\\+");
        Map<Byte, Integer> winLoseAwardMap = new HashMap<>();
        winLoseAwardMap.put(NewOfflinePvpManager.victory, Integer.parseInt(winloseawardStr[0]));
        winLoseAwardMap.put(NewOfflinePvpManager.defeat, Integer.parseInt(winloseawardStr[1]));

        NewOfflinePvpManager.setOfflineMatchVos(offlineMatchVos);
        NewOfflinePvpManager.setOfflineInitializeVos(offlineInitializeVos);
        NewOfflinePvpManager.setOfflineAwardVoMap(offlineAwardVoMap);
        NewOfflinePvpManager.setMaxFightCount(maxFightCount);
        NewOfflinePvpManager.setMaxBuyCount(Integer.parseInt(maxBuyCountStr[0]));
        NewOfflinePvpManager.setBuyCountItemId(Integer.parseInt(maxBuyCountStr[1]));
        NewOfflinePvpManager.setBuyCountItemCount(Integer.parseInt(maxBuyCountStr[2]));
        NewOfflinePvpManager.setGod(Integer.parseInt(tmpStr[0].split("\\+")[0]));
        NewOfflinePvpManager.setLand(Integer.parseInt(tmpStr[1].split("\\+")[1]));
        NewOfflinePvpManager.setRankSectionMap(rankSectionMap);
        NewOfflinePvpManager.setWinLoseAwardMap(winLoseAwardMap);
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(ForeShowChangeEvent.class, new OpenOfflinePvpListener((NewOfflinePvpModule) module));
        eventDispatcher.reg(OfflinePvpClientStageFinishEvent.class, new OfflinePvpClientStageFinishListener((NewOfflinePvpModule) module));
        eventDispatcher.reg(RoleLevelUpEvent.class, new RoleLevelChangeOfflinePvpListener((NewOfflinePvpModule) module));
        eventDispatcher.reg(FightScoreChangeEvent.class, new RoleFightScoreChangeOfflinePvpListener((NewOfflinePvpModule) module));
        eventDispatcher.reg(OfflinePvpSendRankEvent.class, new OfflinePvpSendRanklistener((NewOfflinePvpModule) module));
        eventDispatcher.reg(OfflinePvpMatchEvent.class, new OfflinePvpMatchLitener((NewOfflinePvpModule) module));
        RoleChangeListenner roleChangeListenner=new RoleChangeListenner((NewOfflinePvpModule) module);
        eventDispatcher.reg(ChangeJobEvent.class, roleChangeListenner);
        eventDispatcher.reg(RoleRenameEvent.class,roleChangeListenner);
    }

    @Override
    public NewOfflinePvpModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new NewOfflinePvpModule("新版竞技场", id, self, eventDispatcher, map);
    }
}
