package com.stars.modules.newserversign;

import com.stars.AccountRow;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.newserversign.packet.ClientNewServerSign;
import com.stars.modules.newserversign.prodata.NewServerSignVo;
import com.stars.modules.newserversign.userdata.ActSignRewardRecord;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.operateactivity.labeldisappear.DisappearByDays;
import com.stars.modules.operateactivity.labeldisappear.DisappearByTime;
import com.stars.modules.operateactivity.labeldisappear.LabelDisappearBase;
import com.stars.modules.operateactivity.labeldisappear.NeverDisappear;
import com.stars.modules.operateactivity.opentime.ActOpenTime4;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by gaopeidian on 2016/12/21.
 */
public class NewServerSignModule extends AbstractModule implements OpActivityModule {
    /**
     * 当前正在进行的活动
     * 为null则当前无正在进行的活动
     */
    private NewServerSignActivity curActivity = null;

    //所有活动的用户数据
    /**
     * 活动奖励数据
     * <活动id，<签到奖励id，奖励记录>>
     */
    private Map<Integer, Map<Integer, ActSignRewardRecord>> signRewardMap = new HashMap<Integer, Map<Integer, ActSignRewardRecord>>();

    public NewServerSignModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.NewServerSign, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name_, String account_) throws Throwable {
        //这里创建角色的时候不需要做任何加载，因为还没有任何活动数据
    }

    @Override
    public void onDataReq() throws Exception {
        //加载用户数据
        String sql1 = "select * from `actsignrewardrecord` where `roleid`=" + id();
        List<ActSignRewardRecord> rewardRecords = DBUtil.queryList(DBUtil.DB_USER, ActSignRewardRecord.class, sql1);
        if (rewardRecords != null && rewardRecords.size() > 0) {
            for (ActSignRewardRecord record : rewardRecords) {
                int activityId = record.getOperateActId();
                Map<Integer, ActSignRewardRecord> recordMap = signRewardMap.get(activityId);
                if (recordMap == null) {
                    recordMap = new HashMap<Integer, ActSignRewardRecord>();
                    signRewardMap.put(activityId, recordMap);
                }
                recordMap.put(record.getNewServerSignId(), record);
            }
        }
    }

    @Override
    public void onInit(boolean isCreation) {
        int initActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerSign);
        opOnInit(initActivityId);

        checkActivityData();

        //标记需要计算红点
        signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_GET);
        signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_REGET);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
        if (redPointIds.contains(Integer.valueOf(RedPointConst.NEW_SERVER_SIGN_GET))) {
            if (curActivity != null && curActivity.hasReward() && operateActivityModule.isShow(curActivity.getId())) {
                redPointMap.put(RedPointConst.NEW_SERVER_SIGN_GET, "");
            } else {
                redPointMap.put(RedPointConst.NEW_SERVER_SIGN_GET, null);
            }
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.NEW_SERVER_SIGN_REGET))) {
            if (curActivity != null && curActivity.hasReGetReward() && operateActivityModule.isShow(curActivity.getId())) {
                redPointMap.put(RedPointConst.NEW_SERVER_SIGN_REGET, "");
            } else {
                redPointMap.put(RedPointConst.NEW_SERVER_SIGN_REGET, null);
            }
        }
    }

    /**
     * 是否在活动有效时间内
     *
     * @return
     */
    public boolean isEffectiveTime() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerSign);
        return isEffectiveTime(curActivityId);
    }

    public boolean isEffectiveTime(int activityId) {
        OperateActVo actVo = OperateActivityManager.getOperateActVo(activityId);
        if (actVo == null) return false;
        ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
        if (!(openTimeBase instanceof ActOpenTime4)) return true;
        ActOpenTime4 openTime4 = (ActOpenTime4) openTimeBase;
        RoleModule roleModule = module(MConst.Role);
        int createRoleDays = roleModule.getRoleCreatedDays();
        return openTime4.isEffectiveTime(openTime4, createRoleDays);
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerSign);
        if (curActivityId != -1) {
            OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap()) && isEffectiveTime()) {
                return curActivityId;
            }
        }

        return -1;
    }

    @Override
    public byte getIsShowLabel() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewServerSign);
        if (curActivityId == -1) return (byte) 0;

        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (operateActVo == null) return (byte) 0;

        LabelDisappearBase labelDisappearBase = operateActVo.getDisappear();
        if (labelDisappearBase == null) return (byte) 0;

        if (labelDisappearBase instanceof NeverDisappear) {
            return (byte) 1;
        } else if (labelDisappearBase instanceof DisappearByDays) {
            ActOpenTimeBase openTime = operateActVo.getActOpenTimeBase();
            if (!(openTime instanceof ActOpenTime4)) return (byte) 0;

            ActOpenTime4 actOpenTime4 = (ActOpenTime4) openTime;
            int startDays = actOpenTime4.getStartDays();
            RoleModule roleModule = module(MConst.Role);
            int createRoleDays = roleModule.getRoleCreatedDays();
            int continueDays = createRoleDays - startDays + 1;
            int canContinueDays = ((DisappearByDays) labelDisappearBase).getDays();
            return continueDays > canContinueDays ? (byte) 0 : (byte) 1;
        } else if (labelDisappearBase instanceof DisappearByTime) {
            Date date = ((DisappearByTime) labelDisappearBase).getDate();
            return date.getTime() < new Date().getTime() ? (byte) 0 : (byte) 1;
        }

        return (byte) 0;
    }

    public void opOnInit(int initActivityId) {
        ClientNewServerSign clientNewServerSign = new ClientNewServerSign();
        clientNewServerSign.setFlag(clientNewServerSign.Flag_ACTIVITY_STATUS);
        if(getCurShowActivityId()!=-1){
            clientNewServerSign.setOpen(true);
        }
        send(clientNewServerSign);
        if (initActivityId == -1) {
            if (curActivity != null) {
                curActivity.close();
                curActivity = null;
            }
        } else {
            if (curActivity != null && curActivity.getId() != initActivityId) {
                curActivity.close();
                curActivity = null;
            }
            if (curActivity == null) {
                OperateActVo vo = OperateActivityManager.getOperateActVo(initActivityId);
                if (vo == null) {
                    com.stars.util.LogUtil.info("NewServerSignModule.opOnInit get no OperateActVo,operateActId=" + initActivityId);
                    return;
                }
                curActivity = new NewServerSignActivity(initActivityId, this);
            }
        }

    }

    public void opOnOpen(int openActivityId) {
        if (curActivity != null && curActivity.getId() != openActivityId) {
            curActivity.close();
            curActivity = null;
        } else if (curActivity != null && curActivity.getId() == openActivityId) {
            return;
        }

        if (curActivity == null) {
            OperateActVo vo = OperateActivityManager.getOperateActVo(openActivityId);
            if (vo == null) {
                com.stars.util.LogUtil.info("NewServerSignModule.opOnOpen get no OperateActVo,operateActId=" + openActivityId);
                return;
            }
            curActivity = new NewServerSignActivity(openActivityId, this);
        }
    }

    public void opOnClose(int closeActivityId) {
        if (curActivity != null && curActivity.getId() == closeActivityId) {
            curActivity.close();
            curActivity = null;
        }
    }

    /**
     * 检查活动数据，把没在进行中的活动数据清掉
     */
    private void checkActivityData() {
        int curActivityId = -1;
        if (curActivity != null) {
            curActivityId = curActivity.getId();
        }

        //活动奖励数据
        Iterator<Map.Entry<Integer, Map<Integer, ActSignRewardRecord>>> it = signRewardMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Map<Integer, ActSignRewardRecord>> entry = it.next();
            int activityId = entry.getKey();
            if (activityId != curActivityId) {
                Map<Integer, ActSignRewardRecord> records = entry.getValue();
                for (ActSignRewardRecord record : records.values()) {
                    context().delete(record);
                }

                it.remove();
            }
        }
    }

    public void handleOperateActivityEvent(OperateActivityEvent event) {
        int opType = event.getActivityType();
        if (opType == OperateActivityConstant.ActType_NewServerSign) {
            ClientNewServerSign clientNewServerSign = new ClientNewServerSign();
            clientNewServerSign.setFlag(clientNewServerSign.Flag_ACTIVITY_STATUS);
            byte flag = event.getFlag();
            int activityId = event.getActivityId();
            if (flag == OperateActivityEvent.Flag_Open_Activity) {
                opOnOpen(activityId);
                clientNewServerSign.setOpen(true);
                //标记需要计算红点
                signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_GET);
                signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_REGET);
            } else if (flag == OperateActivityEvent.Flag_Close_Activity) {
                opOnClose(activityId);
                //标记需要计算红点
                signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_GET);
                signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_REGET);
            }
            send(clientNewServerSign);
        }
    }

    public void handleOperateActivityFlowEvent(OperateActivityFlowEvent event) {
        if (event.getStepType() == OperateActivityConstant.FLOW_STEP_NEW_DAY) {//跨天
            if (curActivity != null) {
                curActivity.getRewardsInfo();
            }

            //标记需要计算红点
            signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_GET);
            signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_REGET);
        }
    }

    public void handleRoleLevelUp() {
        if (getCurShowActivityId() != -1) {
            ClientNewServerSign clientNewServerSign = new ClientNewServerSign();
            clientNewServerSign.setFlag(clientNewServerSign.Flag_ACTIVITY_STATUS);
            clientNewServerSign.setOpen(true);
            send(clientNewServerSign);
        }
        //标记需要计算红点
        signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_GET);
        signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_REGET);
    }

    public void handleForeShowChange() {
        //标记需要计算红点
        signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_GET);
        signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_REGET);
    }

    public void getRewardsInfo(int activityId) {
        if (curActivity == null || curActivity.getId() != activityId) {
            warn("Activity not on");
            return;
        }

        curActivity.getRewardsInfo();
    }

    public void getReward(int activityId, int newServerSignId) {
        if (curActivity == null || curActivity.getId() != activityId) {
            warn("Activity not on");
            return;
        }


        int days = curActivity.getReward(newServerSignId);
        //标记需要计算红点
        signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_GET);
        signCalRedPoint(MConst.NewServerSign, RedPointConst.NEW_SERVER_SIGN_REGET);

        recordCertification(days);
    }

    /**
     * 记录首测活动奖励领取资格(二测发放)
     */
    private void recordCertification(int days) {
        if (days != 7) return;
        try {
            LoginModule loginModule = module(MConst.Login);
            if (loginModule == null) return;
            AccountRow accountRow = loginModule.getAccountRow();
            if (accountRow == null) return;

            accountRow.setFirstTestAwardSign((byte) 1);
            context().update(accountRow);
        } catch (SQLException e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public Map<String, Module> getModulMap() {
        return moduleMap();
    }

    /**
     * 操作数据的方法
     */
    public Map<Integer, ActSignRewardRecord> getRewardRecords(int activityId) {
        return signRewardMap.get(activityId);
    }

    public void insertData(DbRow dbRow) {
        if (dbRow instanceof ActSignRewardRecord) {
            ActSignRewardRecord record = (ActSignRewardRecord) dbRow;
            int activityId = record.getOperateActId();
            Map<Integer, ActSignRewardRecord> recordMap = signRewardMap.get(activityId);
            if (recordMap == null) {
                recordMap = new HashMap<Integer, ActSignRewardRecord>();
                signRewardMap.put(activityId, recordMap);
            }
            recordMap.put(record.getNewServerSignId(), record);

            context().insert(dbRow);
        }
    }

    public void updateData(DbRow dbRow) {
        context().update(dbRow);
    }

    /**
     * 获取运营登入登出日志String
     */
    public String getLoginLogoutLogString() {
        if (curActivity == null) {
            return "";
        }

        int nowDay = curActivity.getOpenDays();
        int maxDay = NewServerSignManager.getMaxDay(curActivity.getId());
        if (nowDay > maxDay) {
            return "";
        }

        StringBuffer stringBuffer = new StringBuffer();

        int haveSign = 1;
        int canSign = 2;
        int canResign = 4;
        int canNotSign = -1;


        stringBuffer.append("day@type:");
        Map<Integer, NewServerSignVo> activityVoMap = NewServerSignManager.getActivityVosMap(curActivity.getId());
        if (activityVoMap != null) {
            for (NewServerSignVo vo : activityVoMap.values()) {
                int day = vo.getDays();
                byte status = curActivity.getSignRewardStatus(vo.getNewServerSignId(), nowDay);
                int logStatus = canNotSign;
                if (status == ActSignRewardRecord.Reward_Status_Cannot_get) {
                    logStatus = canNotSign;
                } else if (status == ActSignRewardRecord.Reward_Status_Can_get) {
                    logStatus = canSign;
                } else if (status == ActSignRewardRecord.Reward_Status_Out_Of_Date) {
                    logStatus = canResign;
                } else if (status == ActSignRewardRecord.Reward_Status_Have_Got) {
                    logStatus = haveSign;
                }

                stringBuffer.append(day);
                stringBuffer.append("@");
                if (logStatus != canNotSign) {
                    stringBuffer.append(logStatus);
                }
                stringBuffer.append("&");
            }
        }
        stringBuffer.append("#");

        return stringBuffer.toString();
    }
}

