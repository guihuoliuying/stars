package com.stars.modules.luckydraw;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.luckydraw.packet.ClientLuckyDrawPacket;
import com.stars.modules.luckydraw.pojo.LuckyDrawAnnounce;
import com.stars.modules.luckydraw.prodata.LuckyPumpAwardVo;
import com.stars.modules.luckydraw.userdata.RoleLuckyDrawPo;
import com.stars.modules.luckydraw.userdata.RoleLuckyDrawTimePo;
import com.stars.modules.luckydraw1.LuckyDraw1Module;
import com.stars.modules.luckydraw2.LuckyDraw2Module;
import com.stars.modules.luckydraw3.LuckyDraw3Module;
import com.stars.modules.luckydraw4.LuckyDraw4Module;
import com.stars.modules.operateactivity.*;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.opentime.ActOpenTime5;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class LuckyDrawModule extends AbstractModule implements OpActivityModule, NotSendActivityModule {
    private Map<Integer, RoleLuckyDrawPo> roleLuckyDrawMap = new HashMap<>();
    private RoleLuckyDrawTimePo roleLuckyDrawTime;

    public LuckyDrawModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from roleluckydraw where roleid=%s and type=%s;";
        roleLuckyDrawMap = DBUtil.queryMap(DBUtil.DB_USER, "awardid", RoleLuckyDrawPo.class, String.format(sql, id(), getActType()));
        sql = "select * from roleluckydrawtime where roleid=%s and type=%s;";
        roleLuckyDrawTime = DBUtil.queryBean(DBUtil.DB_USER, RoleLuckyDrawTimePo.class, String.format(sql, id(), getActType()));
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (roleLuckyDrawTime == null) {
            return;
        }
        if (redPointIds.contains(getRedPoint())) {
            LuckyDrawModule luckyDrawModule = module(MConst.LuckyDraw);
            LuckyDraw1Module luckyDraw1Module = module(MConst.LuckyDraw1);
            LuckyDraw2Module luckyDraw2Module = module(MConst.LuckyDraw2);
            LuckyDraw3Module luckyDraw3Module = module(MConst.LuckyDraw3);
            LuckyDraw4Module luckyDraw4Module = module(MConst.LuckyDraw4);
            List<String> statusList = Arrays.asList(luckyDrawModule.getRedPointStatus(), luckyDraw1Module.getRedPointStatus(), luckyDraw2Module.getRedPointStatus(), luckyDraw3Module.getRedPointStatus(), luckyDraw4Module.getRedPointStatus());
            for (String status : statusList) {
                if (status != null) {
                    redPointMap.put(getRedPoint(), "");
                    return;
                }
            }
            redPointMap.put(getRedPoint(), null);
        }
    }

    @Override
    public void onSyncData() throws Throwable {
        if (getActType() == OperateActivityConstant.ActType_LuckyDraw) {
            LuckyDrawModule luckyDrawModule = module(MConst.LuckyDraw);
            LuckyDraw1Module luckyDraw1Module = module(MConst.LuckyDraw1);
            LuckyDraw2Module luckyDraw2Module = module(MConst.LuckyDraw2);
            LuckyDraw3Module luckyDraw3Module = module(MConst.LuckyDraw3);
            LuckyDraw4Module luckyDraw4Module = module(MConst.LuckyDraw4);
            List<Integer> statusList = Arrays.asList(luckyDrawModule.getCurShowActivityId(), luckyDraw1Module.getCurShowActivityId(), luckyDraw2Module.getCurShowActivityId(), luckyDraw3Module.getCurShowActivityId(), luckyDraw4Module.getCurShowActivityId());
            for (int status : statusList) {
                if (status != -1) {
                    return;
                }
            }
            switchReward();
        }

    }

    protected String getRedPointStatus() {
        if (roleLuckyDrawTime == null) {
            return null;
        }
        ToolModule toolModule = module(MConst.Tool);
        long luckyTicketCount = toolModule.getCountByItemId(ToolManager.LUCKY_DRAW_TICKET);
        long time = luckyTicketCount / LuckyDrawManagerFacade.getLuckyDrawConsumeUnit(getActType());
        time += roleLuckyDrawTime.getFreeTime();
        if (time > 0 && roleLuckyDrawTime.getDailyTime() < LuckyDrawManagerFacade.getLuckyDrawNumlimit(getActType())) {
            return "";
        } else {
            return null;
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (roleLuckyDrawTime != null) {
            roleLuckyDrawTime.reset();
            context().update(roleLuckyDrawTime);
            signRedPoint();
        }
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        ClientLuckyDrawPacket clientLuckyDrawPacket = new ClientLuckyDrawPacket(ClientLuckyDrawPacket.SEND_ACTIVITY_STATUS, getActType());
        if (getCurShowActivityId() != -1) {
            clientLuckyDrawPacket.setOpen(true);
            if (roleLuckyDrawMap.size() == 0) {
                for (LuckyPumpAwardVo luckyPumpAwardVo : LuckyDrawManagerFacade.getLuckyPumpAwardMap(getActType()).values()) {
                    RoleLuckyDrawPo roleLuckyDrawPo = new RoleLuckyDrawPo(id(), luckyPumpAwardVo.getId(), getActType());
                    roleLuckyDrawMap.put(luckyPumpAwardVo.getId(), roleLuckyDrawPo);
                    context().insert(roleLuckyDrawPo);
                }
            }
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(getCurShowActivityId());
            ActOpenTime5 actOpenTime5 = (ActOpenTime5) operateActVo.getActOpenTimeBase();
            if (roleLuckyDrawTime == null) {
                roleLuckyDrawTime = new RoleLuckyDrawTimePo(id(), actOpenTime5.getEndDate().getTime(), getActType());
                context().insert(roleLuckyDrawTime);
            }
            if (actOpenTime5.getEndDate().getTime() > roleLuckyDrawTime.getCurrentEndTime()) {
                onActivityReset();
                switchReward();
                onInit(false);
            }
            roleLuckyDrawTime.setCurrentEndTime(actOpenTime5.getEndDate().getTime());
            signRedPoint();
        }else {
            onActivityReset();
        }
        send(clientLuckyDrawPacket);
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(getActType());
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            /**
             * 角色是否被限制
             */
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            if (show) {
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
     * 请求打开主界面的数据
     */
    public void reqMainUiData() {
        sendStaticData();
        sendDynamicData();
    }

    /**
     * 发送静态数据
     */
    public void sendStaticData() {
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(getCurShowActivityId());
        if (operateActVo != null) {
            ClientLuckyDrawPacket staticClientLuckyDrawPacket = new ClientLuckyDrawPacket(ClientLuckyDrawPacket.SEND_MainUiStaticData, getActType());
            staticClientLuckyDrawPacket.setOperateActVo(operateActVo);
            send(staticClientLuckyDrawPacket);
        }
    }

    /**
     * 发送动态数据
     */
    public void sendDynamicData() {
        if (getCurShowActivityId() == -1) {
            warn("不在活动时间内");
            return;
        }
        ToolModule toolModule = module(MConst.Tool);
        long luckyTicketCount = toolModule.getCountByItemId(ToolManager.LUCKY_DRAW_TICKET);
        List<LuckyDrawAnnounce> luckyAnnounceTop10 = ServiceHelper.luckyDrawService().getLuckyAnnounceTop10(getActType());
        ClientLuckyDrawPacket dynamicClientLuckyDrawPacket = new ClientLuckyDrawPacket(ClientLuckyDrawPacket.SEND_MainUiDynamicData, getActType());
        dynamicClientLuckyDrawPacket.setRoleLuckyDrawTime(roleLuckyDrawTime);
        dynamicClientLuckyDrawPacket.setLeftTicketCount((int) luckyTicketCount);
        dynamicClientLuckyDrawPacket.setLuckyAnnounceTop10(luckyAnnounceTop10);
        send(dynamicClientLuckyDrawPacket);
    }

    public void report(int maxTime) {
        Map<Integer, Integer> timeMap = new HashMap<>();
        for (int index = 0; index < maxTime; index++) {
            LuckyPumpAwardVo luckyPumpAwardVo = luckyDraw();
            Integer time = timeMap.get(luckyPumpAwardVo.getId());
            if (time == null) {
                time = 0;
                timeMap.put(luckyPumpAwardVo.getId(), time);
            }
            timeMap.put(luckyPumpAwardVo.getId(), timeMap.get(luckyPumpAwardVo.getId()) + 1);
        }
        try {
            FileWriter fileWriter = new FileWriter("report.txt");
            for (Map.Entry<Integer, Integer> entry : timeMap.entrySet()) {
                fileWriter.write(entry.getKey() + "  " + entry.getValue());
                fileWriter.write("\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 请求抽奖
     *
     * @param times
     */
    public void reqLuckyDraw(int times) {

        if (getCurShowActivityId() == -1) {
            warn("不在活动时间内");
            return;
        }
        if (roleLuckyDrawTime.getDailyTime() + times > LuckyDrawManagerFacade.getLuckyDrawNumlimit(getActType())) {
            warn("luckypump_tips_maxtime");
            return;
        }
        if (times == 0) {
            warn("抽奖次数不得为0");
            return;
        }
        int needCostTicketCount = times * LuckyDrawManagerFacade.getLuckyDrawConsumeUnit(getActType());
        ToolModule toolModule = module(MConst.Tool);
        long luckyTicketCount = toolModule.getCountByItemId(ToolManager.LUCKY_DRAW_TICKET);
        if (needCostTicketCount > luckyTicketCount + roleLuckyDrawTime.getFreeTime()) {
            warn("抽奖券不足");
            return;
        }
        List<LuckyPumpAwardVo> myLuckyDrawAwardVoList = new ArrayList<>();
        for (int time = 0; time < times; time++) {
            LuckyPumpAwardVo luckyPumpAwardVo = luckyDraw();
            myLuckyDrawAwardVoList.add(luckyPumpAwardVo);
        }
        int otherTicketCount = needCostTicketCount - roleLuckyDrawTime.getFreeTime();
        boolean success = false;
        if (otherTicketCount > 0) {
            success = toolModule.deleteAndSend(ToolManager.LUCKY_DRAW_TICKET, otherTicketCount, EventType.LUCKY_DRAW.getCode());
            roleLuckyDrawTime.setFreeTime(0);
        } else {
            roleLuckyDrawTime.setFreeTime(roleLuckyDrawTime.getFreeTime() - times);
            success = true;
        }
        if (success) {
            roleLuckyDrawTime.addTotalDrawTime(times);
            Map<Integer, Integer> itemMap = new HashMap<>();
            for (LuckyPumpAwardVo luckyPumpAwardVo : myLuckyDrawAwardVoList) {
                com.stars.util.MapUtil.add(itemMap, luckyPumpAwardVo.getItemMap());
                if (!StringUtil.isEmpty(luckyPumpAwardVo.getDesc())) {
                    RoleModule roleModule = module(MConst.Role);
                    Role roleRow = roleModule.getRoleRow();
                    LuckyDrawAnnounce luckyDrawAnnounce = new LuckyDrawAnnounce(id(), roleRow.getName(), luckyPumpAwardVo.getId(), getActType());
                    ServiceHelper.luckyDrawService().luckyAnnounce(luckyDrawAnnounce);
                }
            }
            toolModule.addAndSend(itemMap, EventType.LUCKY_DRAW.getCode());
            if (times == 1) {
                ClientLuckyDrawPacket clientLuckyDrawPacket = new ClientLuckyDrawPacket(ClientLuckyDrawPacket.SEND_LuckyDrawOnce, getActType());
                LuckyPumpAwardVo luckyPumpAwardVo = myLuckyDrawAwardVoList.get(0);
                clientLuckyDrawPacket.setRewardId(luckyPumpAwardVo.getId());
                send(clientLuckyDrawPacket);
            } else {
                ClientAward clientAward = new ClientAward(itemMap);
                clientAward.setType((byte) 1);
                send(clientAward);
            }
        }
        sendDynamicData();
        context().update(roleLuckyDrawTime);
        signRedPoint();
    }

    /**
     * 单次抽奖
     *
     * @return
     */
    public LuckyPumpAwardVo luckyDraw() {
        List<LuckyPumpAwardVo> mustDrawList = new ArrayList<>();
        List<LuckyPumpAwardVo> canDrawList = new ArrayList<>();
        generateLuckyDrawPool(mustDrawList, canDrawList);
        List<LuckyPumpAwardVo> myLuckyDrawAwardVoList = new ArrayList<>();
        /**
         * 先处理必中
         */
        if (mustDrawList.size() >= 1) {
            List<LuckyPumpAwardVo> luckyPumpAwardVos = RandomUtil.random(mustDrawList, 1);
            myLuckyDrawAwardVoList.addAll(luckyPumpAwardVos);
        }
        /**
         * 后处理随机
         */
        if (myLuckyDrawAwardVoList.size() == 0) {
            List<LuckyPumpAwardVo> leftluckyDrawAwardVoList = RandomUtil.powerRandom(canDrawList, "odds", 1, true);
            myLuckyDrawAwardVoList.addAll(leftluckyDrawAwardVoList);
        }
        LuckyPumpAwardVo luckyPumpAwardVo = myLuckyDrawAwardVoList.get(0);
        for (Map.Entry<Integer, RoleLuckyDrawPo> entry : roleLuckyDrawMap.entrySet()) {
            RoleLuckyDrawPo roleLuckyDrawPo = entry.getValue();
            if (entry.getKey() == luckyPumpAwardVo.getId()) {
                roleLuckyDrawPo.reset();
                roleLuckyDrawPo.addHitTime();
            } else {
                roleLuckyDrawPo.addNotHit();
            }
            context().update(roleLuckyDrawPo);
        }
        com.stars.util.LogUtil.info("lucky draw:{} hit:{}", id(), luckyPumpAwardVo.getId());
        return luckyPumpAwardVo;
    }


    /**
     * 产生奖品池
     *
     * @param mustDrawList
     * @param canDrawList
     */
    private void generateLuckyDrawPool(List<LuckyPumpAwardVo> mustDrawList, List<LuckyPumpAwardVo> canDrawList) {
        for (LuckyPumpAwardVo luckyPumpAwardVo : LuckyDrawManagerFacade.getLuckyPumpAwardList(getActType())) {
            RoleLuckyDrawPo roleLuckyDrawPo = roleLuckyDrawMap.get(luckyPumpAwardVo.getId());
            boolean canDraw = luckyPumpAwardVo.canDraw(roleLuckyDrawPo, roleLuckyDrawTime);
            boolean mustDraw = luckyPumpAwardVo.mustHit(roleLuckyDrawPo, roleLuckyDrawTime);
            if (canDraw) {
                canDrawList.add(luckyPumpAwardVo);
            }
            if (mustDraw) {
                mustDrawList.add(luckyPumpAwardVo);
            }
        }
    }

    /**
     * 处理充值事件，送奖券
     *
     * @param event
     */
    public void onEvent(Event event) {
        if (event instanceof VipChargeEvent) {
            if (getCurShowActivityId() == -1) {
                return;
            }
            ToolModule toolModule = module(MConst.Tool);
            VipChargeEvent vipChargeEvent = (VipChargeEvent) event;
            int money = vipChargeEvent.getMoney();
            if (LuckyDrawManager.moneyLimit == 0) {
                /**
                 * 配0 则表示不送奖券，奖券为其他产出方式
                 */
                return;
            }
            for (Map.Entry<Integer, Map<Integer, Integer>> entry : LuckyDrawManagerFacade.getMoneyDrawReward(getActType()).entrySet()) {
                int scale = money / entry.getKey();
                if (money >= entry.getKey()) {
                    Map<Integer, Integer> reward = new HashMap<>();
                    for (Map.Entry<Integer, Integer> innerEntry : entry.getValue().entrySet()) {
                        reward.put(innerEntry.getKey(), innerEntry.getValue() * scale);
                    }
                    toolModule.addAndSend(reward, EventType.CHARGE_LUCKY_TICKET.getCode());
                }
            }
        }
        if (event instanceof AddToolEvent) {
            if (getCurShowActivityId() == -1) {
                return;
            }
            AddToolEvent addToolEvent = (AddToolEvent) event;
            if (addToolEvent.getToolMap().containsKey(ToolManager.LUCKY_DRAW_TICKET)) {
                sendDynamicData();
                signRedPoint();
            }
        }
        if (event instanceof OperateActivityEvent) {
            OperateActivityEvent operateActivityEvent = (OperateActivityEvent) event;
            if (operateActivityEvent.getActivityType() == getActType()) {
                if (operateActivityEvent.getFlag() == OperateActivityEvent.Flag_Open_Activity) {
                    com.stars.util.LogUtil.info("activity notice:lucky draw open:{}", id());
                    try {
                        onInit(false);
                    } catch (Throwable throwable) {
                        com.stars.util.LogUtil.error("lucky draw init error!", throwable);
                    }
                } else if (operateActivityEvent.getFlag() == OperateActivityEvent.Flag_Close_Activity) {
                    com.stars.util.LogUtil.info("activity notice:lucky draw close:{},the activity data reset", id());
                    ClientLuckyDrawPacket clientLuckyDrawPacket = new ClientLuckyDrawPacket(ClientLuckyDrawPacket.SEND_ACTIVITY_STATUS, getActType());
                    clientLuckyDrawPacket.setOpen(false);
                    send(clientLuckyDrawPacket);
                    onActivityReset();
                    switchReward();
                }
            }
        }
        if (event instanceof RoleLevelUpEvent) {
            if (getCurShowActivityId() != -1) {
                try {
                    onInit(false);
                } catch (Throwable throwable) {
                    com.stars.util.LogUtil.error(throwable.getMessage(), throwable);
                }
            }
        }
    }

    public void switchReward() {
        ToolModule toolModule = module(MConst.Tool);
        long luckyTicketCount = toolModule.getCountByItemId(ToolManager.LUCKY_DRAW_TICKET);
        com.stars.util.LogUtil.info("remaining {} lucky ticket switch to other item by {}", luckyTicketCount, id());
        if (luckyTicketCount > 0) {
            Map<Integer, Integer> reward = new HashMap<>();
            for (int index = (int) luckyTicketCount; index > 0; index--) {
                MapUtil.add(reward, LuckyDrawManagerFacade.getLuckyDrawSwitch(getActType()));
            }
            toolModule.deleteAndSend(ToolManager.LUCKY_DRAW_TICKET, (int) luckyTicketCount, EventType.LUCKY_DRAW_END_SWITCH.getCode());
            ServiceHelper.emailService().sendToSingle(id(), 28001, 0L, "系统", reward);
        }
        signRedPoint();
    }

    /**
     * 活动重置
     */
    public void onActivityReset() {
        if(roleLuckyDrawTime==null) return;
        com.stars.util.LogUtil.info("roleid:{} trigger lucky draw reset", id());
        String sql1 = "delete  from roleluckydraw where roleid=%s and type=%s;";
        try {
            String sql2 = "delete  from roleluckydrawtime where roleid=%s and type=%s;";
            DBUtil.execBatch(DBUtil.DB_USER, false, String.format(sql1, id(), getActType()), String.format(sql2, id(), getActType()));
            roleLuckyDrawMap = new HashMap<>();
            roleLuckyDrawTime = null;
        } catch (SQLException e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 标记红点
     */
    public void signRedPoint() {
        signCalRedPoint(MConst.LuckyDraw, RedPointConst.LUCKY_DRAW);
        signCalRedPoint(MConst.LuckyDraw1, RedPointConst.LUCKY_DRAW);
        signCalRedPoint(MConst.LuckyDraw2, RedPointConst.LUCKY_DRAW);
        signCalRedPoint(MConst.LuckyDraw3, RedPointConst.LUCKY_DRAW);
        signCalRedPoint(MConst.LuckyDraw4, RedPointConst.LUCKY_DRAW);
    }

    public int getActType() {
        return OperateActivityConstant.ActType_LuckyDraw;
    }

    public int getRedPoint() {
        return RedPointConst.LUCKY_DRAW;
    }


}
