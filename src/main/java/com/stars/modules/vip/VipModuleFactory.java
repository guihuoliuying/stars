package com.stars.modules.vip;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.modules.vip.event.VipLevelupEvent;
import com.stars.modules.vip.gm.ResetFirstChargeGmHandler;
import com.stars.modules.vip.gm.VipChargeGmHandler;
import com.stars.modules.vip.listener.VipListener;
import com.stars.modules.vip.prodata.ChargeVo;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by liuyuheng on 2016/12/3.
 */
public class VipModuleFactory extends AbstractModuleFactory<VipModule> {
    public VipModuleFactory() {
        super(new VipPacketSet());
    }

    @Override
    public VipModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new VipModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("payment", new VipChargeGmHandler());// vip充值
        GmManager.reg("resetfirstcharge", new ResetFirstChargeGmHandler());// 重置首充奖励
    }

    @Override
    public void loadProductData() throws Exception {
        loadCommondefine();
        loadVipinfoVo();
        loadChargeVo();
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        VipListener listener = new VipListener((VipModule) module);

        eventDispatcher.reg(VipLevelupEvent.class, listener);
        eventDispatcher.reg(VipChargeEvent.class, listener);
    }


    private void loadVipinfoVo() throws Exception {
        String sql = "select * from `vipinfo`; ";
        VipManager.vipVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "level", VipinfoVo.class, sql);
    }

    private void loadChargeVo() throws SQLException {
        String sql = "select * from `charge`; ";
        List<ChargeVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, ChargeVo.class, sql);
        Map<String, Map<Integer, ChargeVo>> map = new HashMap<>();
        Map<String, List<ChargeVo>>chargetVoList = new HashMap<String, List<ChargeVo>>();
        Map<String, Map<String, ChargeVo>> chargeVoMap1 = new HashMap<>();
        for (ChargeVo vo : list) {
            Map<Integer, ChargeVo> tempMap = map.get(vo.getChannel());
            List<ChargeVo>cList = chargetVoList.get(vo.getChannel());
            Map<String, ChargeVo>tempMap1 = chargeVoMap1.get(vo.getChannel());
            if (tempMap == null) {
                tempMap = new HashMap<>();
                map.put(vo.getChannel(), tempMap);
            }
            if (cList == null) {
				cList = new ArrayList<ChargeVo>();
				chargetVoList.put(vo.getChannel(), cList);
			}
            if (tempMap1 == null) {
            	tempMap1 = new HashMap<>();
            	chargeVoMap1.put(vo.getChannel(), tempMap1);
            }
            tempMap.put(vo.getChargeId(), vo);
            cList.add(vo);
            tempMap1.put(vo.getIosChargeId(), vo);
        }
        Collection<List<ChargeVo>>col = chargetVoList.values();
        for (List<ChargeVo> list2 : col) {
        	Collections.sort(list2);
		}
        VipManager.chargeVoMap = map;
        VipManager.chargetVoList = chargetVoList;
        VipManager.chargeVoMap1 = chargeVoMap1;
    }

    private void loadCommondefine() throws Exception {
        VipManager.monthCardAward = StringUtil.toMap(DataManager.getCommConfig("vip_monthcaraward"), Integer.class,
                Integer.class, '+', ',');
        VipManager.monthCardDays = Integer.parseInt(DataManager.getCommConfig("vip_monthday"));
        VipManager.cardContinueDay = Integer.parseInt(DataManager.getCommConfig("vip_cardcontinueday"));
        VipManager.VIP_EXP_COEF = DataManager.getCommConfig("vip_rmbchangeexp", 1);
        VipManager.FINISH_BRAVE_DROP_GROUP = DataManager.getCommConfig("vip_bravecompleteaward", 0);
        VipManager.FINISH_MASTER_NOTICE_COST = StringUtil.toMap(DataManager.getCommConfig("vip_noticeauto"), Integer.class, Integer.class, '+', ',');
        VipManager.FINISH_BRAVE_COST = StringUtil.toMap(DataManager.getCommConfig("vip_braveauto"), Integer.class, Integer.class, '+', ',');

    }
}
