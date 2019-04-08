package com.stars.modules.discountgift;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.discountgift.packet.ClientDiscountGift;
import com.stars.modules.discountgift.prodata.DiscountGiftVo;
import com.stars.modules.discountgift.userdata.RoleDiscountGiftPo;
import com.stars.modules.drop.DropModule;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.push.PushManager;
import com.stars.modules.push.PushModule;
import com.stars.modules.push.prodata.PushVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by chenxie on 2017/5/26.
 */
public class DiscountGiftModule extends AbstractModule {

    private RoleDiscountGiftPo roleDiscountGiftPo;

    public DiscountGiftModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("优惠豪礼", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from rolediscountgift where roleid = " + id();
        roleDiscountGiftPo = DBUtil.queryBean(DBUtil.DB_USER, RoleDiscountGiftPo.class, sql);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
//        checkValidity();
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (!isLogin) {
            checkValidity();
        }
    }

    @Override
    public void onWeeklyReset(boolean isLogin) throws Throwable {
        if (!isLogin) {
            checkValidity();
        }
    }

    /**
     * 判断数据是否还有效
     *
     * @return
     */
    public void checkValidity() {
        // 清除过期数据
        if (roleDiscountGiftPo != null) {
            PushModule pushModule = module(MConst.Push);
            Set<Integer> pushIdSet = pushModule.getActivePushIdByActivityId(3);
            if (!pushIdSet.contains(roleDiscountGiftPo.getGiftGroupId())) { // 处理过期数据
                context().delete(roleDiscountGiftPo);
                roleDiscountGiftPo = null;
            }
        }
        // 重新检查数据
        if (roleDiscountGiftPo == null) {
            checkActivedPush();
        }
        // 标记计算红点
        signCalRedPoint(MConst.DiscountGift, RedPointConst.DICOUNT_GIFT);
        LogUtil.info("discountGift|checkValidity|roleId:{}|giftId:{}",
                id(), roleDiscountGiftPo != null ? roleDiscountGiftPo.getGiftId() : 0);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (roleDiscountGiftPo == null) {
            redPointMap.put(RedPointConst.DICOUNT_GIFT, null);
//            LogUtil.info("discountGift|redPoints|false");
        } else {
            redPointMap.put(RedPointConst.DICOUNT_GIFT, "true");
//            LogUtil.info("discountGift|redPoints|true");
        }
    }

    /**
     * 根据规则给玩家发放奖励
     *
     * @param chargeMoney 玩家充值金额（单位：元）
     */
    public void handleChargeEvent(final int chargeMoney) {
        if (isOpen()) {
            if (roleDiscountGiftPo == null) return;
            int totalCharge = roleDiscountGiftPo.getTotalCharge() + chargeMoney;
            if (totalCharge >= roleDiscountGiftPo.getCharge()) {
                //玩家充值满足对应优惠条目后，获得对应的奖励，并且自动选择下一个优惠条目
                sendAward();
                chooseNextGift();
            } else {
                //玩家充值不满足对应优惠条目则累计充值金额
                roleDiscountGiftPo.setTotalCharge(totalCharge);
                context().update(roleDiscountGiftPo);
            }
        }
    }

    /**
     * 发放奖励（通过邮件发放）
     */
    private void sendAward() {
        DropModule dropModule = module(MConst.Drop);
        Map<Integer, Integer> map = dropModule.executeDrop(roleDiscountGiftPo.getDropId(), 1, true);
        ServiceHelper.emailService().sendToSingle(id(), 27001, 0l, "系统", map,
                String.valueOf(roleDiscountGiftPo.getCharge()));
    }

    /**
     * 选择下一个商品礼包
     */
    private void chooseNextGift() {
        //找到玩家当前的商品礼品在列表的下标
        DiscountGiftVo searchVo = new DiscountGiftVo();
        searchVo.setGiftGroupId(roleDiscountGiftPo.getGiftGroupId());
        searchVo.setGiftId(roleDiscountGiftPo.getGiftId());
        int index = DiscountGiftManager.dicGiftVoList.indexOf(searchVo);
        if (index == -1) {
            throw new RuntimeException("数据异常");
        }
        //选择下一个商品礼品
        DiscountGiftVo discountGiftVo;
        if (index + 1 < DiscountGiftManager.dicGiftVoList.size()) {
            discountGiftVo = DiscountGiftManager.dicGiftVoList.get(index + 1);
            if (discountGiftVo.getGiftGroupId() == roleDiscountGiftPo.getGiftGroupId()) {
                roleDiscountGiftPo.setTotalCharge(0);
                roleDiscountGiftPo.copyToUserSpace(discountGiftVo);
                context().update(roleDiscountGiftPo);
                return;
            }
        }
        PushModule pushModule = module(MConst.Push);
        pushModule.inactivePush(roleDiscountGiftPo.getGiftGroupId());
        context().delete(roleDiscountGiftPo);
        roleDiscountGiftPo = null;
        //再次进行初始化，处理之前已经推送过的优惠条目
        if (!initRoleDiscountGiftPo()) {
            signCalRedPoint(MConst.DiscountGift, RedPointConst.DICOUNT_GIFT);
        }
    }

    /**
     * 初始化用户数据
     */
    private boolean initRoleDiscountGiftPo() {
        PushModule pushModule = module(MConst.Push);
        Set<Integer> pushIdSet = pushModule.getActivePushIdByActivityId(3);
        if (pushIdSet != null) {
            Integer[] pushIdArray = new Integer[pushIdSet.size()];
            pushIdSet.toArray(pushIdArray);
            Arrays.sort(pushIdArray);
            for (Integer pushId : pushIdArray) {
                PushVo pushVo = PushManager.getPushVo(pushId);
                // 判断是否之前处理过了
                boolean isUse = pushModule.isActived(pushVo.getPushId());
                if (!isUse) {
                    List<DiscountGiftVo> list = DiscountGiftManager.dicGiftVoMap.get(pushId);
                    if (pushVo != null && list != null && list.size() > 0) {
                        // 为玩家自动选择一个优惠条目
                        DiscountGiftVo discountGiftVo = list.get(0);
                        roleDiscountGiftPo = new RoleDiscountGiftPo();
                        roleDiscountGiftPo.setRoleId(id());
                        roleDiscountGiftPo.setTotalCharge(0);
                        roleDiscountGiftPo.setDate(pushVo.getDate());
                        roleDiscountGiftPo.copyToUserSpace(discountGiftVo);
                        context().insert(roleDiscountGiftPo);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断活动是否开放
     *
     * @return
     */
    private boolean isOpen() {
        boolean state = ((ForeShowModule) module(MConst.ForeShow)).isOpen(ForeShowConst.DISCOUNT_GIFT);
        return state;
    }

    public void view() {
        if (!isOpen()) {
            warn("系统没开放");
            return;
        }
        if (roleDiscountGiftPo == null) {
            warn("discountgift_tips_nogoods");
            return;
        }
        ClientDiscountGift clientDiscountGift = new ClientDiscountGift();
        clientDiscountGift.setGiftGroupId(roleDiscountGiftPo.getGiftGroupId());
        clientDiscountGift.setCharge(roleDiscountGiftPo.getCharge());
        clientDiscountGift.setDropId(roleDiscountGiftPo.getDropId());
        clientDiscountGift.setTotalCharge(roleDiscountGiftPo.getTotalCharge());
        clientDiscountGift.setImg(roleDiscountGiftPo.getImg());
        clientDiscountGift.setDate(PushManager.getPushVo(roleDiscountGiftPo.getGiftGroupId()).getDate());
        clientDiscountGift.setOriginalPrice(DiscountGiftManager.getOriginalPrice(roleDiscountGiftPo.getGiftGroupId()));
        send(clientDiscountGift);
    }

    /**
     * 处理精准推送激活事件
     */
    public void checkActivedPush() {
        if (roleDiscountGiftPo == null) {
            PushModule pushModule = module(MConst.Push);
            for (Integer pushId : pushModule.getActivePushIdByActivityId(3)) {
                if (DiscountGiftManager.dicGiftVoMap.containsKey(pushId)) {
                    DiscountGiftVo discountGiftVo = DiscountGiftManager.dicGiftVoMap.get(pushId).get(0);
                    PushVo pushVo = PushManager.getPushVo(pushId);
                    if (discountGiftVo != null && pushVo != null) {
                        roleDiscountGiftPo = new RoleDiscountGiftPo();
                        roleDiscountGiftPo.setRoleId(id());
                        roleDiscountGiftPo.setTotalCharge(0);
                        roleDiscountGiftPo.setDate(pushVo.getDate());
                        roleDiscountGiftPo.copyToUserSpace(discountGiftVo);
                        context().insert(roleDiscountGiftPo);
                        signCalRedPoint(MConst.DiscountGift, RedPointConst.DICOUNT_GIFT);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 处理精准推送关闭事件
     *
     * @param pushId
     */
    public void handlePushInActivedEvent(int pushId) {
        if (roleDiscountGiftPo != null) {
            PushVo pushVo = PushManager.getPushVo(pushId);
            if (pushVo.getActivityId() == 3) {
                if (roleDiscountGiftPo.getGiftGroupId() == pushVo.getPushId()) {
                    context().delete(roleDiscountGiftPo);
                    this.roleDiscountGiftPo = null;
                    signCalRedPoint(MConst.DiscountGift, RedPointConst.DICOUNT_GIFT);
                }
            }
        }
    }

    public int getGiftId() {
        return roleDiscountGiftPo != null ? roleDiscountGiftPo.getGiftId() : 0;
    }

    public int getGiftGroupId() {
        return roleDiscountGiftPo != null ? roleDiscountGiftPo.getGiftGroupId() : 0;
    }

}
