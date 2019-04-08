package com.stars.modules.newservermoney;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.newservermoney.event.NSMoneyRewardEvent;
import com.stars.modules.newservermoney.packet.ClientNewServerMoney;
import com.stars.modules.newservermoney.prodata.NewServerMoneyVo;
import com.stars.modules.newservermoney.userdata.ActRoleNSMoney;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.labeldisappear.DisappearByDays;
import com.stars.modules.operateactivity.labeldisappear.DisappearByTime;
import com.stars.modules.operateactivity.labeldisappear.LabelDisappearBase;
import com.stars.modules.operateactivity.labeldisappear.NeverDisappear;
import com.stars.modules.operateactivity.opentime.ActOpenTime5;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.services.ServiceHelper;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by liuyuheng on 2017/1/4.
 */
public class NewServerMoneyModule extends AbstractModule implements OpActivityModule {
    private ActRoleNSMoney roleNSMoney;
    // 红点状态(有改变才推送),这里红点只跟时间有关,所以需要存储状态特殊处理
    private boolean redPointStatus = Boolean.FALSE;
    int curActId = -1;
    OperateActVo operateActVo = null;
    private Map<Integer, long[]> execTimeMap = NewServerMoneyManager.execTimeMap;

    public NewServerMoneyModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("新服活动-撒钱", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        curActId = getCurShowActivityId();
        if (curActId == -1)
            return;
        operateActVo = OperateActivityManager.getOperateActVo(curActId);
        String sql = "select * from `actnewservermoney` where `roleid`=" + id() + " and `operateactid`=" + curActId + " and opentime='" + operateActVo.getOpenTime() + "'";
        roleNSMoney = DBUtil.queryBean(DBUtil.DB_USER, ActRoleNSMoney.class, sql);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        setExecTimeMap();
        signCalRedPoint(MConst.NewServerMoney, RedPointConst.NEW_SERVER_MONEY);
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        setExecTimeMap();
    }

    @Override
    public void onTimingExecute() {
        signCalRedPoint(MConst.NewServerMoney, RedPointConst.NEW_SERVER_MONEY);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.NEW_SERVER_MONEY)) {
            checkRedPoint(redPointMap);
        }
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerMoney);
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            if (show) {
                return curActivityId;
            }

        }
        return -1;
    }

    @Override
    public byte getIsShowLabel() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerMoney);
        if (curActivityId == -1) return (byte) 0;

        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (operateActVo == null) return (byte) 0;

        LabelDisappearBase labelDisappearBase = operateActVo.getDisappear();
        if (labelDisappearBase == null) return (byte) 0;

        if (labelDisappearBase instanceof NeverDisappear) {
            return (byte) 1;
        } else if (labelDisappearBase instanceof DisappearByDays) {
            ActOpenTimeBase openTime = operateActVo.getActOpenTimeBase();
            if (!(openTime instanceof ActOpenTime5)) return (byte) 0;

            ActOpenTime5 actOpenTime5 = (ActOpenTime5) openTime;
            return DateUtil.isBetween(new Date(), actOpenTime5.getStartDate(), actOpenTime5.getEndDate()) ? (byte) 0 : (byte) 1;
        } else if (labelDisappearBase instanceof DisappearByTime) {
            Date date = ((DisappearByTime) labelDisappearBase).getDate();
            return date.getTime() < new Date().getTime() ? (byte) 0 : (byte) 1;
        }

        return (byte) 0;
    }

    /**
     * 发奖
     *
     * @param event
     */
    public void rewardHandler(Event event) {
        NSMoneyRewardEvent rewardEvent = (NSMoneyRewardEvent) event;
        NewServerMoneyVo vo = NewServerMoneyManager.getMoneyVo(rewardEvent.getCurActId(), rewardEvent.getMoneyTypeId());
        ServiceHelper.emailService().sendToSingle(id(), vo.getEmailTemplate(), 0L, "系统", vo.getRewardMap());
        if (roleNSMoney == null) {
            roleNSMoney = new ActRoleNSMoney(id(), getCurShowActivityId(), operateActVo.getOpenTime());
            context().insert(roleNSMoney);
        }
        roleNSMoney.updateRewardRecord(rewardEvent.getRewardTime(), rewardEvent.getMoneyTypeId());
        context().update(roleNSMoney);
    }

    /**
     * 请求数据
     */
    public void reqData() {
        int curActId = getCurShowActivityId();

        Date startDate = new Date();
        Date endDate = new Date();
        if (curActId != -1) {
            OperateActVo vo = OperateActivityManager.getOperateActVo(curActId);
            if (vo != null) {
                ActOpenTimeBase openTimeBase = vo.getActOpenTimeBase();
                if ((openTimeBase != null) && (openTimeBase instanceof ActOpenTime5)) {
                    ActOpenTime5 time = (ActOpenTime5) openTimeBase;
                    startDate = time.getStartDate();
                    endDate = time.getEndDate();
                }
            }
        }

        ClientNewServerMoney packet = new ClientNewServerMoney(ClientNewServerMoney.SEND_DATA);
        packet.setNsMoneyVoMap(curActId, NewServerMoneyManager.getMoneyVoMap(curActId));
        packet.setStartTimeStamp(startDate.getTime());
        packet.setEndTimeStamp(endDate.getTime());
        send(packet);
    }

    /**
     * 请求我的获奖记录
     */
    public void reqRewardRecord() {
        ClientNewServerMoney packet = new ClientNewServerMoney(ClientNewServerMoney.MY_REWARD_RECORD);
        packet.setMyRewardRecord(roleNSMoney == null ? "" : roleNSMoney.getRewardRecord());
        send(packet);
    }

    /**
     * 红点检查
     * 有进行中的奖励类型需要显示红点
     *
     * @param redPointMap
     */
    private void checkRedPoint(Map<Integer, String> redPointMap) {
        boolean rewarding = Boolean.FALSE;
        int curActId = getCurShowActivityId();
        if (curActId == -1) {
            return;
        }
        if (StringUtil.isEmpty(execTimeMap)) {
            return;
        }
        for (long[] time : execTimeMap.values()) {
            long cur = System.currentTimeMillis();
            if (time[0] <= cur && cur <= time[1]) {
                rewarding = Boolean.TRUE;
                break;
            }
        }
        if (redPointStatus != rewarding) {
            redPointMap.put(RedPointConst.NEW_SERVER_MONEY, rewarding ? "" : null);
            redPointStatus = rewarding;
        }
    }

    private void setExecTimeMap() {
        execTimeMap.clear();
        int curActId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerMoney);
        if (curActId == -1) {
            return;
        }

        Map<Integer, NewServerMoneyVo> voMap = NewServerMoneyManager.moneyVoMap.get(curActId);
        if (voMap != null) {
            Map<Integer, long[]> timeMap = new HashMap<>();
            for (NewServerMoneyVo vo : voMap.values()) {
                long startTime = DateUtil.hourStrTimeToDateTime(vo.getStartTime()).getTime();
                timeMap.put(vo.getType(), new long[]{startTime,
                        startTime + vo.getInterval() * (vo.getExtractTimes() - 1) * 1000L});
            }

            execTimeMap = timeMap;
        }
    }
}
