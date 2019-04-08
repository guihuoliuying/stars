package com.stars.modules.newsignin;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.newsignin.gm.NewSigninGmHandler;
import com.stars.modules.newsignin.prodata.SigninVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017/2/5 17:09
 */
public class NewSigninModuleFactory extends AbstractModuleFactory<NewSigninModule> {
    public NewSigninModuleFactory() {
        super(new NewSigninPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        List<SigninVo> signinVos = DBUtil.queryList(DBUtil.DB_PRODUCT, SigninVo.class, "select * from signin");
        Map<String, SigninVo> singleSignMap = new HashMap<>();
        Map<String, Map<Integer, SigninVo>> accumulateAwardMap = new HashMap<>();
        Map<String, Map<Integer, SigninVo>> specialAwardMap = new HashMap<>();
        Map<Integer, String> signId2Date = new HashMap<>();
        for (SigninVo vo : signinVos) {
            if (vo.getType() == NewSigninConst.singleSign) {
                vo.setYyyymmdd(vo.getParam());
                singleSignMap.put(vo.getYyyymmdd(), vo);
                signId2Date.put(vo.getSigninId(),vo.getYyyymmdd());
            }
            if (vo.getType() == NewSigninConst.accumulateAward) {
                setSigninVo(vo);
                Map<Integer, SigninVo> accTempMap = accumulateAwardMap.get(vo.getYyyymm());
                if (accTempMap == null) {
                    accTempMap = new HashMap<>();
                    accumulateAwardMap.put(vo.getYyyymm(), accTempMap);
                }
                accTempMap.put(vo.getCount(), vo);
                signId2Date.put(vo.getSigninId(),vo.getYyyymm());
            }
            if (vo.getType() == NewSigninConst.specialAward) {
                setSigninVo(vo);
                Map<Integer, SigninVo> specTempMap = specialAwardMap.get(vo.getYyyymm());
                if (specTempMap == null) {
                    specTempMap = new HashMap<>();
                    specialAwardMap.put(vo.getYyyymm(), specTempMap);
                }
                specTempMap.put(vo.getCount(), vo);
                signId2Date.put(vo.getSigninId(),vo.getYyyymm());
            }
        }
        NewSigninManager.setSingleSignMap(singleSignMap);
        NewSigninManager.setSignId2Date(signId2Date);
        NewSigninManager.setAccumulateAwardMap(accumulateAwardMap);
        NewSigninManager.setSpecialAwardMap(specialAwardMap);
        loadCommondDefine();

    }

    @Override
    public void init() throws Exception {
        GmManager.reg("sign",new NewSigninGmHandler());
    }

    private void setSigninVo(SigninVo vo){
        String[] paramStr = vo.getParam().split("\\+");
        vo.setYyyymm(paramStr[0]);
        vo.setCount(Integer.parseInt(paramStr[1]));
    }

    @Override
    public NewSigninModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new NewSigninModule("签到", id, self, eventDispatcher, map);
    }

    private void loadCommondDefine() {
        String resignCost = DataManager.getCommConfig("signin_resign_cost");
        int serverOpenDays = Integer.parseInt(DataManager.getCommConfig("signin_opendays"));
        NewSigninManager.setReSignCostMap(resignCost);
        NewSigninManager.setServerOpenDays(serverOpenDays);
    }
}
