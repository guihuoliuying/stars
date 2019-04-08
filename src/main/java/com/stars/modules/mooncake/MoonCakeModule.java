package com.stars.modules.mooncake;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.mooncake.packet.ClientMoonCake;
import com.stars.modules.mooncake.prodata.moonCakeRwdVo;
import com.stars.modules.mooncake.userdata.RoleMoonCakePo;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.opentime.ActOpenTime5;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.services.ServiceHelper;
import com.stars.services.actloopreset.event.ActLoopResetEvent;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangerjiang on 2017/9/14.
 */
public class MoonCakeModule extends AbstractModule implements OpActivityModule {
    private int curActivityId;
    private long validity;
    private RoleMoonCakePo roleMoonCakePo;

    private long beginTime;
    private boolean isSendFinishPacket = false;


    public MoonCakeModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    /**
     * 是否在活动有效时间内
     *
     * @return
     */
    public boolean isEffectiveTime() {
        OperateActVo actVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (actVo == null) {
            return false;
        }
        ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
        if (!(openTimeBase instanceof ActOpenTime5)) {
            return false;
        }
        ActOpenTime5 openTime5 = (ActOpenTime5) openTimeBase;
        // 有效时间
        validity = openTime5.getEndDate().getTime();
        return DateUtil.isBetween(new Date(), openTime5.getStartDate(), openTime5.getEndDate());

    }

    /**
     * 是否开启活动
     *
     * @return
     */
    public boolean isOpenActivity() {
        curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_MoonCake);
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
            warn(I18n.get("mooncakeModule.wedding.inactivity"));
            return;
        }
        com.stars.util.LogUtil.info("viewMainUi:{}", roleMoonCakePo);
        ClientMoonCake client = new ClientMoonCake(ClientMoonCake.RES_VIEW);
        client.setDaySingleMaxScore(roleMoonCakePo.getiDaySingleMaxScore());
        client.setWeekSingleMaxScore(roleMoonCakePo.getiWeekSingleMaxScore());
        client.setMoonCakeRank(0);
        client.setTargetScoreRwdMap(roleMoonCakePo.getTargetScoreRwdMap());
        ServiceHelper.moonCakeService().view(id(), client);
//        send(client);
        com.stars.util.LogUtil.info("after|viewMainUi:{}", roleMoonCakePo);
    }

    private void resetTargetScoreRwdState() {
        for (Map.Entry<Integer, moonCakeRwdVo> entry : MoonCakeManager.dayScoreRwdMap.entrySet()) {
            roleMoonCakePo.updateTargetScoreRwdState(entry.getKey(), -1);
        }
        context().update(roleMoonCakePo);
    }

    public void beginMoonCakeGame() {
        if (!isOpenActivity()) {
            com.stars.util.LogUtil.info("进入游戏时活动已经结束|roleId:{},", id());
            warn("进入游戏|非法的游戏时间");
            return;
        }
        beginTime = System.currentTimeMillis();
//        isSendFinishPacket = false;
//        roleMoonCakePo.setBeginTime(nowTime);
//        context().update(roleMoonCakePo);
    }

    @Override
    public void onTimingExecute() {
//        System.out.println("这是测试热更");
//        if (beginTime <= 0L) return;
//        long nowTime = System.currentTimeMillis();
//        if (nowTime - beginTime / 1000 >= MoonCakeManager.iLastTime && !isSendFinishPacket) {
//            ClientMoonCake clientMoonCake = new ClientMoonCake(ClientMoonCake.RES_FINISH_GAME);
//            send(clientMoonCake);
//            isSendFinishPacket = true;
//        }
    }

    public void endMoonCakeGame(int score) {
        updateMaxScore(score);
        beginTime = 0L;
        context().update(roleMoonCakePo);
    }

    public void updateMaxScore(int iScore) {
//        long beginTime = roleMoonCakePo.getBeginTime();
        if (!isOpenActivity()) {
            com.stars.util.LogUtil.info("退出游戏时活动已经结束|roleId:{},score:{}", id(), iScore);
            warn("非法的游戏时间");
            ClientMoonCake clientMoonCake = new ClientMoonCake(ClientMoonCake.RES_FINISH);
            clientMoonCake.setIsNewRecord(3);
            send(clientMoonCake);
            return;
        }
        long nowTime = System.currentTimeMillis();
        long diffTime = (nowTime - beginTime) / 1000;
        int daySingleMaxScore = 0;
        boolean isNewRecord = false;
        if (diffTime > MoonCakeManager.MAX_TIME) {
            //超出单局最长时间，记录不生效，作弊行为
            com.stars.util.LogUtil.info("玩家更新单次接月饼积分时间异常|roleid:{}|score:{}|lastTime:{}", id(), iScore, diffTime);
            ClientMoonCake clientMoonCake = new ClientMoonCake(ClientMoonCake.RES_FINISH);
            clientMoonCake.setIsNewRecord(2);
            send(clientMoonCake);
            warn("单局时间过久");
            return;
        }
        if (iScore > MoonCakeManager.iCanGetMaxScore) {
            com.stars.util.LogUtil.info("玩家更新单次接月饼积分上限异常|roleid:{}|score:{}|lastTime:{}", id(), iScore, diffTime);
            warn("单局积分超出上限");
            return;
        }
        daySingleMaxScore = roleMoonCakePo.getiDaySingleMaxScore();
        if (iScore > daySingleMaxScore) {
            isNewRecord = true;
            roleMoonCakePo.setiDaySingleMaxScore(iScore);
        }
        int weekSingleMaxScore = roleMoonCakePo.getiWeekSingleMaxScore();
        if (iScore > weekSingleMaxScore) {
            roleMoonCakePo.setiWeekSingleMaxScore(iScore);
            ServiceHelper.moonCakeService().updateMaxWeeklyScore(id(), iScore);
        }
        setTargetScoreRwdMapState(iScore);
        context().update(roleMoonCakePo);
        ClientMoonCake clientMoonCake = new ClientMoonCake(ClientMoonCake.RES_FINISH);
        clientMoonCake.setLastPoint(daySingleMaxScore);
        clientMoonCake.setThisPoint(iScore);
        clientMoonCake.setIsNewRecord(isNewRecord ? 1 : 0);
        send(clientMoonCake);
        signCalRedPoint(MConst.MoonCake, RedPointConst.MOON_CAKE);
    }

    public void setTargetScoreRwdMapState(int score) {
        for (Map.Entry<Integer, Integer> entry : roleMoonCakePo.getTargetScoreRwdMap().entrySet()) {
            if (entry.getKey() <= score && entry.getValue() == -1) {
                roleMoonCakePo.updateTargetScoreRwdState(entry.getKey(), 1);
            }
        }
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.MOON_CAKE)) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Integer, Integer> entry : roleMoonCakePo.getTargetScoreRwdMap().entrySet()) {
                if (entry.getValue() != 2) {
                    sb.append(entry.getKey()).append("+");
                }
            }
            if (sb.length() <= 0) {
                redPointMap.put(RedPointConst.MOON_CAKE, null);
            } else {
                redPointMap.put(RedPointConst.MOON_CAKE, sb.toString());
            }
        }
    }

    @Override
    public void onOffline() throws Throwable {

    }

    public void getScoreReward(int iScore) {
        int state = roleMoonCakePo.getTargetScoreRwdMap().get(iScore);

        if (state == 1) {//可领取
            moonCakeRwdVo rwditem = MoonCakeManager.getDayScoreRwdMap(iScore);

            //奖励前端提示
            ClientAward clientAward = new ClientAward();
            clientAward.setAward(rwditem.getItemId(), rwditem.getCount());
            send(clientAward);

            //奖励入背包
            ToolModule tool = module(MConst.Tool);
            tool.addAndSend(rwditem.getItemId(), rwditem.getCount(), EventType.MOON_CAKE.getCode());

            roleMoonCakePo.updateTargetScoreRwdState(iScore, 2);


            ClientMoonCake client = new ClientMoonCake(ClientMoonCake.RES_GETRWD);
            client.setTargetScoreRwdMap(roleMoonCakePo.getTargetScoreRwdMap());
            send(client);

        } else if (state == 0) {  //未领取
            com.stars.util.LogUtil.info("接月饼积分奖励0状态不处理|roleid:{}|stage:{}", id(), state);
        } else if (state == -1) { //没达到领取条件
            warn("没达到领取条件，不可领取");
        } else if (state == 2) {  //已经领取
            warn("奖励已领取，不可重复领取");
            com.stars.util.LogUtil.info("玩家已经领取过接月饼积分奖励|roleid:{}|stage:{}", id(), state);
        } else {
            com.stars.util.LogUtil.info("玩家领取接月饼积分奖励状态异常|roleid:{}|stage:{}|score:{}", id(), state, iScore);
        }
        context().update(roleMoonCakePo);
        signCalRedPoint(MConst.MoonCake, RedPointConst.MOON_CAKE);
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        reset();
        long s = System.currentTimeMillis();
        Calendar thisCalendar = getCalendar(true);
        Calendar lastCalendar = getCalendar(false);
        if ((now.getTimeInMillis() >= thisCalendar.getTimeInMillis() && getLong(MoonCakeConst.MOON_CAKE_RESET_WEEK, 0) < thisCalendar.getTimeInMillis())
                || s - getLong(MoonCakeConst.MOON_CAKE_RESET_WEEK, 0) > 3600 * 24 * 7 * 1000
                || getLong(MoonCakeConst.MOON_CAKE_RESET_WEEK, 0) < lastCalendar.getTimeInMillis()) {
            roleMoonCakePo.setiWeekSingleMaxScore(0);
            roleMoonCakePo.resetTarget();
            setLong(MoonCakeConst.MOON_CAKE_RESET_WEEK, s);
            context().update(roleMoonCakePo);
            ServiceHelper.moonCakeService().removeFromRank(id());
            if (isOpenActivity()) {
                if (roleMoonCakePo.getTargetScoreRwdMap().isEmpty()) {
                    resetTargetScoreRwdState();
                }
            }
        }
    }

    private Calendar getCalendar(boolean thisOrLast) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (!thisOrLast) {
            calendar.add(Calendar.WEEK_OF_MONTH, -1);
        }
        com.stars.util.LogUtil.info("周三时间点 {} ", calendar.getTimeInMillis());
        return calendar;
    }

    @Override
    public void onDataReq() throws Throwable {
        initRoleMoonCakePo();
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (roleMoonCakePo == null) {
            roleMoonCakePo = new RoleMoonCakePo();
            roleMoonCakePo.setRoleId(id());
            roleMoonCakePo.setiWeekSingleMaxScore(0);
//            roleMoonCakePo.setiMoonCakeRank(0);
            roleMoonCakePo.setsTargetReward("");
            context().insert(roleMoonCakePo);
            com.stars.util.LogUtil.info("roleMoonCakePo:{}", roleMoonCakePo);
        }
        if (isOpenActivity() && roleMoonCakePo.getTargetScoreRwdMap().size() == 0) {
            resetTargetScoreRwdState();
        }
        if (getLong(MoonCakeConst.MOON_CAKE_RESET_WEEK, 0L) == 0L) {
            long s = System.currentTimeMillis();
            setLong(MoonCakeConst.MOON_CAKE_RESET_WEEK, s);
        }
        signCalRedPoint(MConst.MoonCake, RedPointConst.MOON_CAKE);
    }

    private void initRoleMoonCakePo() {
        String sql = "select * from rolemooncake where roleid=" + id();
        try {
            roleMoonCakePo = DBUtil.queryBean(DBUtil.DB_USER, RoleMoonCakePo.class, sql);
            com.stars.util.LogUtil.info("roleMoonCakePo:{}", roleMoonCakePo);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void reset() {
        roleMoonCakePo.setiDaySingleMaxScore(0);
        roleMoonCakePo.setsTargetReward("");
        resetTargetScoreRwdState();
    }

    @Override
    public int getCurShowActivityId() {
        if (isOpenActivity())
            return curActivityId;
        return 0;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    public void onEvent(Event event) {
        if(event instanceof ActLoopResetEvent){
            try {
                onDataReq();
                onInit(false);
            } catch (Throwable throwable) {
                com.stars.util.LogUtil.error("actLoopReset:4018 fail:" + id(), throwable);
            }
        }
        if (event instanceof OperateActivityEvent) {
            OperateActivityEvent operateActivityEvent = (OperateActivityEvent) event;
            if (operateActivityEvent.getActivityType() == 4018) {
                if (operateActivityEvent.getFlag() == OperateActivityEvent.Flag_Open_Activity) {
                    com.stars.util.LogUtil.info("activity notice:{} open:{}", MConst.MoonCake, id());
                    try {
                        onInit(false);
                    } catch (Throwable throwable) {
                        LogUtil.error(MConst.MoonCake + " init error!", throwable);
                    }
                } else if (operateActivityEvent.getFlag() == OperateActivityEvent.Flag_Close_Activity) {
                    reset();
                }
            }
        }
    }
}
