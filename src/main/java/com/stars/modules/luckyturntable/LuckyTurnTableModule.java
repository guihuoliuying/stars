package com.stars.modules.luckyturntable;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.luckyturntable.packet.ClientLuckyTurnTable;
import com.stars.modules.luckyturntable.prodata.LuckyTurnTableVo;
import com.stars.modules.luckyturntable.userdata.RoleLuckyTurnTable;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipModule;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.*;

import static com.stars.modules.luckyturntable.LuckyTurnTableManager.*;

/**
 * Created by chenkeyu on 2017-07-12.
 */
public class LuckyTurnTableModule extends AbstractModule implements OpActivityModule {

    private RoleLuckyTurnTable roleLockyTurnTable;
    private int curActivityId;

    public LuckyTurnTableModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleLockyTurnTable = DBUtil.queryBean(DBUtil.DB_USER, RoleLuckyTurnTable.class, "select * from roleluckyturntable where roleid = " + id());
        if (roleLockyTurnTable == null) {
            roleLockyTurnTable = new RoleLuckyTurnTable(id());
            roleLockyTurnTable.setLuckyId("");
            context().insert(roleLockyTurnTable);
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleLockyTurnTable = new RoleLuckyTurnTable(id());
        roleLockyTurnTable.setLuckyId("");
        context().insert(roleLockyTurnTable);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        ForeShowModule open = module(MConst.ForeShow);
        boolean isOpen = open.isOpen(ForeShowConst.LUCKTURNTABLE);
        com.stars.util.LogUtil.info("isOpen:{}", isOpen);
        ServiceHelper.luckyTurnTableService().sendMainIcon(id(), isOpen);
        if (isOpenActivity() && roleLockyTurnTable != null && roleLockyTurnTable.getLuckyIdMap().isEmpty()) {
            putAllLuckyId();
        }
        signCalRedPoint(MConst.LuckyTurnTable, RedPointConst.LUCKYTURNTABLE);
        if (getLong(LUCKYTURNTABLE, 0L) == 0L) {
            long s = System.currentTimeMillis();
            setLong(LUCKYTURNTABLE, s);
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        long s = System.currentTimeMillis();
        Calendar thisCalendar = getCalendar(true);
        Calendar lastCalendar = getCalendar(false);
        if ((now.getTimeInMillis() >= thisCalendar.getTimeInMillis() && getLong(LUCKYTURNTABLE, 0) < thisCalendar.getTimeInMillis())
                || s - getLong(LUCKYTURNTABLE, 0) > 3600 * 24 * 7 * 1000
                || getLong(LUCKYTURNTABLE, 0) < lastCalendar.getTimeInMillis()) {
            roleLockyTurnTable.getLuckyIdMap().clear();
            roleLockyTurnTable.setAccMoney(0);
            roleLockyTurnTable.setLuckyId("");
            int lotteryCount = roleLockyTurnTable.getLottery();
            roleLockyTurnTable.setLottery(0);
            context().update(roleLockyTurnTable);
            if (lotteryCount > 0) {
                Map<Integer, Integer> tmpMap = new HashMap<>();
                tmpMap.put(LuckyTurnTableManager.recycle_ItemId, LuckyTurnTableManager.recycle_Count * lotteryCount);
                ServiceHelper.emailService().sendToSingle(id(), MAIL_ID, 0L, "系统", tmpMap);
            }
            setLong(LUCKYTURNTABLE, s);
            if (isOpenActivity()) {
                putAllLuckyId();
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
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.LUCKYTURNTABLE)) {
            if (!roleLockyTurnTable.getUnTurnIdMap().isEmpty()
                    && roleLockyTurnTable.getLottery() >= getNeedLottery(roleLockyTurnTable.getTurnCount() + 1)) {
                redPointMap.put(RedPointConst.LUCKYTURNTABLE, "");
            } else {
                redPointMap.put(RedPointConst.LUCKYTURNTABLE, null);
            }
        }
    }

    public void putAllLuckyId() {
        RoleModule role = module(MConst.Role);
        VipModule vip = module(MConst.Vip);
        List<Integer> luckyIds = LuckyTurnTableManager.getLockyTurnTableVos(id(), role.getLevel(), vip.getVipLevel());
        roleLockyTurnTable.putAllLuckyId(luckyIds);
        context().update(roleLockyTurnTable);
    }

    public void handleChargeEvent(int money) {
        if (!isOpenActivity()) return;
        roleLockyTurnTable.addAccMoney(money);
        int tmpLottery = roleLockyTurnTable.getAccMoney() / luckWard_Worth;
        int tmpAccMoney = roleLockyTurnTable.getAccMoney() % luckWard_Worth;
        roleLockyTurnTable.addLottery(tmpLottery);
        roleLockyTurnTable.setAccMoney(tmpAccMoney);
        context().update(roleLockyTurnTable);
        signCalRedPoint(MConst.LuckyTurnTable, RedPointConst.LUCKYTURNTABLE);
    }

    public void viewUi() {
        ClientLuckyTurnTable turnTable = new ClientLuckyTurnTable(ClientLuckyTurnTable.turnTable);
        int havCount = roleLockyTurnTable.getTurnCount();
        turnTable.setCount(havCount);
        turnTable.setReqLotteryCount(getNeedLottery(havCount + 1));
        turnTable.setLotteryCount(roleLockyTurnTable.getLottery());
        Map<LuckyTurnTableVo, Byte> tmpMap = new HashMap<>();
        for (LuckyTurnTableVo tableVo : luckyTurnTableMap.values()) {
            if (roleLockyTurnTable.getLuckyIdMap().containsKey(tableVo.getId())) {
                tmpMap.put(tableVo, roleLockyTurnTable.getDRAWAN_OR_NOT(tableVo.getId()));
            }
        }
        turnTable.setItemMap(tmpMap);
        send(turnTable);
        ServiceHelper.luckyTurnTableService().sendLuckyList(id());
    }

    public void turnTable() {
        Set<Integer> itemSet = roleLockyTurnTable.getUnTurnIdMap();
        if (itemSet.isEmpty()) {
            warn("luckyaward_finish");
            return;
        }
        int havCount = roleLockyTurnTable.getTurnCount();
        int needLotteryCount = getNeedLottery(havCount + 1);
        if (roleLockyTurnTable.getLottery() < needLotteryCount) {
            warn("抽奖券不足");
            return;
        }
        int totalPower = 0;
        List<LuckyTurnTableVo> tmpTurnTableVoList = new LinkedList<>();
        for (LuckyTurnTableVo tableVo : luckyTurnTableMap.values()) {
            if (itemSet.contains(tableVo.getId()) && (havCount + 1) >= tableVo.getTimeRange()) {
                totalPower += tableVo.getOdds();
                tmpTurnTableVoList.add(tableVo);
            }
        }
        for (LuckyTurnTableVo turnTableVo : tmpTurnTableVoList) {
            com.stars.util.LogUtil.info("总权值 {} 参与玩家 {} 抽奖的产品数据| {}", totalPower, id(), turnTableVo);
        }
        LuckyTurnTableVo tableVo = getLockyTurnTableVo(totalPower, tmpTurnTableVoList);
        if (tableVo == null) {
            return;
        }
        roleLockyTurnTable.decLottery(needLotteryCount);
        roleLockyTurnTable.updateLockyId(tableVo.getId());
        com.stars.util.LogUtil.info("抽到符合的:{}", tableVo);
        ToolModule tool = module(MConst.Tool);
        tool.addAndSend(tableVo.getItemId(), tableVo.getCount(), EventType.LUCKY_TURN_TABLE.getCode());
        ClientLuckyTurnTable turnTable = new ClientLuckyTurnTable(ClientLuckyTurnTable.winning);
        turnTable.setId(tableVo.getId());
        send(turnTable);
        signCalRedPoint(MConst.LuckyTurnTable, RedPointConst.LUCKYTURNTABLE);
        context().update(roleLockyTurnTable);
    }

    public void announceAndAddLuckyList(int id) {
        LuckyTurnTableVo tableVo = luckyTurnTableMap.get(id);
        if (tableVo != null && tableVo.getDes() == SHOW) {
            RoleModule role = module(MConst.Role);
            String roleName = role.getRoleRow().getName();
            String itemName = DataManager.getGametext(ToolManager.getItemName(tableVo.getItemId()));
            String text0 = String.format(DataManager.getGametext("luckyaward_good_ward_list"), roleName, itemName, tableVo.getCount());
            String text1 = String.format(DataManager.getGametext("luckyaward_good_ward_chuanwen"), roleName, itemName, tableVo.getCount());
            ServiceHelper.luckyTurnTableService().addLuckyList(id(), roleName, tableVo.getItemId(), tableVo.getCount());
            ServiceHelper.chatService().announce(text1);
            com.stars.util.LogUtil.info("这货真幸运|roleId:{} ,text0:{},text1:{}", id(), text0, text1);
        }
    }

    private LuckyTurnTableVo getLockyTurnTableVo(int totalPower, List<LuckyTurnTableVo> tmpTurnTableVoList) {
        int flag = 0;
        int random = new Random().nextInt(totalPower) + 1;
        LogUtil.info("随机数|random:{}", random);
        for (LuckyTurnTableVo tableVo : tmpTurnTableVoList) {
            flag = flag + tableVo.getOdds();
            if (random <= flag) {
                return tableVo;
            }
        }
        return null;
    }

    /**
     * 是否开启活动
     *
     * @return
     */
    public boolean isOpenActivity() {
        curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_LuckyTurnTable);
        if (curActivityId == -1) return false;
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
        return vo != null && operateActivityModule.isShow(vo.getRoleLimitMap());
    }

    @Override
    public int getCurShowActivityId() {
//        if (isOpenActivity()) return curActivityId;
        return -1;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }
}
