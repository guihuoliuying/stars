package com.stars.modules.authentic;

import com.stars.core.annotation.DependOn;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.authentic.gm.anthenticGmHandle;
import com.stars.modules.authentic.prodata.AuthenticVo;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2016/12/22.
 */
@DependOn({MConst.Data})
public class AuthenticModuleFactory extends AbstractModuleFactory<AbstractModule> {
    public AuthenticModuleFactory() {
        super(new AuthenticPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from authentic";
        Map<String, AuthenticVo> tmpMap = new HashMap<>();
        List<AuthenticVo> authenticVos =  DBUtil.queryList(DBUtil.DB_PRODUCT, AuthenticVo.class, sql);
        for(AuthenticVo vo:authenticVos) {
            tmpMap.put(vo.getLevelsection()+"+"+ vo.getType(), vo);
        }
        AuthenticManager.authenticVoMap = tmpMap;
        loadMoneyDropData();
        loadGoldDropData();
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("jianbao",new anthenticGmHandle());
    }

    @Override
    public AbstractModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new AuthenticModule("鉴宝", id, self, eventDispatcher, map);
    }

    private void loadMoneyDropData(){
        String moneydrop = DataManager.getCommConfig("authentic_firstmoneydropid");
        Map<Integer , Integer> moneyDropMap = StringUtil.toMap(moneydrop,Integer.class,Integer.class,'+','|');
        AuthenticConst.newMoneyCount = moneyDropMap.size();
        AuthenticManager.newPlayerMoneyDrop = moneyDropMap;

    }

    private void loadGoldDropData(){
        String moneydrop = DataManager.getCommConfig("authentic_firstgolddropid");
        Map<Integer , Integer> goldDropMap = StringUtil.toMap(moneydrop,Integer.class,Integer.class,'+','|');
        AuthenticConst.newGoldCount = goldDropMap.size();
        AuthenticManager.newPlayerGoldDrop = goldDropMap;
    }
}
