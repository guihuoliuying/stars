package com.stars.modules.dailyCharge;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.dailyCharge.packet.ClientDailyCharge;
import com.stars.modules.dailyCharge.prodata.DailyChargeInfo;
import com.stars.modules.dailyCharge.userdata.RoleDailyCharge;
import com.stars.modules.drop.DropModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.opentime.ActOpenTime4;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.vip.VipModule;
import com.stars.services.ServiceHelper;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by wuyuxing on 2017/3/29.
 */
public class DailyChargeModule extends AbstractModule implements OpActivityModule {

    private RoleDailyCharge roleDailyCharge;
    private int curActivityId;

    public DailyChargeModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("DailyCharge", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from roledailycharge where roleid = " + id();
        roleDailyCharge = DBUtil.queryBean(DBUtil.DB_USER, RoleDailyCharge.class, sql);
        if (roleDailyCharge == null) {
            roleDailyCharge = new RoleDailyCharge(id());
            context().insert(roleDailyCharge);
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleDailyCharge = new RoleDailyCharge(id());
        context().insert(roleDailyCharge);//添加插入语句
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {

    }

    @Override
    public void onSyncData() throws Throwable {

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {

    }

    public void handleChargeEvent(int money) {
        if (!isOpenActivity()) return;
        roleDailyCharge.addTotalCharge(money);
        context().update(roleDailyCharge);

        checkAndSendAward();//检测并发奖
    }

    private void checkAndSendAward() {
        List<DailyChargeInfo> list = getRoleDailyChargeList();
        if (StringUtil.isEmpty(list)) return;

        int curCharge = roleDailyCharge.getTotalCharge();
        for (DailyChargeInfo info : list) {
            if (curCharge >= info.getTotalCharge() && !roleDailyCharge.hasSendAward(info.getDailyTotalId())) {
                sendAward(info);
            }
        }
    }

    private void sendAward(DailyChargeInfo info) {
        if (info == null) return;
        DropModule dropModule = module(MConst.Drop);
        Map<Integer, Integer> map = dropModule.executeDrop(info.getReward(), 1, true);
        ServiceHelper.emailService().sendToSingle(id(), DailyChargeManager.MAIL_ID, 0l, "系统", map, String.valueOf(info.getTotalCharge()));

        roleDailyCharge.recordSendAward(info.getDailyTotalId());
        context().update(roleDailyCharge);
    }

    /**
     * 每天凌晨五点重置
     */
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

    /**
     * 是否在活动有效时间内
     *
     * @return
     */
    public boolean isEffectiveTime() {
        OperateActVo actVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (actVo == null) return false;
        ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
        if (!(openTimeBase instanceof ActOpenTime4)) return true;
        ActOpenTime4 openTime4 = (ActOpenTime4) openTimeBase;
        RoleModule roleModule = module(MConst.Role);
        int createRoleDays = roleModule.getRoleCreatedDays();
        return openTime4.isEffectiveTime(openTime4, createRoleDays);
    }

    /**
     * 是否开启活动
     *
     * @return
     */
    public boolean isOpenActivity() {
        curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_DailyCharge);
        if (curActivityId == -1) return false;
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
        if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap()) && isEffectiveTime()) {
            return true;
        }
        return false;
    }

    public void viewMainUI() {
        if (!isOpenActivity()) {//活动暂未开启
            warn(I18n.get("marry.wedding.inactivity"));
            return;
        }
        checkAndInit();//检测并初始化

        ClientDailyCharge client = new ClientDailyCharge(ClientDailyCharge.RESP_VIEW);
        client.setTotalCharge(roleDailyCharge.getTotalCharge());
        client.setList(getRoleDailyChargeList());
        client.setBeginTimes(getBeginTimes());
        client.setEndTimes(getEndTimes());
        send(client);
    }

    private long getBeginTimes() {
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        if (vo == null) return 0;
        ActOpenTimeBase openTimeBase = vo.getActOpenTimeBase();
        if (openTimeBase == null || !(openTimeBase instanceof ActOpenTime4)) return 0;
        ActOpenTime4 time = (ActOpenTime4) openTimeBase;
        RoleModule roleModule = module(MConst.Role);
        return ActOpenTime4.getStartTimes(time, roleModule.getRoleCreatedTime());
    }

    private int getCurDay() {
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        if (vo == null) return -1;
        ActOpenTimeBase openTimeBase = vo.getActOpenTimeBase();
        if (openTimeBase == null || !(openTimeBase instanceof ActOpenTime4)) return 0;
        ActOpenTime4 time = (ActOpenTime4) openTimeBase;
        RoleModule roleModule = module(MConst.Role);
        Calendar startCalendar = ActOpenTime4.getStartCalendar(time, roleModule.getRoleCreatedTime());
        long now = System.currentTimeMillis();
        Calendar curCalendar = Calendar.getInstance();
        curCalendar.set(Calendar.HOUR_OF_DAY, 5);
        curCalendar.set(Calendar.MINUTE,0);
        curCalendar.set(Calendar.SECOND,0);
        curCalendar.set(Calendar.MILLISECOND,0);
        long resetTimes = curCalendar.getTimeInMillis();
        int plus = 0;
        int curDay = 0;
        if (isBefore5Hour(roleModule.getRoleCreatedTime())) {
            plus++;
        }
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

    private boolean isBefore5Hour(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.HOUR_OF_DAY) < 5;
    }

    private long getEndTimes() {
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        if (vo == null) return 0;
        ActOpenTimeBase openTimeBase = vo.getActOpenTimeBase();
        if (openTimeBase == null || !(openTimeBase instanceof ActOpenTime4)) return 0;
        ActOpenTime4 time = (ActOpenTime4) openTimeBase;
        RoleModule roleModule = module(MConst.Role);
        return ActOpenTime4.getEndTimes(time, roleModule.getRoleCreatedTime());
    }

    private List<DailyChargeInfo> getRoleDailyChargeList() {
        List<DailyChargeInfo> list = new ArrayList<>();
        int curDay = getCurDay();
//        LogUtil.info("curDay:{}", curDay);
        for (DailyChargeInfo info : DailyChargeManager.DailyChargeInfoMap.values()) {
            if (info.matchLevel(roleDailyCharge.getDailyLevel())
                    && info.matchVipLevel(roleDailyCharge.getDailyVipLevel())
                    && info.matchDays(curDay)
                    && info.getOperateActId() == roleDailyCharge.getCurActId()) {
                list.add(info);
            }
        }
        Collections.sort(list);
        return list;
    }

    private void checkAndInit() {
        if (roleDailyCharge.getCurActId() == curActivityId) return;
        reset();//重置至当前活动
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
