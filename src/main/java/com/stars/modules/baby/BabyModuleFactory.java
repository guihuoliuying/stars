package com.stars.modules.baby;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.baby.gm.BabyGmHandler;
import com.stars.modules.baby.listener.AddOrDelToolForBabyListener;
import com.stars.modules.baby.prodata.BabyFashion;
import com.stars.modules.baby.prodata.BabySweepVo;
import com.stars.modules.baby.prodata.BabyVo;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.event.UseToolEvent;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-20.
 */
public class BabyModuleFactory extends AbstractModuleFactory {
    public BabyModuleFactory() {
        super(new BabyPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        List<BabyVo> babyVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, BabyVo.class, "select * from babymain");
        Map<Integer, Map<Integer, BabyVo>> babyVoMap = new HashMap<>();
        for (BabyVo babyVo : babyVoList) {
            Map<Integer, BabyVo> babyVos = babyVoMap.get(babyVo.getStage());
            if (babyVos == null) {
                babyVoMap.put(babyVo.getStage(), babyVos = new HashMap<>());
            }
            babyVos.put(babyVo.getLevel(), babyVo);
        }
        BabyManager.babyVoMap = babyVoMap;
        List<BabySweepVo> babySweepVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, BabySweepVo.class, "select * from babysweep");
        Map<Integer, List<BabySweepVo>> babySweepVoMap = new HashMap<>();
        for (BabySweepVo babySweepVo : babySweepVoList) {
            if (babySweepVo.getId() == BabyConst.SEARCHTREASURE_SWEEP && (babySweepVo.getLoopMark() < 1 || babySweepVo.getLoopMark() > 3)) {
                throw new IllegalArgumentException("loopMark不允许超出1-3");
            }
            List<BabySweepVo> babySweepVos = babySweepVoMap.get(babySweepVo.getId());
            if (babySweepVos == null) {
                babySweepVoMap.put(babySweepVo.getId(), babySweepVos = new ArrayList<>());
            }
            babySweepVos.add(babySweepVo);
        }
        BabyManager.babySweepVoMap = babySweepVoMap;
        BabyManager.PAY_MULTIPLE = DataManager.getCommConfig("baby_pay_growuptimes", 2);
        String[] tmp0 = DataManager.getCommConfig("baby_sex_luckyup").split(",");
        BabyManager.NORMAL_LUCKY_VALUE = Integer.parseInt(tmp0[0]);
        BabyManager.PAY_LUCKY_VALUE = Integer.parseInt(tmp0[1]);
        String[] tmp1 = DataManager.getCommConfig("baby_sex_roll").split(",");
        BabyManager.NORMAL_RATE = Double.parseDouble(tmp1[0]);
        BabyManager.PAY_RATE = Double.parseDouble(tmp1[1]);
        String[] tmp2 = DataManager.getCommConfig("baby_energy").split(",");
        BabyManager.DELTA_ENERGY = Integer.parseInt(tmp2[0]);
        BabyManager.INTERVAL_ENERGY = Integer.parseInt(tmp2[1]);
        BabyManager.MAX_ENERGY = Integer.parseInt(tmp2[2]);
        String[] tmp3 = DataManager.getCommConfig("baby_growup_ctr").split(",");
        String[] tmp4 = tmp3[0].split("\\+");
        BabyManager.NORMAL_CRIT_PER = (int) (Double.parseDouble(tmp4[0]) * 1000);
        BabyManager.NORMAL_CRIT = (int) Double.parseDouble(tmp4[1]);
        String[] tmp5 = tmp3[1].split("\\+");
        BabyManager.PAY_CRIT_PER = (int) (Double.parseDouble(tmp5[0]) * 1000);
        BabyManager.PAY_CRIT = (int) Double.parseDouble(tmp5[1]);
        BabyManager.SWEEP = DataManager.getCommConfig("baby_rush_switch", 1) == 1;
        BabyManager.PRAY_FAIL_TIPS = StringUtil.toArrayList(DataManager.getGametext("baby_sex_miss"), String.class, '|');
        List<String> tmp6 = StringUtil.toArrayList(DataManager.getGametext("baby_notice_stage1_nor"), String.class, '|');
        List<String> tmp7 = StringUtil.toArrayList(DataManager.getGametext("baby_notice_stage1_pay"), String.class, '|');
        List<String> tmp8 = StringUtil.toArrayList(DataManager.getGametext("baby_notice_stage2_nor"), String.class, '|');
        List<String> tmp9 = StringUtil.toArrayList(DataManager.getGametext("baby_notice_stage2_pay"), String.class, '|');
        List<String> tmp10 = StringUtil.toArrayList(DataManager.getGametext("baby_notice_stage3_nor"), String.class, '|');
        List<String> tmp11 = StringUtil.toArrayList(DataManager.getGametext("baby_notice_stage3_pay"), String.class, '|');
        BabyManager.FEED_TIPS.put(BabyConst.NORMAL_PRAY_OR_FEED, new HashMap<Integer, List<String>>());
        BabyManager.FEED_TIPS.put(BabyConst.PAY_PRAY_OR_FEED, new HashMap<Integer, List<String>>());
        for (Map.Entry<Integer, Map<Integer, List<String>>> entry : BabyManager.FEED_TIPS.entrySet()) {
            entry.getValue().put(BabyConst.QIANGBAO, new ArrayList<String>());
            entry.getValue().put(BabyConst.YOUER, new ArrayList<String>());
            entry.getValue().put(BabyConst.BABY, new ArrayList<String>());
        }
        BabyManager.FEED_TIPS.get(BabyConst.NORMAL_PRAY_OR_FEED).get(BabyConst.QIANGBAO).addAll(tmp6);
        BabyManager.FEED_TIPS.get(BabyConst.NORMAL_PRAY_OR_FEED).get(BabyConst.YOUER).addAll(tmp8);
        BabyManager.FEED_TIPS.get(BabyConst.NORMAL_PRAY_OR_FEED).get(BabyConst.BABY).addAll(tmp10);
        BabyManager.FEED_TIPS.get(BabyConst.PAY_PRAY_OR_FEED).get(BabyConst.QIANGBAO).addAll(tmp7);
        BabyManager.FEED_TIPS.get(BabyConst.PAY_PRAY_OR_FEED).get(BabyConst.YOUER).addAll(tmp9);
        BabyManager.FEED_TIPS.get(BabyConst.PAY_PRAY_OR_FEED).get(BabyConst.BABY).addAll(tmp11);
        String[] tmp12 = DataManager.getCommConfig("baby_changename").split("\\+");
        BabyManager.CHANGENAME_REQ_ITEMID = Integer.parseInt(tmp12[0]);
        BabyManager.CHANGENAME_REQ_ITEMCOUNT = Integer.parseInt(tmp12[1]);
        BabyManager.stageLvFeedCountMap = StringUtil.toMap(DataManager.getCommConfig("baby_growup_times"), Integer.class, Integer.class, '=', '|');
        BabyManager.babyFashionVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "id", BabyFashion.class, "select * from babyfashion;");
    }

    @Override
    public void init() throws Exception {
        GmManager.reg("baby", new BabyGmHandler());
    }

    @Override
    public Module newModule(long id, Player self, EventDispatcher eventDispatcher, Map map) {
        return new BabyModule(MConst.Baby, id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        AddOrDelToolForBabyListener listener = new AddOrDelToolForBabyListener(module);
        eventDispatcher.reg(UseToolEvent.class, listener);
        eventDispatcher.reg(AddToolEvent.class, listener);
    }
}
