package com.stars.modules.newdailycharge;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.newdailycharge.packet.ClientNewDailyCharge;
import com.stars.modules.newdailycharge.prodata.NewDailyChargeInfo;
import com.stars.modules.newdailycharge.userdata.NewRoleDailyCharge;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.opentime.ActOpenTime2;
import com.stars.modules.operateactivity.opentime.ActOpenTime4;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.vip.VipModule;
import com.stars.services.ServiceHelper;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by chenkeyu on 2017-07-10.
 */
public class NewDailyChargeModule extends AbstractModule implements OpActivityModule {
    private NewRoleDailyCharge roleDailyCharge;
    private int curActivityId;

    public NewDailyChargeModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from newroledailycharge where roleid = " + id();
        roleDailyCharge = DBUtil.queryBean(DBUtil.DB_USER, NewRoleDailyCharge.class, sql);
        if (roleDailyCharge == null) {
            roleDailyCharge = new NewRoleDailyCharge(id());
            context().insert(roleDailyCharge);
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleDailyCharge = new NewRoleDailyCharge(id());
        context().insert(roleDailyCharge);
    }

    @Override
    public void onFiveOClockReset(Calendar now) throws Throwable {
        reset();
    }

    private void reset() {
        RoleModule roleModule = module(MConst.Role);
        VipModule vipModule = module(MConst.Vip);
        roleDailyCharge.setDailyLevel(roleModule.getLevel());
        roleDailyCharge.setDailyVipLevel(vipModule.getVipLevel());
        roleDailyCharge.setTotalCharge(0);
        roleDailyCharge.getSendAwardList().clear();
        if (isOpenActivity()) {
            roleDailyCharge.setCurActId(curActivityId);
        } else {
            roleDailyCharge.setCurActId(-1);
        }
        context().update(roleDailyCharge);
        checkAndSendAward();
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {

    }

    public void handleChargeEvent(int money) {
        com.stars.util.LogUtil.info("充值事件|roleId:{},money:{}", id(), money);
        if (!isOpenActivity()) return;
        roleDailyCharge.addTotalCharge(money);
        context().update(roleDailyCharge);
        checkAndSendAward();//检测并发奖
        com.stars.util.LogUtil.info("发奖完毕|roleId:{}", id());
    }

    private void checkAndSendAward() {
        List<NewDailyChargeInfo> list = getRoleDailyChargeList();
        if (StringUtil.isEmpty(list)) return;
        int curCharge = roleDailyCharge.getTotalCharge();
        for (NewDailyChargeInfo info : list) {
            if (curCharge >= info.getTotalCharge() && !roleDailyCharge.hasSendAward(info.getNewDailyTotalId())) {
                sendAward(info);
            }
        }
    }

    private void sendAward(NewDailyChargeInfo info) {
        if (info == null) return;
        DropModule dropModule = module(MConst.Drop);
        Map<Integer, Integer> map = dropModule.executeDrop(info.getReward(), 1, true);
        ServiceHelper.emailService().sendToSingle(id(), NewDailyChargeManager.EMAIL_ID, 0l, "系统", map, String.valueOf(info.getTotalCharge()));
        roleDailyCharge.recordSendAward(info.getNewDailyTotalId());
        context().update(roleDailyCharge);
        com.stars.util.LogUtil.info("新版日累计充值发奖| {} ", roleDailyCharge);
    }

    private List<NewDailyChargeInfo> getRoleDailyChargeList() {
        List<NewDailyChargeInfo> list = new ArrayList<>();
        int curDay = getCurDay();
//        LogUtil.info("curDay:{}", curDay);
        for (NewDailyChargeInfo info : NewDailyChargeManager.getNewDailyChargeInfoMap().values()) {
            if (info.matchLevel(roleDailyCharge.getDailyLevel())
                    && info.matchVipLevel(roleDailyCharge.getDailyVipLevel())
                    && info.matchDays(curDay)
                    && info.getOperateactid() == roleDailyCharge.getCurActId()) {
                LogUtil.info("roleDailyCharge:{}", info);
                list.add(info);
            }
        }
        Collections.sort(list);
        return list;
    }

    private int getCurDay() {
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        if (vo == null) return -1;
        ActOpenTimeBase openTimeBase = vo.getActOpenTimeBase();
        if (openTimeBase == null || !(openTimeBase instanceof ActOpenTime2)) return 0;
        ActOpenTime2 time = (ActOpenTime2) openTimeBase;
        RoleModule roleModule = module(MConst.Role);
        Calendar startCalendar = ActOpenTime2.getStartCalendar(time);
        long now = System.currentTimeMillis();
        Calendar curCalendar = Calendar.getInstance();
        curCalendar.set(Calendar.HOUR_OF_DAY, 5);
        curCalendar.set(Calendar.MINUTE, 0);
        curCalendar.set(Calendar.SECOND, 0);
        curCalendar.set(Calendar.MILLISECOND, 0);
        long resetTimes = curCalendar.getTimeInMillis();
        int plus = 0;
        int curDay = 0;
        Date curDate=new Date(curCalendar.getTimeInMillis());
        Date startDate=new Date(startCalendar.getTimeInMillis());
        curDay= DateUtil.getRelativeDifferDays(startDate,curDate);
        if (now > resetTimes) {
            plus++;
            return curDay + plus;
        } else {
            return curDay + plus;
        }
    }

    /**
     * 是否开启活动
     *
     * @return
     */
    public boolean isOpenActivity() {
        curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_NewDailyCharge);
        if (curActivityId == -1) return false;
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
        if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap()) && isEffectiveTime()) {
            return true;
        }
        return false;
    }

    /**
     * 是否在活动有效时间内
     *
     * @return
     */
    private boolean isEffectiveTime() {
        OperateActVo actVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (actVo == null) return false;
        ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
        if (!(openTimeBase instanceof ActOpenTime4)) return true;
        ActOpenTime4 openTime4 = (ActOpenTime4) openTimeBase;
        RoleModule roleModule = module(MConst.Role);
        int createRoleDays = roleModule.getRoleCreatedDays();
        return openTime4.isEffectiveTime(openTime4, createRoleDays);
    }

    public void viewMainUI() {
        if (!isOpenActivity()) {//活动暂未开启
            warn(I18n.get("marry.wedding.inactivity"));
            return;
        }
        checkAndInit();//检测并初始化

        ClientNewDailyCharge client = new ClientNewDailyCharge();
        client.setTotalCharge(roleDailyCharge.getTotalCharge());
        client.setList(getRoleDailyChargeList());
        client.setBeginTimes(getBeginTimes());
        client.setEndTimes(getEndTimes());
        send(client);
    }

    private void checkAndInit() {
        if (roleDailyCharge.getCurActId() == curActivityId) return;
        reset();//重置至当前活动
    }

    private long getBeginTimes() {
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        if (vo == null) return 0;
        ActOpenTimeBase openTimeBase = vo.getActOpenTimeBase();
        if (openTimeBase == null || !(openTimeBase instanceof ActOpenTime2)) return 0;
        ActOpenTime2 time = (ActOpenTime2) openTimeBase;
        return time.getStartDate().getTime();
    }

    private long getEndTimes() {
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        if (vo == null) return 0;
        ActOpenTimeBase openTimeBase = vo.getActOpenTimeBase();
        if (openTimeBase == null || !(openTimeBase instanceof ActOpenTime2)) return 0;
        ActOpenTime2 time = (ActOpenTime2) openTimeBase;
        return time.getEndDate().getTime();
    }

    @Override
    public int getCurShowActivityId() {
        if (isOpenActivity()) return curActivityId;
        return -1;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }
}
