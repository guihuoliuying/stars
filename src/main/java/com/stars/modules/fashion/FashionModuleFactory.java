package com.stars.modules.fashion;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.fashion.gm.FashionGmHandler;
import com.stars.modules.fashion.listenner.FashionChangeJobListenner;
import com.stars.modules.fashion.prodata.FashionAttrVo;
import com.stars.modules.fashion.prodata.FashionVo;
import com.stars.modules.fashion.summary.FashionSummaryComponentImpl;
import com.stars.modules.fashioncard.event.FashionCardEvent;
import com.stars.modules.gm.GmManager;
import com.stars.services.summary.Summary;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 时装的模块工厂;
 * Created by gaopeidian on 2016/10/08.
 */
public class FashionModuleFactory extends AbstractModuleFactory<FashionModule> {

    public FashionModuleFactory() {
        super(new FashionPacketSet());
    }

    @Override
    public FashionModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new FashionModule(id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("fashion", new FashionGmHandler());

        Summary.regComponentClass(MConst.Fashion, FashionSummaryComponentImpl.class);
    }

    @Override
    public void loadProductData() throws Exception {
        initProductFashion();
    }

    private void initProductFashion() throws SQLException {
        String sql = "select * from `fashion`; ";
        Map<Integer, FashionVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "fashionid", FashionVo.class, sql);
        FashionManager.setFashionVoMap(map);
        /**
         * 职业类型组装
         * 《jobid，《type，fashionvo》
         */
        Map<Integer, Map<Integer, FashionVo>> jobFashionMap = new HashMap<>();
        for (Map.Entry<Integer, FashionVo> entry : map.entrySet()) {
            FashionVo fashionVo = entry.getValue();
            Map<Integer, FashionVo> typeFashionMap = jobFashionMap.get(fashionVo.getJob());
            if (typeFashionMap == null) {
                typeFashionMap = new HashMap<>();
                jobFashionMap.put(fashionVo.getJob(), typeFashionMap);
            }
            typeFashionMap.put(fashionVo.getType(), fashionVo);
        }
        FashionManager.jobFashionMap = jobFashionMap;
        sql = "select * from `fashionattr`;";
        Map<Integer, FashionAttrVo> attrMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "fashionid", FashionAttrVo.class, sql);
        FashionManager.setFashionAttrVoMap(attrMap);

        String marryFashionStr = DataManager.getCommConfig("goumailifu_jiage");
        if (StringUtil.isEmpty(marryFashionStr)) {
            com.stars.util.LogUtil.error("commondefine 没有配 goumailifu_jiage数据，结婚礼服，请检查");
        }
        try { // 格式为：道具id+道具数量,货币类型+数量
            String[] array = marryFashionStr.split(",");
            String[] itemCountStr = array[0].split("[+]");
            int itemId = Integer.parseInt(itemCountStr[0]);
            int itemCount = Integer.parseInt(itemCountStr[1]);
            String[] costStr = array[1].split("[+]");
            int reqItemId = Integer.parseInt(costStr[0]);
            int reqCount = Integer.parseInt(costStr[1]);
            FashionManager.setMarryFashionItemId(itemId);
            FashionManager.setMarryFashionBuyCount(itemCount);
            FashionManager.setBuyMarryFashionItemId(reqItemId);
            FashionManager.setBuyMarryfashionReqCount(reqCount);
        } catch (Exception e) {
            LogUtil.error("commondefine goumailifu_jiage 格式不对，当前值为：{}", marryFashionStr);
        }

    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        FashionChangeJobListenner listenner = new FashionChangeJobListenner((FashionModule) module);
        eventDispatcher.reg(ChangeJobEvent.class, listenner);
        eventDispatcher.reg(FashionCardEvent.class, listenner);
    }
}

