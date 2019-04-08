package com.stars.modules.newfirstrecharge;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.newfirstrecharge.packet.ClientNewFirstRechargePacket;
import com.stars.modules.newfirstrecharge.pojo.NewFirstRechargeReward;
import com.stars.modules.newfirstrecharge.prodata.NewFirstRecharge;
import com.stars.modules.newfirstrecharge.usrdata.RoleNewFirstRecharge;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.opentime.ActOpenTime5;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.services.ServiceHelper;
import com.stars.services.actloopreset.event.ActLoopResetEvent;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/6.
 */
public class NewFirstRechargeModule extends AbstractModule implements OpActivityModule {
    private RoleNewFirstRecharge roleNewFirstRecharge;

    public NewFirstRechargeModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleNewFirstRecharge = DBUtil.queryBean(DBUtil.DB_USER, RoleNewFirstRecharge.class, String.format("select * from rolenewfirstrecharge where roleid=%s and activitytype=%s", id(), getActivityType()));

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (roleNewFirstRecharge != null) {
            roleNewFirstRecharge.reset(moduleMap());
            checkSendAdditionalReward();
            context().update(roleNewFirstRecharge);
            checkReward();
        }
    }

    public int getEmailTemplateId() {
        return 30021;
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (roleNewFirstRecharge == null) return;
        if (redPointIds.contains(getRedPointId()) && getCurShowActivityId() != -1) {
            StringBuilder sb = new StringBuilder();
//            for (Map.Entry<Integer, Integer> entry : roleNewFirstRecharge.getActivityDataMap().entrySet()) {
            int tmp = roleNewFirstRecharge.getDayStatus(roleNewFirstRecharge.getToday());
            if (tmp != 0) {
                sb.append(tmp).append("+");
//                    redPointMap.put(getRedPointId(), "");
//                    break;
            }
//                redPointMap.put(getRedPointId(), null);
//            }
            redPointMap.put(getRedPointId(), sb.length() > 0 ? sb.toString() : null);
        }
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        int curActivityId = OperateActivityManager.getCurActivityId(getActivityType());
        if (curActivityId == -1) {
            onActivityReset();
        } else {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            ActOpenTime5 actOpenTime5 = (ActOpenTime5) operateActVo.getActOpenTimeBase();
            if (roleNewFirstRecharge != null) {
                if (actOpenTime5.getEndDate().getTime() > roleNewFirstRecharge.getLastActivityEndTime()) {
                    onActivityReset();
                    onInit(false);
                }
            } else {
                roleNewFirstRecharge = new RoleNewFirstRecharge();
                roleNewFirstRecharge.setRoleId(id());
                roleNewFirstRecharge.setActivityType(getActivityType());
                roleNewFirstRecharge.setLastActivityEndTime(actOpenTime5.getEndDate().getTime());
                initReward();
                context().insert(roleNewFirstRecharge);
            }
            VipModule vipModule = module(MConst.Vip);
            int vipLevel = vipModule.getVipLevel();
            roleNewFirstRecharge.setVipLevel(vipLevel);
            checkSendAdditionalReward();
            checkReward();
        }
    }

    /**
     * 检测发送补充奖励
     */
    private void checkSendAdditionalReward() {
        for (Map.Entry<Integer, Integer> entry : roleNewFirstRecharge.getActivityDataMap().entrySet()) {
            Date startDate = new Date(roleNewFirstRecharge.getLastTakeTime());
            if (roleNewFirstRecharge.getLastTakeTime() != 0 && DateUtil.getRelativeDifferDays(startDate, new Date()) >= 1 &&
                    entry.getValue() == 1) {
                NewFirstRecharge newFirstRecharge = NewFirstRechargeManagerFacade.getNewFirstRechargeMap(getActivityType()).get(entry.getKey());
                NewFirstRechargeReward award1 = newFirstRecharge.getAward(0, roleNewFirstRecharge.getVipLevel());
                Map<Integer, Integer> reward = award1.getReward();
                ServiceHelper.emailService().sendToSingle(id(), getEmailTemplateId(), 0L, "系统", reward);
                roleNewFirstRecharge.setDayStatus(entry.getKey(), 0);
                break;
            }
        }

    }

    public void reqMainUIData() {
        ClientNewFirstRechargePacket clientNewFirstRechargePacket = new ClientNewFirstRechargePacket(getActivityType(), ClientNewFirstRechargePacket.SEND_MAIN_UI_DATA);
        clientNewFirstRechargePacket.setRoleNewFirstRecharge(roleNewFirstRecharge);
        send(clientNewFirstRechargePacket);
    }

    /**
     * 检测奖励状态
     * -1,未初始化状态
     * 0，已领取
     * 1,可领取
     */
    public void checkReward() {
        if (getCurShowActivityId() == -1) {
            return;
        }
        for (Map.Entry<Integer, NewFirstRecharge> entry : NewFirstRechargeManagerFacade.getNewFirstRechargeMap(getActivityType()).entrySet()) {
            Integer day = entry.getKey();
            Date startDate = new Date(roleNewFirstRecharge.getLastTakeTime());
            if (roleNewFirstRecharge.getLastTakeTime() == 0 || DateUtil.getRelativeDifferDays(startDate, new Date()) >= 1) {
                if (roleNewFirstRecharge.getDayStatus(day) == -1) {
                    NewFirstRecharge newFirstRecharge = entry.getValue();
                    if (roleNewFirstRecharge.getPayCount() >= newFirstRecharge.getPaycount()) {
                        roleNewFirstRecharge.setLastTakeTime(System.currentTimeMillis());
                        roleNewFirstRecharge.setDayStatus(day, 1);
                        LogUtil.info("roleid:{},in acttype:{} check reward:day:{} can take reward", id(), getActivityType(), day);
                        roleNewFirstRecharge.setPayCount(0);
                    }
                    roleNewFirstRecharge.setToday(day);
                    break;
                }
            }
            if (roleNewFirstRecharge.getDayStatus(day) == 1 || roleNewFirstRecharge.getDayStatus(day) == 0) {
                roleNewFirstRecharge.setToday(day);
            }
        }
        context().update(roleNewFirstRecharge);
        signCalRedPoint(getModuleName(), getRedPointId());
    }

    /**
     * 检测奖励状态
     * -1,未初始化状态
     * 0，已领取
     * 1,可领取
     */
    public void initReward() {
        Map<Integer, Integer> activityDataMap = roleNewFirstRecharge.getActivityDataMap();
        if (activityDataMap.size() != NewFirstRechargeManagerFacade.getNewFirstRechargeMap(getActivityType()).size()) {
            for (Map.Entry<Integer, NewFirstRecharge> entry : NewFirstRechargeManagerFacade.getNewFirstRechargeMap(getActivityType()).entrySet()) {
                Integer day = entry.getKey();
                roleNewFirstRecharge.setDayStatus(day, -1);
            }
        }

    }

    public int getActivityType() {
        return OperateActivityConstant.ActType_NewFirstRecharge;
    }

    /**
     * 活动重置
     */
    public void onActivityReset() {
        LogUtil.info("roleid:{} trigger new first recharge reset", id());
        if (roleNewFirstRecharge != null) {
            roleNewFirstRecharge = null;
            String sql = "delete from rolenewfirstrecharge where roleid=%s and activitytype=%s;";
            try {
                DBUtil.execSql(DBUtil.DB_USER, String.format(sql, id(), getActivityType()));
            } catch (SQLException e) {
                LogUtil.error(e.getMessage(), e);
            }
        }

    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(getActivityType());
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            /**
             * 角色是否被限制
             */
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            boolean isClose = true;
            if (roleNewFirstRecharge == null) {
                isClose = false;
            } else {
                for (Map.Entry<Integer, Integer> entry : roleNewFirstRecharge.getActivityDataMap().entrySet()) {
                    Integer status = entry.getValue();
                    if (status != 0) {
                        isClose = false;
                        break;
                    }
                }
            }
            if (show && !isClose) {
                return curActivityId;
            }

        }
        return -1;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    /**
     * 检测奖励状态
     * -1,未初始化状态
     * 0，已领取
     * 1,可领取
     * 2，未达到条件(当日)
     */
    public void reqTakeReward(int group) {
        int dayStatus = roleNewFirstRecharge.getDayStatus(roleNewFirstRecharge.getToday());
        int vipLevel = roleNewFirstRecharge.getVipLevel();
        reqChooseReward(group);
        switch (dayStatus) {
            case 1: {
                NewFirstRecharge newFirstRecharge = NewFirstRechargeManagerFacade.getNewFirstRechargeMap(getActivityType()).get(roleNewFirstRecharge.getToday());
                NewFirstRechargeReward award = newFirstRecharge.getAward(group, vipLevel);
                Map<Integer, Integer> reward = award.getReward();
                ToolModule toolModule = module(MConst.Tool);
                toolModule.addAndSend(reward, getEventType().getCode());
                LogUtil.info("newFirstRecharge roleid:{} take reward day:{} group:{}", id(), roleNewFirstRecharge.getToday(), group);
                roleNewFirstRecharge.setDayStatus(roleNewFirstRecharge.getToday(), 0);
                context().update(roleNewFirstRecharge);
                ClientAward clientAward = new ClientAward(reward);
                clientAward.setType((byte) 1);
                send(clientAward);
                checkReward();
                reqMainUIData();
                signCalRedPoint(getModuleName(), getRedPointId());
            }
            break;
            default: {
                warn("无可领取的奖励");
            }
        }
    }

    public EventType getEventType() {
        return EventType.NEW_FIRST_RECHARGE;
    }

    public String getModuleName() {
        return MConst.NewFirstRechargeModule;
    }

    public int getRedPointId() {
        return RedPointConst.NEW_FIRST_RECHARGE;
    }

    /**
     * 监听充值
     *
     * @param event
     */
    public void onEvent(Event event) {
        if (event instanceof VipChargeEvent) {
            if (getCurShowActivityId() != -1) {
                if (roleNewFirstRecharge.getDayStatus(roleNewFirstRecharge.getToday()) == -1) {
                    VipChargeEvent vipChargeEvent = (VipChargeEvent) event;
                    int money = vipChargeEvent.getMoney();
                    roleNewFirstRecharge.addPayCount(money * 10);
                    LogUtil.info("newFirstRecharge:{} payment yb:{}", id(), money * 10);
                    checkReward();
                    context().update(roleNewFirstRecharge);
                    reqMainUIData();
                }

            }
        }
        if (event instanceof RoleLevelUpEvent) {
            if (getCurShowActivityId() != -1) {
                try {
                    onInit(false);
                } catch (Throwable throwable) {
                    LogUtil.error(getModuleName() + " init error:" + id(), throwable);
                }
            }
        }
        if (event instanceof OperateActivityEvent) {
            OperateActivityEvent operateActivityEvent = (OperateActivityEvent) event;
            if (operateActivityEvent.getActivityType() == getActivityType()) {
                if (operateActivityEvent.getFlag() == OperateActivityEvent.Flag_Open_Activity) {
                    LogUtil.info("activity notice:{} open:{}", getModuleName(), id());
                    try {
                        onInit(false);
                    } catch (Throwable throwable) {
                        LogUtil.error(getModuleName() + " init error!", throwable);
                    }
                } else if (operateActivityEvent.getFlag() == OperateActivityEvent.Flag_Close_Activity) {
                    onActivityReset();
                }
            }
        }
        if (event instanceof ActLoopResetEvent) {
            try {
                onDataReq();
                onInit(false);
            } catch (Throwable throwable) {
                LogUtil.error("actLoopReset:3009 fail:" + id(), throwable);

            }
        }
    }

    public void reqChooseReward(int group) {
        roleNewFirstRecharge.setGroup(group);
        context().update(roleNewFirstRecharge);
    }

    public RoleNewFirstRecharge getRoleNewFirstRecharge() {
        return roleNewFirstRecharge;
    }
}
