package com.stars.modules.chargepreference;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.chargepreference.packet.ClientChargePref;
import com.stars.modules.chargepreference.prodata.ChargePrefVo;
import com.stars.modules.chargepreference.userdata.RoleChargePrefPo;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.push.PushModule;
import com.stars.modules.push.PushUtil;
import com.stars.modules.push.event.PushActivedEvent;
import com.stars.modules.push.event.PushLoginInitEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by zhaowenshuo on 2017/3/29.
 */
public class ChargePrefModule extends AbstractModule {

    public static final String F_CHOSEN_PREF_ID = "chargePref.chosenPrefId";
    public static final String F_REMAIN_COUNT = "chargePref.remainCount";

    private int remainCount = 0;
    private int chosenPrefId = 0;
    private Map<Integer, RoleChargePrefPo> prefPoMap; // (prefId -> roleChargePrefPo)

    public ChargePrefModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("chargepreference", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        this.prefPoMap = new HashMap<>();
        setRemainCount(ChargePrefManager.countLimit); //
        setChosenPrefId(ChargePrefManager.NO_CHOSEN_PREF_ID); //
    }

    @Override
    public void onDataReq() throws Throwable {
        this.prefPoMap = DBUtil.queryMap(DBUtil.DB_USER, "prefid", RoleChargePrefPo.class,
                "select * from `rolechargepreference` where `roleid`=" + id());
        this.remainCount = getInt(F_REMAIN_COUNT, 0);
        this.chosenPrefId = getInt(F_CHOSEN_PREF_ID, ChargePrefManager.NO_CHOSEN_PREF_ID);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (!isLogin) {
            reset();
        }
    }

    @Override
    public void onWeeklyReset(boolean isLogin) throws Throwable {
        if (!isLogin) {
            reset();
        }
    }

    // 通过红点控制按钮的显示和隐藏
    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (prefPoMap.size() == 0 || getRemainCount() <= 0) {
            redPointMap.put(RedPointConst.CHARGE_PREF, null);
        } else {
            redPointMap.put(RedPointConst.CHARGE_PREF, "true");
        }
    }

    public void onEvent(Event event) {
        // 充值特惠开启
        if (event instanceof ForeShowChangeEvent) {
            if (((ForeShowChangeEvent) event).getMap().containsKey(ForeShowConst.CHARGE_PREF)) {
                resetData();
                autoChoose();
            }
            LogUtil.info("ChargePref|open|roleId:{}|perfPoSet:{}", id(), prefPoMap != null ? prefPoMap.values() : null);
            LogUtil.info("ChargePref|open|roleId:{}|chosenPrefId:{}|remainCount:{}", id(), getChosenPrefId(), getRemainCount());
        }
        if (isOpen()) {
            // 登陆初始化
            if (event instanceof PushLoginInitEvent) {
                PushLoginInitEvent e = (PushLoginInitEvent) event;
                init();
                if (e.isReset()) {
                    reset();
                }
            }
            // 推送激活
            if (event instanceof PushActivedEvent) {
                for (Integer pushId : ((PushActivedEvent) event).getPushInfoMap().keySet()) {
                    resetDataByGroup(getPrefIdFromPushId(pushId));
                }
                if (getChosenPrefId() == ChargePrefManager.NO_CHOSEN_PREF_ID) {
                    autoChoose();
                }

                LogUtil.info("ChargePref|pushActived|roleId:{}|perfPoSet:{}", id(), this.prefPoMap.values());
                LogUtil.info("ChargePref|pushActived|roleId:{}|chosenPrefId:{}|remainCount:{}", id(), getChosenPrefId(), getRemainCount());
            }
            // 充值到账
            if (event instanceof VipChargeEvent) {
                VipChargeEvent e = (VipChargeEvent) event;
                if (getRemainCount() <= 0) {
                    LogUtil.info("ChargePref|event|roleId:{}|orderNo:{}|noRemainCount", id(), e.getOrderNo());
                    return;
                }
                if (getChosenPrefId() == ChargePrefManager.NO_CHOSEN_PREF_ID) {
                    LogUtil.info("ChargePref|event|roleId:{}|orderNo:{}|noChosenPrefId", id(), e.getOrderNo());
                    return;
                }
                RoleChargePrefPo po = prefPoMap.get(getChosenPrefId());
                if (po == null) {
                    LogUtil.info("ChargePref|event|roleId:{}|orderNo:{}|prefId:{}|noUserData", id(), e.getOrderNo(), getChosenPrefId());
                    return;
                }
                ChargePrefVo vo = ChargePrefManager.getPrefVo(po.getPrefId());
                if (vo == null) {
                    LogUtil.info("ChargePref|event|roleId:{}|orderNo:{}|prefId:{}|noProductData", id(), e.getOrderNo(), getChosenPrefId());
                    return;
                }
                po.setChargeNumber(po.getChargeNumber() + e.getMoney());
                context().update(po);
                LogUtil.info("ChargePref|event|roleId:{}|orderNo:{}|prefId:{}|chargeNumber:{}", id(), e.getOrderNo(), getChosenPrefId(), po.getChargeNumber());
                if (po.getChargeNumber() >= vo.getCurrentPrice()
                        || (po.isRebate() && po.getChargeNumber() >= vo.getRebatePrice())) {
                    setRemainCount(getRemainCount() - 1);
                    if (po.isRebate()) {
                        ServiceHelper.emailService().sendToSingle(
                                id(), ChargePrefManager.rebateEmailTemplateId, 0L, "系统", vo.getItems(), Integer.toString(vo.getRebatePrice()));
                    } else {
                        ServiceHelper.emailService().sendToSingle(
                                id(), ChargePrefManager.normalEmailTemplateId, 0L, "系统", vo.getItems(), Integer.toString(vo.getCurrentPrice()));
                    }
                    prefPoMap.remove(po.getPrefId());
                    PushModule pushModule = module(MConst.Push);
                    ChargePrefVo chargePrefVo = ChargePrefManager.getPrefVo(po.getPrefId());
                    pushModule.inactivePush(chargePrefVo.getPushId());
                    context().delete(po);
                    resetDataByGroup(po.getPrefId()); // 更新一次同组的推送数据
                    autoChoose(); // 自动重选
                    signCalRedPoint(MConst.ChargePref, RedPointConst.CHARGE_PREF); // 重新计算红点

                    /* 推送完成日志 */
                    pushModule.finishPush(getPushIdFromPrefId(po.getPrefId()), 1);

                    LogUtil.info("ChargePref|event|roleId:{}|orderNo:{}|prefId:{}|finishChargePref", id(), e.getOrderNo(), getChosenPrefId());
                }
            }
        } else {
            if (event instanceof VipChargeEvent) {
                VipChargeEvent e = (VipChargeEvent) event;
                LogUtil.info("ChargePref|event|roleId:{}|orderNo:{}|notOpen", id(), e.getOrderNo());
            }
        }
    }

    public void view() {
        if (!isOpen()) {
            warn("系统没开放");
            return;
        }
        ClientChargePref packet = new ClientChargePref(ClientChargePref.SUBTYPE_VIEW);
        packet.setRemainCount(getRemainCount());
        packet.setChosenId(getChosenPrefId());
        packet.setPrefPoMap(prefPoMap);
        send(packet);
    }

    public void choose(int prefId) {
        if (getRemainCount() <= 0) {
            warn("剩余次数不够");
            return;
        }
        RoleChargePrefPo po = prefPoMap.get(prefId);
        if (po == null) {
            warn("不存在对应的用户数据");
            return;
        }
        if (hasChosenPrefId() && prefId != getChosenPrefId()) {
            RoleChargePrefPo prevChosenPo = prefPoMap.get(getChosenPrefId());
            if (prevChosenPo != null) {
                context().update(prevChosenPo);
            }
        }
        setChosenPrefId(prefId);
        context().update(po);
        // 发包
        ClientChargePref packet = new ClientChargePref(ClientChargePref.SUBTYPE_CHOOSE);
        packet.setChosenId(getChosenPrefId());
        send(packet);
    }

    private void resetData() {
        PushModule pushModule = module(MConst.Push);
        Set<Integer> pushIdSet = pushModule.getActivePushIdByActivityId(1);
        // 删除未激活pushId对应特惠数据
        Iterator<Map.Entry<Integer, RoleChargePrefPo>> it = prefPoMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, RoleChargePrefPo> entry = it.next();
            int pushId = getPushIdFromPrefId(entry.getKey());
            RoleChargePrefPo prefPo = entry.getValue();
            if (!pushIdSet.contains(pushId)) {
                context().delete(prefPo);
                it.remove();
            } else {
                prefPo.setChargeNumber(0);
                prefPo.setRebate(false);
                context().update(prefPo);
            }
        }
        // 新增不存在的数据
        for (int pushId : pushIdSet) {
            ChargePrefVo vo = ChargePrefManager.getPrefVoByPushId(pushId);
            if (vo == null) {
                continue;
            }
            RoleChargePrefPo po = prefPoMap.get(vo.getPrefId());
            if (po == null) {
                prefPoMap.put(vo.getPrefId(), po = new RoleChargePrefPo(id(), vo.getPrefId()));
                context().insert(po);
            }
            if (PushUtil.isTrue(vo.getCondChecker(), moduleMap()) && satisfyDate(vo)) {
                po.setRebate(true);
                context().update(po);
            }

        }
        // 自动选择
        autoChoose();
        // 控制按钮是否显示
        signCalRedPoint(MConst.ChargePref, RedPointConst.CHARGE_PREF);
    }

    private void resetDataByGroup(int prefId) {
        PushModule pushModule = module(MConst.Push);
        int pushId = pushModule.getMaxRankActivedPushIdInGroup(getPushIdFromPrefId(prefId));
        if (pushId == 0) {
            return;
        }
        ChargePrefVo vo = ChargePrefManager.getPrefVoByPushId(pushId);
        if (!prefPoMap.containsKey(vo.getPrefId())) {
            RoleChargePrefPo po = prefPoMap.get(vo.getPrefId());
            if (po == null) {
                prefPoMap.put(vo.getPrefId(), po = new RoleChargePrefPo(id(), vo.getPrefId()));
                if (PushUtil.isTrue(vo.getCondChecker(), moduleMap()) && satisfyDate(vo)) {
                    po.setRebate(true);
                }
                context().insert(po);
            }
        }
        // 控制按钮是否显示
        signCalRedPoint(MConst.ChargePref, RedPointConst.CHARGE_PREF);
    }

    private void autoChoose() {
        // 不够次数就不选了
        if (getRemainCount() <= 0) {
            setChosenPrefId(ChargePrefManager.NO_CHOSEN_PREF_ID);
            return;
        }
        // 排序并选择其中一个
        RoleChargePrefPo maxWeightPo = null;
        ChargePrefVo maxWeightVo = null;
        for (RoleChargePrefPo po : prefPoMap.values()) {
            ChargePrefVo vo = ChargePrefManager.getPrefVo(po.getPrefId());
            if (maxWeightPo == null
                    || (po.isRebate() && !maxWeightPo.isRebate())
                    || (vo.isNew() && !maxWeightVo.isNew())
                    || (vo.getRank() > maxWeightVo.getRank())) {
                maxWeightPo = po;
                maxWeightVo = vo;
            }
        }
        if (maxWeightPo != null) {
            context().update(maxWeightPo);
            setChosenPrefId(maxWeightPo.getPrefId());
        } else {
            setChosenPrefId(ChargePrefManager.NO_CHOSEN_PREF_ID);
        }
        LogUtil.info("ChargePref|autoChoose|roleId:{}|chosenPrefId:{}|remainCount:{}", id(), getChosenPrefId(), getRemainCount());
    }

    public void init() {
        /* 移除过期数据 */
        Iterator<RoleChargePrefPo> it = this.prefPoMap.values().iterator();
        while (it.hasNext()) {
            RoleChargePrefPo prefPo = it.next();
            if (ChargePrefManager.getPrefVo(prefPo.getPrefId()) == null) {
                context().delete(prefPo);
                it.remove();
            }
        }
        /* 新增数据 */
        PushModule pushModule = module(MConst.Push);
        Set<Integer> pushIdSet = pushModule.getActivePushIdByActivityId(1);
        for (Integer pushId : pushIdSet) {
            resetDataByGroup(getPrefIdFromPushId(pushId));
        }
        /* 更新状态 */
        if (getChosenPrefId() == ChargePrefManager.NO_CHOSEN_PREF_ID
                || !this.prefPoMap.containsKey(getChosenPrefId())) {
            resetData();
            autoChoose();
        }
        signCalRedPoint(MConst.ChargePref, RedPointConst.CHARGE_PREF);

        LogUtil.info("ChargePref|init|roleId:{}|perfPoSet:{}", id(), this.prefPoMap.values());
        LogUtil.info("ChargePref|init|roleId:{}|chosenPrefId:{}|remainCount:{}", id(), getChosenPrefId(), getRemainCount());
    }

    public void reset() {
        if (!isOpen()) {
            return;
        }
        setRemainCount(ChargePrefManager.countLimit);
        resetData();
        autoChoose();

        LogUtil.info("ChargePref|reset|roleId:{}|perfPoSet:{}", id(), this.prefPoMap.values());
        LogUtil.info("ChargePref|reset|roleId:{}|chosenPrefId:{}|remainCount:{}", id(), getChosenPrefId(), getRemainCount());
    }

    private int getPrefIdFromPushId(int pushId) {
        ChargePrefVo vo = ChargePrefManager.getPrefVoByPushId(pushId);
        if (vo != null) {
            return vo.getPrefId();
        }
        return -1;
    }

    private int getPushIdFromPrefId(int prefId) {
        ChargePrefVo vo = ChargePrefManager.getPrefVo(prefId);
        if (vo != null) {
            return vo.getPushId();
        }
        return -1;
    }

    private boolean satisfyDate(ChargePrefVo prefVo) {
        long now = now();
        return !prefVo.hasExpirationDate()
                || now >= prefVo.getBeginTimeMillis() && now <= prefVo.getEndTimeMillis();
    }

    private boolean isOpen() {
        return ((ForeShowModule) module(MConst.ForeShow)).isOpen(ForeShowConst.CHARGE_PREF);
    }

    private boolean hasChosenPrefId() {
        return chosenPrefId != ChargePrefManager.NO_CHOSEN_PREF_ID;
    }

    private void setChosenPrefId(int prefId) {
        this.chosenPrefId = prefId;
        setInt(F_CHOSEN_PREF_ID, prefId);
    }

    public int getChosenPrefId() {
        return this.chosenPrefId;
    }

    private void setRemainCount(int remainCount) {
        this.remainCount = remainCount;
        setInt(F_REMAIN_COUNT, remainCount);
    }

    private int getRemainCount() {
        return this.remainCount;
    }

    public Iterator<RoleChargePrefPo> prefPoIterator() {
        return prefPoMap.values().iterator();
    }
}
