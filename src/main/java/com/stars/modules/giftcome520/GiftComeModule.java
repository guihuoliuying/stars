package com.stars.modules.giftcome520;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.giftcome520.packet.ClientGiftComePacket;
import com.stars.modules.giftcome520.userdata.RoleGiftCome520Po;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/4/15.
 */
public class GiftComeModule extends AbstractModule implements OpActivityModule {
    private RoleGiftCome520Po roleGiftCome520;

    public GiftComeModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        super.onDataReq();
        if (getCurShowActivityId() == -1) {
            return;
        }
        String sql = "select * from rolegiftcome520 where  roleid=" + id();
        roleGiftCome520 = DBUtil.queryBean(DBUtil.DB_USER, RoleGiftCome520Po.class, sql);
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleGiftCome520 = new RoleGiftCome520Po(id(), "");
        context().insert(roleGiftCome520);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        /**
         * 不在活动时间内让npc走普通npc逻辑对话
         */
        if (getCurShowActivityId() == -1) {
            ClientGiftComePacket clientGiftComePacket = new ClientGiftComePacket(ClientGiftComePacket.SEND_UI_RESOURCE);
            clientGiftComePacket.setBtnType((byte) GiftComeManager.ACT_NOT_ACTIVE);
            send(clientGiftComePacket);
            return;
        }
        if (roleGiftCome520 == null) {
            onCreation(null, null);
        }
        signCalRedPoint(MConst.GiftCome520, RedPointConst.GIFT_COME520);

    }

    @Override
    public void onSyncData() throws Throwable {
        if (getCurShowActivityId() == -1) {
            return;
        }
        /**
         * 避免角色直接与npc对话，服务端主动下发状态
         */
        sendUIResource();
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_GiftCome520);
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            if (show) {
                return curActivityId;
            } else {
                return -1;
            }

        }
        return curActivityId;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (getCurShowActivityId() == -1) {
            return;
        }
        onInit(false);
        sendUIResource();
    }


    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.GIFT_COME520)) {
            int state = checkActJoinState();
            if (state == 1) {
                redPointMap.put(RedPointConst.GIFT_COME520, "");
            } else {
                redPointMap.put(RedPointConst.GIFT_COME520, null);
            }
        }
    }

    /**
     * 发送活动界面的资源
     */
    public void sendUIResource() {
        ClientGiftComePacket clientGiftComePacket = new ClientGiftComePacket(ClientGiftComePacket.SEND_UI_RESOURCE);
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(getCurShowActivityId());
        if (operateActVo != null) {
            String ruledescKey = operateActVo.getRuledesc();
            String ruledesc = DataManager.getGametext(ruledescKey);
            clientGiftComePacket.setRuleDesc(ruledesc);
            String timedescKey = operateActVo.getTimedesc();
            String timedesc = DataManager.getGametext(timedescKey);
            clientGiftComePacket.setTimeDesc(String.format(timedesc, GiftComeManager.beginDate, GiftComeManager.endDate));
            DropModule dropModule = module(MConst.Drop);
            Map<Integer, Integer> showItemByDropGroup = dropModule.getShowItemByDropGroup(GiftComeManager.npcLoveGiftRewardDropGroupId);
            String showItem = StringUtil.makeString(showItemByDropGroup, '+', ',');
            clientGiftComePacket.setDropId(-1);
            int checkResult = checkActJoinState();
            switch (checkResult) {
                case GiftComeManager.ACT_NOT_BEGIN: {
                    clientGiftComePacket.setBtnType((byte) GiftComeManager.ACT_NOT_BEGIN);
                    clientGiftComePacket.setTips(DataManager.getGametext("npclovegift_gettips_early"));
                    clientGiftComePacket.setBtnText(DataManager.getGametext("npclovegift_label_notbegin"));
                    clientGiftComePacket.setRewardShowItem(showItem);
                }
                break;
                case GiftComeManager.ACT_TAKE_REWARD: {
                    clientGiftComePacket.setBtnType((byte) GiftComeManager.ACT_TAKE_REWARD);
                    clientGiftComePacket.setTips(DataManager.getGametext("npclovegift_gettips_get"));
                    clientGiftComePacket.setBtnText(DataManager.getGametext("commonbtntext_goto"));
                    clientGiftComePacket.setRewardShowItem(showItem);
                }
                break;
                case GiftComeManager.ACT_TAKED_REWARD: {
                    clientGiftComePacket.setBtnType((byte) GiftComeManager.ACT_TAKED_REWARD);
                    clientGiftComePacket.setTips(DataManager.getGametext("npclovegift_gettips_nextday"));
                    clientGiftComePacket.setBtnText(DataManager.getGametext("common_btn_getted"));
                    clientGiftComePacket.setRewardShowItem(showItem);
                }
                break;
                case GiftComeManager.ACT_END: {
                    clientGiftComePacket.setBtnType((byte) GiftComeManager.ACT_END);
                    clientGiftComePacket.setTips("");
                    clientGiftComePacket.setBtnText(DataManager.getGametext("npclovegift_label_finished"));
                    clientGiftComePacket.setRewardShowItem("");
                }
                break;
                case GiftComeManager.ACT_NOT_ACTIVE: {
                    clientGiftComePacket.setBtnType((byte) GiftComeManager.ACT_NOT_ACTIVE);
                }
                break;
            }
        } else {
            clientGiftComePacket.setBtnType((byte) GiftComeManager.ACT_NOT_ACTIVE);
        }
        send(clientGiftComePacket);
    }

    /**
     * 为奖励根据品质排序
     *
     * @param reward
     * @return
     */
    private Map<Integer, Integer> sortByQuality(Map<Integer, Integer> reward) {
        TreeSet<SortedItem> sortedItemTreeSet = new TreeSet<>();
        for (Map.Entry<Integer, Integer> entry : reward.entrySet()) {
            sortedItemTreeSet.add(new SortedItem(entry.getKey(), entry.getValue()));
        }
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (SortedItem sortedItem : sortedItemTreeSet) {
            map.put(sortedItem.getItemId(), sortedItem.getNum());
        }
        return map;
    }

    /**
     * 领取奖励
     */
    public void takeReward() {
        int state = checkActJoinState();
        switch (state) {
            case GiftComeManager.ACT_NOT_BEGIN: {
                warn("活动未开启");
            }
            break;
            case GiftComeManager.ACT_TAKE_REWARD: {
                if (roleGiftCome520.getLastRewardDateTime() == null) {
                    roleGiftCome520.setRewardtime("" + DateUtil.getRelativeDifferDays(GiftComeManager.benginDateTime, new Date()));
                } else {
                    roleGiftCome520.setRewardtime(roleGiftCome520.getRewardtime() + "+" + DateUtil.getRelativeDifferDays(GiftComeManager.benginDateTime, new Date()));
                }
                context().update(roleGiftCome520);
                DropModule dropModule = module(MConst.Drop);
                ToolModule toolModule = module(MConst.Tool);
                Map<Integer, Integer> reward = dropModule.executeDrop(GiftComeManager.npcLoveGiftRewardDropGroupId, 1, true);
                toolModule.addAndSend(reward, EventType.AWARD.getCode());
                ClientAward clientAward = new ClientAward(sortByQuality(reward));
                clientAward.setType((byte) 1);
                toolModule.sendPacket(clientAward);
                sendUIResource();
                signCalRedPoint(MConst.GiftCome520, RedPointConst.GIFT_COME520);
            }
            break;
            case GiftComeManager.ACT_TAKED_REWARD: {
                warn("奖励已领取");
            }
            break;
            case GiftComeManager.ACT_END: {
                warn("活动已结束");
            }
            break;
        }
    }

    /**
     * 检测当前活动对于玩家的进行状态
     * -1:活动未开始
     * 0:奖励已领取
     * 1:奖励未领取
     * 2:活动已结束
     * 3:不在活动时间内
     *
     * @return
     */
    private int checkActJoinState() {
        Date now = new Date();
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(getCurShowActivityId());
        if (operateActVo == null) {
            return 3;
        }
        if (now.before(GiftComeManager.benginDateTime)) {
            return -1;
        } else if (DateUtil.isBetween(now, GiftComeManager.benginDateTime, GiftComeManager.endDateTime)) {
            if (roleGiftCome520.getLastRewardDateTime() != null && DateUtil.getRelativeDifferDays(roleGiftCome520.getLastRewardDateTime(), now) < 1) {
                if (DateUtil.getRelativeDifferDays(GiftComeManager.endDateTime, new Date()) == 0) {
                    return 2;
                }
                return 0;
            }
            return 1;
        }
        return 2;
    }

    class SortedItem implements Comparable<SortedItem> {
        private Integer itemId;
        private Integer num;

        public SortedItem(Integer itemId, Integer num) {
            this.itemId = itemId;
            this.num = num;
        }

        @Override
        public int compareTo(SortedItem o) {
            ItemVo myItemVo = ToolManager.getItemVo(itemId);
            ItemVo otherItemVo = ToolManager.getItemVo(o.itemId);
            if (myItemVo.getColor() != otherItemVo.getColor()) {
                return otherItemVo.getColor() - myItemVo.getColor();
            }
            if (!myItemVo.getRank().equals(otherItemVo.getRank())) {
                return otherItemVo.getRank() - myItemVo.getRank();
            }
            return itemId;
        }

        public Integer getItemId() {
            return itemId;
        }

        public void setItemId(Integer itemId) {
            this.itemId = itemId;
        }

        public Integer getNum() {
            return num;
        }

        public void setNum(Integer num) {
            this.num = num;
        }
    }

}
