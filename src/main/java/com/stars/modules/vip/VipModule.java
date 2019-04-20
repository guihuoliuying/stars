package com.stars.modules.vip;

import com.stars.AccountRow;
import com.stars.core.db.DBUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.AccountRowAware;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.userdata.LoginInfo;
import com.stars.modules.drop.DropModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.labeldisappear.DisappearByDays;
import com.stars.modules.operateactivity.labeldisappear.DisappearByTime;
import com.stars.modules.operateactivity.labeldisappear.LabelDisappearBase;
import com.stars.modules.operateactivity.labeldisappear.NeverDisappear;
import com.stars.modules.operateactivity.opentime.*;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.packet.ClientRole;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.packet.ClientChargeSwitchPacket;
import com.stars.modules.vip.packet.ClientVipData;
import com.stars.modules.vip.prodata.ChargeVo;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.modules.vip.userdata.RoleVip;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.ChatManager;
import com.stars.startup.MainStartup;
import com.stars.util.DateUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;
import com.stars.util.TimeUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by liuyuheng on 2016/12/3.
 */
public class VipModule extends AbstractModule implements OpActivityModule, AccountRowAware {

//    public static final String F_NOTIC_COUNT = "vip.noticeCount"; // 滚屏次数

    private RoleVip roleVip;

    private AccountRow accountRow;

    public VipModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("贵族", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleVip = new RoleVip(id());
        context().insert(roleVip);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from `rolevip` where `roleid`=" + id();
        roleVip = DBUtil.queryBean(DBUtil.DB_USER, RoleVip.class, sql);
        if (roleVip == null) {
            roleVip = new RoleVip(id());
            context().insert(roleVip);
        }
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        sendChargeSwitchState();
        // 检查发放vip等级奖励
        vipLevelUpReward(accountRow.getVipLevel(), roleVip.getTotalCharge(), Boolean.FALSE);
        vipCompensate();
    }

    @Override
    public void onSyncData() throws Throwable {
        // vip等级上线滚屏公告
        if (accountRow.getVipLevel() > 0) {
            VipinfoVo vipinfoVo = VipManager.getVipinfoVo(accountRow.getVipLevel());
            if (vipinfoVo == null
                    || StringUtil.isEmptyIncludeZero(vipinfoVo.getOnlineNotice()))
                return;
            RoleModule roleModule = module(MConst.Role);
            if (roleVip.getDailySendAnnouncement() == 0) {
                roleVip.setDailySendAnnouncement((byte) 1);
                context().update(roleVip);
                ServiceHelper.chatService().announce(vipinfoVo.getOnlineNotice(),
                        String.valueOf(accountRow.getVipLevel()), roleModule.getRoleRow().getName());
            }

            ServiceHelper.chatService().chat("系统", ChatManager.CHANNEL_SYSTEM, 0L, 0L,
                    String.format(DataManager.getGametext(vipinfoVo.getOnlineNotice()),
                            accountRow.getVipLevel(), roleModule.getRoleRow().getName()), Boolean.FALSE);
        }
    }

    /**
     * 发送支付开关的状态
     */
    private void sendChargeSwitchState() {
        ClientChargeSwitchPacket clientChargeSwitchPacket = new ClientChargeSwitchPacket();
        send(clientChargeSwitchPacket);

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        updateMonthCardReward();
        roleVip.setMonthCardRewardStatus((byte) 0);
        roleVip.setDailySendAnnouncement((byte) 0);
        roleVip.setDailyChargeSum(0);
        context().update(roleVip);
        sendUpdateVipData(ClientVipData.MONTH_CARD_DAILY_REWARD);
    }

    @Override
    public void setAccountRow(AccountRow accountRow) {
        this.accountRow = accountRow;
    }

    public RoleVip getRoleVip() {
        return roleVip;
    }


    /**
     * 获得当前vip等级配置数据
     *
     * @return
     */
    public VipinfoVo getCurVipinfoVo() {
        return VipManager.getVipinfoVo(accountRow.getVipLevel());
    }


    public int getVipLevel() {
        LoginModule loginModule = module(MConst.Login);
        AccountRow accountRow = null;
        try {
            accountRow = loginModule.getAccountRow();
        } catch (Exception e) {
            com.stars.util.LogUtil.error("", e);
        }
        return accountRow != null ? accountRow.getVipLevel() : 0;
    }

    public int getVipExp() {
        LoginModule loginModule = module(MConst.Login);
        AccountRow accountRow = null;
        try {
            accountRow = loginModule.getAccountRow();
        } catch (Exception e) {
            com.stars.util.LogUtil.error("", e);
        }
        return accountRow != null ? accountRow.getVipExp() : 0;
    }

    /**
     * 下发更新数据
     */
    public void sendUpdateVipData() {
        sendUpdateVipData(ClientVipData.SEND_UPDATE_DATA);
    }

    public void sendUpdateVipData(byte sendType) {
        ClientVipData clientVipData = new ClientVipData(sendType);
        if (sendType == ClientVipData.SEND_UPDATE_DATA) {
            clientVipData.setVipExp(getVipExp());
        }
        clientVipData.setRoleVip(roleVip);
        send(clientVipData);
    }

    /**
     * 发货
     * money额度包括了extraMoney的额度了
     */
    public void consignment(int chargeId, String orderNo, int money, boolean isFirst, byte payPoint, int lastVipLevel) {
        com.stars.util.LogUtil.info("consignment order=" + orderNo);
        Map<Integer, Integer> rewardMap = new HashMap<>();

        Map<Integer, Integer> extraMap = new HashMap<>();

        ChargeVo chargeVo = null;
        if (chargeId != -1) {
            String channel = MainStartup.serverChannel;
            chargeVo = VipManager.getChargeVo(channel, chargeId);

            if (roleVip.isFirstCharge(chargeId)) {
                roleVip.addFirstChargeRecord(chargeId);
                // 首充奖励
                ServiceHelper.emailService().sendToSingle(id(), 11903, 0L, "系统", chargeVo.getFirstChargeAward());
            }
            // 充值奖励
            com.stars.util.MapUtil.add(rewardMap, chargeVo.getChargeAward());

            if (money > chargeVo.getReqRmb()) {
                extraMap.put(ToolManager.GOLD, (money - chargeVo.getReqRmb()) * VipManager.payCardMoney2GoldRate);
            }

            roleVip.addFirstChargeRecord(chargeId);
        } else {
            extraMap.put(ToolManager.GOLD, money * VipManager.payCardMoney2GoldRate);
        }

        com.stars.util.MapUtil.add(rewardMap, extraMap);

        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(rewardMap, EventType.PAY.getCode());
        warn("common_tips_chargesuccess", money + "");
        warn("common_tips_getaward", rewardMap.get(ToolManager.GOLD) + "", ToolManager.getItemName(ToolManager.GOLD));
        roleVip.setTotalCharge(money + roleVip.getTotalCharge());
        roleVip.addDailyCharge(money);
        context().update(roleVip);
        if (chargeId == VipManager.MONTH_CARD_CHARGEID) {
            buyMonthCard();
        }
        try {
            LoginModule lModule = module(MConst.Login);
            LoginInfo info = lModule.getAccountRow().getLoginInfo();
            String uid = "";
            if (info != null) {
                uid = info.getUid();
            } else {
                String tmpStr = lModule.getAccount();
                if (tmpStr.contains("#")) {
                    String[] tmp = tmpStr.split("#");
                    uid = tmp[0];
                } else {
                    uid = tmpStr;
                }
            }

        } catch (Exception e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        }
        sendUpdateVipData(ClientVipData.SEND_UPDATE_DATA);
        com.stars.util.LogUtil.info("launch callback payservice order=" + orderNo);
        ServiceHelper.payService().consignmentCallBack(orderNo, money, chargeId);


    }

    /**
     * 虚拟币专用发货
     */
    public void consignment4Virtual(int chargeId, String orderNo, int money, boolean isFirst, byte payPoint) {
        String channel = MainStartup.serverChannel;
        ChargeVo chargeVo = VipManager.getChargeVo(channel, chargeId);
        if (chargeVo == null)
            return;
        Map<Integer, Integer> rewardMap = new HashMap<>();

        if (roleVip.isFirstCharge(chargeId)) {
            roleVip.addFirstChargeRecord(chargeId);
            // 首充奖励
            ServiceHelper.emailService().sendToSingle(id(), 11903, 0L, "系统", chargeVo.getFirstChargeAward());
        }
        /**
         * 虚拟币充值钻石需为itemid：2
         * 转化奖励
         */
        Map<Integer, Integer> temp = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : chargeVo.getChargeAward().entrySet()) {
            if (entry.getKey() == 1) {
                temp.put(2, entry.getValue());
            } else {
                temp.put(entry.getKey(), entry.getValue());
            }
        }
        // 充值奖励
        com.stars.util.MapUtil.add(rewardMap, temp);
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(rewardMap, EventType.PAY.getCode());
        roleVip.addFirstChargeRecord(chargeId);
        roleVip.setTotalCharge(money + roleVip.getTotalCharge());
        context().update(roleVip);
        if (chargeId == VipManager.MONTH_CARD_CHARGEID) {
            buyMonthCard();
        }
        warn("common_tips_chargesuccess", chargeVo.getReqRmb() + "");
        warn("common_tips_getaward", rewardMap.get(ToolManager.BANDGOLD) + "", ToolManager.getItemName(ToolManager.BANDGOLD));
        sendUpdateVipData(ClientVipData.SEND_UPDATE_DATA);
        com.stars.util.LogUtil.info("launch callback payservice order=" + orderNo);
        ServiceHelper.payService().consignmentCallBack(orderNo, money, chargeId);
    }

    /**
     * 领取月卡每日奖励
     */
    public void rewardMonthCard() {
        // 已领完
        if (roleVip.getMonthCardRest() <= 0) {
            return;
        }
        // 今日奖励已领取
        if (roleVip.getMonthCardRewardStatus() == VipManager.MONTH_CARD_DAILY_REWARDED) {
            return;
        }
        roleVip.setMonthCardRest(roleVip.getMonthCardRest() - 1);
        roleVip.setLastMonthCardRewardTime(System.currentTimeMillis());
        roleVip.setMonthCardRewardStatus(VipManager.MONTH_CARD_DAILY_REWARDED);
        context().update(roleVip);
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(VipManager.monthCardAward, EventType.PAY.getCode());
        sendUpdateVipData(ClientVipData.MONTH_CARD_DAILY_REWARD);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.MONTHCARD_GETAWARD)) {
            calMonthCardGetAward(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.MONTHCARD_RENEW)) {
            calMonthCardRenew(redPointMap);
        }
    }

    private void calMonthCardGetAward(Map<Integer, String> redPointMap) {
        StringBuilder sb = new StringBuilder();
        if (roleVip.getMonthCardRest() > 0) {
            if (roleVip.getMonthCardRewardStatus() < VipManager.MONTH_CARD_DAILY_REWARDED) {
                sb.append(RedPointConst.MONTHCARD_GETAWARD);
            }
        }
        redPointMap.put(RedPointConst.MONTHCARD_GETAWARD,
                sb.toString().isEmpty() ? null : sb.toString());
    }

    private void calMonthCardRenew(Map<Integer, String> redPointMap) {
        StringBuilder sb = new StringBuilder();
        if (roleVip.getMonthCardRest() > 0 && roleVip.getMonthCardRest() <= VipManager.cardContinueDay) {
            sb.append(RedPointConst.MONTHCARD_RENEW);
        }
        redPointMap.put(RedPointConst.MONTHCARD_RENEW,
                sb.toString().isEmpty() ? null : sb.toString());
    }


    /**
     * vip等级提升
     *
     * @param newVipLevel
     * @param totalCharge
     */
    public void vipLevelUpHandler(int newVipLevel, int totalCharge) {
        vipLevelUpReward(newVipLevel, totalCharge, Boolean.TRUE);
        sendUpdateVipData();
        RoleModule roleModule = module(MConst.Role);
        ClientRole clientRole = new ClientRole(ClientRole.UPDATE_VIP_LEVEL, roleModule.getRoleRow());
        clientRole.setVipLevel(newVipLevel);
        send(clientRole);
    }

    /**
     * vip等级奖励
     *
     * @param newVipLevel
     * @param isOnline    是否为在线充值客户
     */
    private void vipLevelUpReward(int newVipLevel, int totalCharge, boolean isOnline) {
        Map<Integer, Integer> immdiateReward = new HashMap<>();
        DropModule dropModule = module(MConst.Drop);
        while (roleVip.getRewardedVipLv() < newVipLevel) {
            Map<Integer, Integer> emailReward = new HashMap<>();
            VipinfoVo vipinfoVo = VipManager.getVipinfoVo(roleVip.getRewardedVipLv() + 1);
            if (vipinfoVo == null) {
                break;
            }
            if (isOnline) {
                Map<Integer, Integer> immediateReward = dropModule.executeDrop(vipinfoVo.getAtonceAward(), 1, true);
                // 立即发放到背包奖励
                com.stars.util.MapUtil.add(emailReward, immediateReward);
            }
            // 邮件发放奖励
            MapUtil.add(emailReward, dropModule.executeDrop(vipinfoVo.getAward(), 1, true));

            int mailId = VipManager.VIP_REWARD_EMAIL_TEMPLEID;
            if (vipinfoVo.getLevel() == 1) {
                mailId = VipManager.VIP_REWARD_EMAIL_TEMPLEID_FIRST;
            }
            if (StringUtil.isNotEmpty(emailReward)) {
                ServiceHelper.emailService().sendToSingle(id(), mailId,
                        Long.valueOf(vipinfoVo.getLevel()), VipManager.VIP_REWARD_EMAIL_SENDER, emailReward,
                        String.valueOf(vipinfoVo.getLevel()));
            }
            roleVip.setRewardedVipLv(roleVip.getRewardedVipLv() + 1);
        }
        context().update(roleVip);
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(immdiateReward, EventType.VIP.getCode());
    }

    /**
     * 改为运营活动
     * 购买月卡
     */
    private void buyMonthCard() {
        if (!isOpActMonthCardOpen()) {
            warn("不在运营活动时间内");
            return;
        }
        if ((roleVip.getLastMonthCardRewardTime() == 0) ||
                (roleVip.getMonthCardRest() == 0 && roleVip.getMonthCardRewardStatus() == 0)) {
            // 今天重置时间
            Calendar nowReset = Calendar.getInstance();
            nowReset.setTimeInMillis(System.currentTimeMillis());
            nowReset.set(Calendar.HOUR_OF_DAY, 0);
            nowReset.set(Calendar.MINUTE, 0);
            nowReset.set(Calendar.SECOND, 0);
            nowReset.set(Calendar.MILLISECOND, 0);
            roleVip.setLastMonthCardRewardTime(nowReset.getTime().getTime() - 3600 * 1000L);
        }
        roleVip.setMonthCardRest(roleVip.getMonthCardRest() + VipManager.monthCardDays);
        context().update(roleVip);
    }

    public boolean isOpActMonthCardOpen() {
        Date now = new Date();
        int openServerDay = DataManager.getServerDays();
        List<OperateActVo> operateActVos = OperateActivityManager.
                getOperateActVoListByType(OperateActivityConstant.ActType_MonthCard);
        OperateActVo operateActVo = operateActVos.get(0);
        if (isOn(operateActVo.getOpen(), operateActVo.getActOpenTimeBase(), now, openServerDay)) {
            return true;
        }
        return false;
    }

    /**
     * 加入时间判断,一般来说永久开放返回值都是true
     *
     * @param isOpen
     * @param openTimeBase
     * @param nowDate
     * @param openServerDay
     * @return
     */
    private boolean isOn(byte isOpen, ActOpenTimeBase openTimeBase, Date nowDate, int openServerDay) {
        if (isOpen == (byte) 0) return false;

        if (openTimeBase == null || nowDate == null) return false;

        if (openTimeBase instanceof ActOpenTime0) {//永久开放
            return true;
        } else if (openTimeBase instanceof ActOpenTime1) {//固定时间开放
            ActOpenTime1 openTime1 = (ActOpenTime1) openTimeBase;
            Date startDate = openTime1.getStartDate();
            Date endDate = openTime1.getEndDate();
            if (!DateUtil.isBetween(nowDate, startDate, endDate)) return false;
            int startDay = DataManager.getServerDays(startDate.getTime());
            return startDay > 0;
        } else if (openTimeBase instanceof ActOpenTime2) {//固定时间+开服前几天内开放
            ActOpenTime2 openTime2 = (ActOpenTime2) openTimeBase;
            Date startDate = openTime2.getStartDate();
            Date endDate = openTime2.getEndDate();
            if (!DateUtil.isBetween(nowDate, startDate, endDate)) return false;
            int limitDay = openTime2.getServerLimitDay();
            if (limitDay == -1) return false;
            int startDay = DataManager.getServerDays(startDate.getTime());
            return startDay > limitDay;
        } else if (openTimeBase instanceof ActOpenTime3) {//开服x~y天内开启
            ActOpenTime3 openTime3 = (ActOpenTime3) openTimeBase;
            int startDays = openTime3.getStartDays();
            int endDays = openTime3.getEndDays();
            if (startDays == -1 || endDays == -1) return false;
            return openServerDay >= startDays && openServerDay <= endDays;
        } else if (openTimeBase instanceof ActOpenTime6) {//开服x天内不开此活动,之后开启
            ActOpenTime6 openTime6 = (ActOpenTime6) openTimeBase;
            int days = openTime6.getDays();
            if (days == -1) return false;
            return openServerDay > days;
        }
        return false;
    }

    /**
     * 更新月卡领取奖励
     */
    private void updateMonthCardReward() {
        // 已领完
        if (roleVip.getMonthCardRest() <= 0) {
            return;
        }
        // 今天重置时间
        Calendar nowReset = Calendar.getInstance();
        nowReset.setTimeInMillis(DateUtil.hourStrTimeToDateTime(DataManager.DAILY_RESET_TIME_STR).getTime());
        // 上次领取的重置时间
        Calendar lastReset = Calendar.getInstance();
        lastReset.setTimeInMillis(roleVip.getLastMonthCardRewardTime());
        lastReset.set(Calendar.HOUR_OF_DAY, nowReset.get(Calendar.HOUR_OF_DAY));
        lastReset.set(Calendar.MINUTE, 0);
        lastReset.set(Calendar.SECOND, 0);
        lastReset.set(Calendar.MILLISECOND, 0);
        byte countDown;
        long endTime;
        // 结束时间
        if (System.currentTimeMillis() < nowReset.getTime().getTime()) {
            endTime = System.currentTimeMillis();
        } else {
            endTime = nowReset.getTime().getTime();
        }
        countDown = (byte) DateUtil.getDaysBetweenTwoDates(lastReset.getTime(), new Date(endTime));
        if (countDown > 0) {
            countDown = (byte) (countDown - 1);
        }
        // 上次未领
        if (roleVip.getLastMonthCardRewardTime() < lastReset.getTime().getTime()
                && roleVip.getMonthCardRewardStatus() == 0) {
            countDown = (byte) (countDown + 1);
        }
        // 补发奖励
        if (countDown != 0) {
            /*ServiceHelper.emailService().sendToSingle(id(), VipManager.MONTHCARD_AWARD_TEMPLEID,
                    Long.valueOf(VipManager.MONTH_CARD_CHARGEID), VipManager.VIP_REWARD_EMAIL_SENDER,
                    VipManager.monthCardAward);*/
            roleVip.setLastMonthCardRewardTime(nowReset.getTime().getTime() - 3600 * 1000L);
            roleVip.setMonthCardRest(Math.max(0, (roleVip.getMonthCardRest() - countDown)));
            context().update(roleVip);
        }
    }

    @Override
    public void onLog() {
    }

    @Override
    public int getCurShowActivityId() {
        return OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_MonthCard);
    }

    @Override
    public byte getIsShowLabel() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_MonthCard);
        if (curActivityId == -1) return (byte) 0;

        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (operateActVo == null) return (byte) 0;

        LabelDisappearBase labelDisappearBase = operateActVo.getDisappear();
        if (labelDisappearBase == null) return (byte) 0;

        if (labelDisappearBase instanceof NeverDisappear) {
            return (byte) 1;
        } else if (labelDisappearBase instanceof DisappearByDays) {
            ActOpenTimeBase openTime = operateActVo.getActOpenTimeBase();
            if (!(openTime instanceof ActOpenTime3)) return (byte) 0;

            ActOpenTime3 actOpenTime3 = (ActOpenTime3) openTime;
            int startDays = actOpenTime3.getStartDays();
            int openServerDays = DataManager.getServerDays();
            int continueDays = openServerDays - startDays + 1;
            int canContinueDays = ((DisappearByDays) labelDisappearBase).getDays();
            return continueDays > canContinueDays ? (byte) 0 : (byte) 1;
        } else if (labelDisappearBase instanceof DisappearByTime) {
            Date date = ((DisappearByTime) labelDisappearBase).getDate();
            return date.getTime() < new Date().getTime() ? (byte) 0 : (byte) 1;
        }

        return (byte) 0;
    }

    /**
     * 为角色月卡添加剩余时间
     *
     * @param days
     */
    public void addMonthCardDays(Integer days) {
        if (!isOpActMonthCardOpen()) {
            warn("不在运营活动时间内");
            return;
        }
        RoleVip roleVip = getRoleVip();
        if ((roleVip.getLastMonthCardRewardTime() == 0) ||
                (roleVip.getMonthCardRest() == 0 && roleVip.getMonthCardRewardStatus() == 0)) {
            // 今天重置时间
            Calendar nowReset = Calendar.getInstance();
            nowReset.setTimeInMillis(System.currentTimeMillis());
            nowReset.set(Calendar.HOUR_OF_DAY, 0);
            nowReset.set(Calendar.MINUTE, 0);
            nowReset.set(Calendar.SECOND, 0);
            nowReset.set(Calendar.MILLISECOND, 0);
            roleVip.setLastMonthCardRewardTime(nowReset.getTime().getTime() - 3600 * 1000L);
        }
        roleVip.setMonthCardRest((byte) (roleVip.getMonthCardRest() + days));
        context().update(roleVip);
    }

    public int getDailyChargeSum() {
        return roleVip.getDailyChargeSum();
    }

    private void vipCompensate() {
        VipinfoVo vipinfoVo = getCurVipinfoVo();
        if (vipinfoVo == null) return;
        String timeStr = DataManager.getCommConfig("vipCompensation_time");
        if (getInt(VipManager.VIPCOMPENSATE) == 1) return;
        LoginModule login = module(MConst.Login);
        try {
            AccountRow accountRow = login.getAccountRow();
            long timestamp = accountRow.getFirstLoginTimestamp();
            if (TimeUtil.toDateString(timestamp).compareTo(timeStr) < 0) {
                Map<Integer, Integer> itemMap = vipinfoVo.getVipCompensationMap();
                if (itemMap.isEmpty()) return;
                ServiceHelper.emailService().sendToSingle(id(), 28301, 0L, "系统", itemMap);
                setInt(VipManager.VIPCOMPENSATE, 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
