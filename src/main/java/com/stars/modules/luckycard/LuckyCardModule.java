package com.stars.modules.luckycard;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.luckycard.packet.ClientLuckyCardPacket;
import com.stars.modules.luckycard.pojo.LuckyCardAnnounce;
import com.stars.modules.luckycard.pojo.RoleLuckyCardTarget;
import com.stars.modules.luckycard.prodata.LuckyCard;
import com.stars.modules.luckycard.usrdata.RoleLuckyCard;
import com.stars.modules.luckycard.usrdata.RoleLuckyCardBox;
import com.stars.modules.luckycard.usrdata.RoleLuckyCardTime;
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

import java.sql.SQLException;
import java.util.*;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class LuckyCardModule extends AbstractModule implements OpActivityModule, NotSendActivityModule {
    private Map<Integer, RoleLuckyCard> roleLuckyCardMap = new HashMap<>();
    private Map<Integer, RoleLuckyCardBox> roleLuckyCardBoxMap = new HashMap<>();
    private RoleLuckyCardTime roleLuckyCardTime;
    private List<RoleLuckyCardTarget> roleLuckyCardTargets;

    public LuckyCardModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleLuckyCardMap = DBUtil.queryMap(DBUtil.DB_USER, "cardid", RoleLuckyCard.class, "select * from roleluckycard where roleid=" + id());
        roleLuckyCardBoxMap = DBUtil.queryMap(DBUtil.DB_USER, "cardid", RoleLuckyCardBox.class, "select * from roleluckycardbox where roleid=" + id());
        roleLuckyCardTime = DBUtil.queryBean(DBUtil.DB_USER, RoleLuckyCardTime.class, "select * from roleluckycardtime where roleid=" + id());
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        super.onDailyReset(now, isLogin);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        ClientLuckyCardPacket clientLuckyCardPacket = new ClientLuckyCardPacket(ClientLuckyCardPacket.SEND_ACTIVITY_STATUS);
        if (getCurShowActivityId() != -1) {
            clientLuckyCardPacket.setOpen(true);
            if (roleLuckyCardMap.size() == 0) {
                for (LuckyCard luckyCard : LuckyCardManager.allCards) {
                    if (luckyCard.getType() == 1) {
                        RoleLuckyCard roleLuckyCard = new RoleLuckyCard(id(), luckyCard.getId());
                        roleLuckyCardMap.put(roleLuckyCard.getCardId(), roleLuckyCard);
                        context().insert(roleLuckyCard);
                    }
                }
            }
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(getCurShowActivityId());
            ActOpenTime5 actOpenTime5 = (ActOpenTime5) operateActVo.getActOpenTimeBase();
            if (roleLuckyCardTime == null) {
                roleLuckyCardTime = new RoleLuckyCardTime(id(), actOpenTime5.getEndDate().getTime());
                context().insert(roleLuckyCardTime);
            }
            if (actOpenTime5.getEndDate().getTime() > roleLuckyCardTime.getCurrentEndTime()) {
                onActivityReset();
                onInit(false);
            }
            roleLuckyCardTime.setCurrentEndTime(actOpenTime5.getEndDate().getTime());
            signRedPoint();
        } else {
            if (roleLuckyCardTime != null) {
                onActivityReset();
            }
        }
        send(clientLuckyCardPacket);
    }

    private void signRedPoint() {
        signCalRedPoint(MConst.LuckyCard, RedPointConst.LUCKY_CARD_TEMP_BOX);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.LUCKY_CARD_TEMP_BOX)) {
            if (roleLuckyCardBoxMap.size() == 0) {
                redPointMap.put(RedPointConst.LUCKY_CARD_TEMP_BOX, null);
            } else {
                redPointMap.put(RedPointConst.LUCKY_CARD_TEMP_BOX, "");
            }
        }
    }

    public void onEvent(Event event) {
        if (event instanceof VipChargeEvent) {
            if (getCurShowActivityId() == -1) {
                return;
            }
            ToolModule toolModule = module(MConst.Tool);
            VipChargeEvent vipChargeEvent = (VipChargeEvent) event;
            int money = vipChargeEvent.getMoney();
            int unitMoney = LuckyCardManager.basicScale[0];
            int unitTicketCount = LuckyCardManager.basicScale[1];
            Map<Integer, Integer> reward = new HashMap<>();
            if (money >= unitMoney) {
                int scale = money / unitMoney;
                reward.put(ToolManager.LUCKY_CARD_TICKET, unitTicketCount * scale);
            }
            roleLuckyCardTime.addTotalPayCount(money);
            int additionalUnitMoney = LuckyCardManager.additionalScale[0];
            int additionalUnitTicketCount = LuckyCardManager.additionalScale[1];
            Map<Integer, Integer> additionalReward = new HashMap<>();
            if (roleLuckyCardTime.getTotalPayCount() >= additionalUnitMoney) {
                int scale = roleLuckyCardTime.getTotalPayCount() / additionalUnitMoney;
                roleLuckyCardTime.addTotalPayCount(-additionalUnitMoney * scale);
                additionalReward.put(ToolManager.LUCKY_CARD_TICKET, additionalUnitTicketCount * scale);
            }
            com.stars.util.MapUtil.add(reward, additionalReward);
            if (reward.size() != 0) {
                toolModule.addAndSend(reward, EventType.CHARGE_LUCKY_CARD.getCode());
            }
            context().update(roleLuckyCardTime);
        }
        if (event instanceof AddToolEvent) {
            if (getCurShowActivityId() == -1) {
                return;
            }
            AddToolEvent addToolEvent = (AddToolEvent) event;
            Map<Integer, Integer> toolMap = addToolEvent.getToolMap();
            if (toolMap.containsKey(ToolManager.LUCKY_CARD_TICKET)) {
                reqMainData(false);
            }
        }
        if (event instanceof OperateActivityEvent) {
            OperateActivityEvent operateActivityEvent = (OperateActivityEvent) event;
            if (operateActivityEvent.getActivityType() == getActType()) {
                if (operateActivityEvent.getFlag() == OperateActivityEvent.Flag_Open_Activity) {
                    com.stars.util.LogUtil.info("activity notice:lucky card open:{}", id());
                    try {
                        onInit(false);
                    } catch (Throwable throwable) {
                        com.stars.util.LogUtil.error("lucky card init error!", throwable);
                    }
                } else if (operateActivityEvent.getFlag() == OperateActivityEvent.Flag_Close_Activity) {
                    com.stars.util.LogUtil.info("activity notice:lucky card close:{},the activity data reset", id());
                    ClientLuckyCardPacket clientLuckyCardPacket = new ClientLuckyCardPacket(ClientLuckyCardPacket.SEND_ACTIVITY_STATUS);
                    clientLuckyCardPacket.setOpen(false);
                    send(clientLuckyCardPacket);
                    onActivityReset();
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


    private void onActivityReset() {
        com.stars.util.LogUtil.info("roleid:{} trigger lucky card reset", id());
        switchReward();
        String sql1 = "delete  from roleluckycard where roleid=%s ;";
        try {
            String sql2 = "delete  from roleluckycardtime where roleid=%s ;";
            String sql3 = "delete  from roleluckycardbox where roleid=%s ;";
            DBUtil.execBatch(DBUtil.DB_USER, false, String.format(sql1, id()), String.format(sql2, id()), String.format(sql3, id()));
            roleLuckyCardMap = new HashMap<>();
            roleLuckyCardTime = null;
            roleLuckyCardBoxMap = new HashMap<>();
        } catch (SQLException e) {
            com.stars.util.LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 将奖励转换成物品
     */
    private void switchReward() {
        ToolModule toolModule = module(MConst.Tool);
        long luckyTicketCount = toolModule.getCountByItemId(ToolManager.LUCKY_CARD_TICKET);
        com.stars.util.LogUtil.info("remaining {} lucky card ticket switch to other item by {}", luckyTicketCount, id());
        if (luckyTicketCount > 0) {
            Map<Integer, Integer> reward = new HashMap<>();
            for (int index = (int) luckyTicketCount; index > 0; index--) {
                com.stars.util.MapUtil.add(reward, LuckyCardManager.resolveReward);
            }
            toolModule.deleteAndSend(ToolManager.LUCKY_CARD_TICKET, (int) luckyTicketCount, EventType.LUCKY_CARD_END_RESOLVE.getCode());
            ServiceHelper.emailService().sendToSingle(id(), 28201, 0L, "系统", reward);
        }
        if (roleLuckyCardBoxMap.size() != 0) {
            Map<Integer, Integer> reward = new HashMap<>();
            for (RoleLuckyCardBox roleLuckyCardBox : roleLuckyCardBoxMap.values()) {
                Map<Integer, Integer> rewardMap = roleLuckyCardBox.getRewardMap();
                for (int index = 0; index < roleLuckyCardBox.getCount(); index++) {
                    com.stars.util.MapUtil.add(reward, rewardMap);
                }
            }
            ServiceHelper.emailService().sendToSingle(id(), 28202, 0L, "系统", reward);

        }

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

    public int getActType() {
        return OperateActivityConstant.ActType_LuckyCard;
    }

    /**
     * 请求选择卡牌
     *
     * @param cardIds
     */
    public void reqChooseCards(List<Integer> cardIds) {
        if (cardIds.size() != 3) {
            warn("选择卡牌数目错误:" + cardIds.size());
            return;
        }
        int specialCount = 0;
        roleLuckyCardTargets = new ArrayList<>();
        for (Integer cardId : cardIds) {
            LuckyCard luckyCard = LuckyCardManager.luckyCardMap.get(cardId);
            if (luckyCard.getType() == 1) {
                specialCount++;
            }
            roleLuckyCardTargets.add(new RoleLuckyCardTarget(cardId));
        }

        if (specialCount != 1) {
            warn("选择卡牌无效，须一张稀有两张普通");
            return;
        }
        Collections.sort(roleLuckyCardTargets);
        reqMainData(false);
    }


    /**
     * 选择默认卡组
     */
    private void chooseDefaultCardGroup() {
        List<Integer> chooseCardIds = new ArrayList<>();
        chooseCardIds.add(LuckyCardManager.specialCards.get(0).getId());
        chooseCardIds.add(LuckyCardManager.normalCards.get(0).getId());
        chooseCardIds.add(LuckyCardManager.normalCards.get(1).getId());
        roleLuckyCardTargets = new ArrayList<>();
        for (Integer cardId : chooseCardIds) {
            roleLuckyCardTargets.add(new RoleLuckyCardTarget(cardId));
        }
        Collections.sort(roleLuckyCardTargets);
    }

    /**
     * 请求抽奖
     *
     * @param time
     */
    public void reqLuckyGo(int time) {
        ToolModule toolModule = module(MConst.Tool);
        long count = toolModule.getCountByItemId(ToolManager.LUCKY_CARD_TICKET);
        if (time < 1 || count < time * LuckyCardManager.luckyCardConsumeUnit) {
            warn("无效抽奖次数");
            return;
        }
        List<Integer> cardIds = new ArrayList<>();
        if (time == 1) {
            int cardId = luckyGo();
            cardIds.add(cardId);
            ClientLuckyCardPacket clientLuckyCardPacket = new ClientLuckyCardPacket(ClientLuckyCardPacket.SEND_LUCKY_1);
            clientLuckyCardPacket.setCardIds(cardIds);
            send(clientLuckyCardPacket);
        } else {
            for (int index = 0; index < time; index++) {
                int cardId = luckyGo();
                cardIds.add(cardId);
                LuckyCard luckyCard = LuckyCardManager.luckyCardMap.get(cardId);
                if (luckyCard.getType() == 1) {
                    break;
                }
            }
            ClientLuckyCardPacket clientLuckyCardPacket = new ClientLuckyCardPacket(ClientLuckyCardPacket.SEND_LUCKY_MORE);
            clientLuckyCardPacket.setCardIds(cardIds);
            clientLuckyCardPacket.setTime(time);
            send(clientLuckyCardPacket);
        }
        if (toolModule.deleteAndSend(ToolManager.LUCKY_CARD_TICKET, cardIds.size() * LuckyCardManager.luckyCardConsumeUnit, EventType.LUCKY_CARD.getCode())) {
            StringBuilder sb = new StringBuilder("luckycard->roleid:{} req lucky time:{},reward into temp box->rolelcukycardbox:[");
            for (int cardId : cardIds) {
                sb.append(cardId).append("+");
                RoleLuckyCardBox roleLuckyCardBox = roleLuckyCardBoxMap.get(cardId);
                if (roleLuckyCardBox == null) {
                    roleLuckyCardBox = new RoleLuckyCardBox(id(), cardId, 1, LuckyCardManager.luckyCardMap.get(cardId).getItem());
                    roleLuckyCardBoxMap.put(cardId, roleLuckyCardBox);
                    context().insert(roleLuckyCardBox);
                } else {
                    roleLuckyCardBox.addCount(1);
                    context().update(roleLuckyCardBox);
                }
            }
            sb.append("]");
            com.stars.util.LogUtil.info(sb.toString());
        }
        signRedPoint();
        reqMainData(false);
    }

    /**
     * 抽一次
     *
     * @return 抽中的卡id
     */
    public int luckyGo() {
        RoleLuckyCardTarget roleLuckyCardTarget_A = getRoleLuckyCardTargets().get(0);
        int specialCardId = roleLuckyCardTarget_A.getCardId();
        RoleLuckyCard specialRoleLuckyCard = roleLuckyCardMap.get(specialCardId);
        LuckyCard luckyCard = LuckyCardManager.luckyCardMap.get(specialCardId);
        int hitCardId = 0;
        if (specialRoleLuckyCard.getNotHit() >= luckyCard.getFullget()) {
            hitCardId = specialCardId;
        } else {
            /**
             * 计算权值
             */
            int specialOdds = luckyCard.getOdds() * (specialRoleLuckyCard.getNotHit() + 1);
            if (specialOdds >= 1000) {
                hitCardId = specialCardId;
            } else {
                roleLuckyCardTarget_A.setOdds(specialOdds);
                RoleLuckyCardTarget roleLuckyCardTarget_B = getRoleLuckyCardTargets().get(1);
                LuckyCard luckyCard_B = roleLuckyCardTarget_B.getLuckyCard();
                RoleLuckyCardTarget roleLuckyCardTarget_C = getRoleLuckyCardTargets().get(2);
                LuckyCard luckyCard_C = roleLuckyCardTarget_C.getLuckyCard();
                roleLuckyCardTarget_B.setOdds((1000 - specialOdds) * luckyCard_B.getOdds() / (luckyCard_B.getOdds() + luckyCard_C.getOdds()));
                roleLuckyCardTarget_C.setOdds((1000 - roleLuckyCardTarget_A.getOdds() - roleLuckyCardTarget_B.getOdds()));
                com.stars.util.LogUtil.info("lucky card odds:--->A cardid:{} odds:{},B cardid:{} odds:{},C cardid:{} odds:{}", roleLuckyCardTarget_A.getCardId(), roleLuckyCardTarget_A.getOdds(), roleLuckyCardTarget_B.getCardId(), roleLuckyCardTarget_B.getOdds(), roleLuckyCardTarget_C.getCardId(), roleLuckyCardTarget_C.getOdds());
                List<RoleLuckyCardTarget> roleLuckyCardTargets = RandomUtil.powerRandom(getRoleLuckyCardTargets(), "odds", 1, false);
                hitCardId = roleLuckyCardTargets.get(0).getCardId();
            }
        }
        if (hitCardId != specialCardId) {
            specialRoleLuckyCard.addNotHit();
        } else {
            specialRoleLuckyCard.setNotHit(0);
        }
        LuckyCard hitLuckyCard = LuckyCardManager.luckyCardMap.get(hitCardId);
        if (!StringUtil.isEmpty(hitLuckyCard.getMessage())) {
            RoleModule roleModule = module(MConst.Role);
            Role roleRow = roleModule.getRoleRow();
            ServiceHelper.luckyCardService().luckyAnnounce(new LuckyCardAnnounce(id(), roleRow.getName(), hitCardId));
        }
        context().update(specialRoleLuckyCard);
        return hitCardId;
    }

    /**
     * 请求主界面数据
     *
     * @param includeProduct
     */
    public void reqMainData(boolean includeProduct) {
        ClientLuckyCardPacket clientLuckyCardPacket = new ClientLuckyCardPacket(ClientLuckyCardPacket.SEND_MAIN_UI_DATA);
        clientLuckyCardPacket.setIncludeProduct(includeProduct);
        ToolModule toolModule = module(MConst.Tool);
        long remainTicketCount = toolModule.getCountByItemId(ToolManager.LUCKY_CARD_TICKET);
        clientLuckyCardPacket.setRemainTicketCount((int) remainTicketCount);
        RoleLuckyCardTarget roleLuckyCardTarget_A = getRoleLuckyCardTargets().get(0);
        RoleLuckyCard specialRoleLuckyCard = roleLuckyCardMap.get(roleLuckyCardTarget_A.getCardId());
        clientLuckyCardPacket.setSpecialRoleLuckyCard(specialRoleLuckyCard);
        clientLuckyCardPacket.setRoleLuckyCardTargets(getRoleLuckyCardTargets());
        send(clientLuckyCardPacket);
    }


    /**
     * 打开暂存箱
     */
    public void reqOpenTempBox() {
        Collection<RoleLuckyCardBox> roleLuckyCardBoxes = roleLuckyCardBoxMap.values();
        ClientLuckyCardPacket clientLuckyCardPacket = new ClientLuckyCardPacket(ClientLuckyCardPacket.SEND_OPEN_TEMP_BOX);
        clientLuckyCardPacket.setRoleLuckyCardBoxes(roleLuckyCardBoxes);
        send(clientLuckyCardPacket);
    }

    /**
     * 请求分解暂存箱物品
     *
     * @param cardId
     */
    public void reqResolve(int cardId) {
        /**
         * -1表示获取所有
         */
        StringBuilder sb = new StringBuilder("luckycard->roleid:{} req resolve,reward from temp box:[");
        Map<Integer, Integer> reward = new HashMap<>();
        if (cardId == -1) {
            for (RoleLuckyCardBox roleLuckyCardBox : roleLuckyCardBoxMap.values()) {
                int cardIdTmp = roleLuckyCardBox.getCardId();
                sb.append(cardIdTmp).append("+").append(roleLuckyCardBox.getCount()).append(",");
                int count = roleLuckyCardBox.getCount();
                LuckyCard luckyCard = LuckyCardManager.luckyCardMap.get(cardIdTmp);
                for (int index = 0; index < count; index++) {
                    com.stars.util.MapUtil.add(reward, luckyCard.getResolveReward());
                }
                context().delete(roleLuckyCardBox);
            }
            roleLuckyCardBoxMap.clear();

        } else {
            RoleLuckyCardBox roleLuckyCardBox = roleLuckyCardBoxMap.get(cardId);
            sb.append(cardId).append("+").append(roleLuckyCardBox.getCount()).append(",");
            int count = roleLuckyCardBox.getCount();
            LuckyCard luckyCard = LuckyCardManager.luckyCardMap.get(cardId);
            for (int index = 0; index < count; index++) {
                com.stars.util.MapUtil.add(reward, luckyCard.getResolveReward());
            }
            context().delete(roleLuckyCardBox);
            roleLuckyCardBoxMap.remove(cardId);
        }
        sb.append("]");
        com.stars.util.LogUtil.info(sb.toString());
        signRedPoint();
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(reward, EventType.LUCKY_CARD_GET.getCode());
        ClientAward clientAward = new ClientAward(reward);
        clientAward.setType((byte) 1);
        send(clientAward);
        reqOpenTempBox();
    }

    /**
     * 请求取出暂存箱物品
     *
     * @param cardId
     */
    public void reqGet(int cardId) {
        /**
         * -1表示获取所有
         */
        StringBuilder sb = new StringBuilder("luckycard->roleid:{} req get,reward from temp box:[");
        Map<Integer, Integer> reward = new HashMap<>();
        if (cardId == -1) {
            for (RoleLuckyCardBox roleLuckyCardBox : roleLuckyCardBoxMap.values()) {
                int cardIdTmp = roleLuckyCardBox.getCardId();
                sb.append(cardIdTmp).append("+").append(roleLuckyCardBox.getCount()).append(",");
                int count = roleLuckyCardBox.getCount();
                LuckyCard luckyCard = LuckyCardManager.luckyCardMap.get(cardIdTmp);
                for (int index = 0; index < count; index++) {
                    com.stars.util.MapUtil.add(reward, luckyCard.getReward());
                }
                context().delete(roleLuckyCardBox);
            }
            roleLuckyCardBoxMap.clear();

        } else {
            RoleLuckyCardBox roleLuckyCardBox = roleLuckyCardBoxMap.get(cardId);
            sb.append(cardId).append("+").append(roleLuckyCardBox.getCount()).append(",");
            int count = roleLuckyCardBox.getCount();
            LuckyCard luckyCard = LuckyCardManager.luckyCardMap.get(cardId);
            for (int index = 0; index < count; index++) {
                MapUtil.add(reward, luckyCard.getReward());
            }
            context().delete(roleLuckyCardBox);
            roleLuckyCardBoxMap.remove(cardId);
        }
        sb.append("]");
        LogUtil.info(sb.toString());
        signRedPoint();
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(reward, EventType.LUCKY_CARD_GET.getCode());
        ClientAward clientAward = new ClientAward(reward);
        clientAward.setType((byte) 1);
        send(clientAward);
        reqOpenTempBox();

    }

    public List<RoleLuckyCardTarget> getRoleLuckyCardTargets() {
        if (roleLuckyCardTargets == null) {
            chooseDefaultCardGroup();
        }
        return roleLuckyCardTargets;
    }


}
