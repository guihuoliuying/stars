package com.stars.modules.chargegift;

import com.stars.AccountRow;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.chargegift.packet.ClientChargeGift;
import com.stars.modules.chargegift.userdata.RoleChargeGiftPo;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.userdata.LoginInfo;
import com.stars.modules.drop.DropModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.services.ServiceHelper;
import com.stars.util.I18n;

import java.util.Calendar;
import java.util.Map;

/**
 * Created by chenxie on 2017/5/18.
 */
public class ChargeGiftModule extends AbstractModule implements OpActivityModule {

    /**
     * 生命周期从用户请求开始
     */
    private RoleChargeGiftPo roleChargeGiftPo;
    private AccountRow accountRow;

    public ChargeGiftModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("充值送礼", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from chargegift where roleid = " + id();
        roleChargeGiftPo = DBUtil.queryBean(DBUtil.DB_USER, RoleChargeGiftPo.class, sql);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (roleChargeGiftPo == null) {
            roleChargeGiftPo = new RoleChargeGiftPo();
            roleChargeGiftPo.setRoleId(id());
            roleChargeGiftPo.setGiftNum(0);
            roleChargeGiftPo.setTotalCharge(0);
            context().insert(roleChargeGiftPo);
        }
        LoginModule loginModule = module(MConst.Login);
        accountRow = loginModule.getAccountRow();
    }

    /**
     * 根据充值送礼的规则给玩家发放奖励
     *
     * @param chargeMoney 玩家充值金额（单位：元）
     */
    public void handleChargeEvent(final int chargeMoney) {
        if (!isOpenActivity()) {
            return;
        }

        //重新设置玩家当日总充值额度
        roleChargeGiftPo.setTotalCharge(roleChargeGiftPo.getTotalCharge() + chargeMoney);
        int giftNum = computeGiftNum(chargeMoney);
        if (giftNum > 0) {
            //重新设置玩家当日累计获得的礼包数量
            roleChargeGiftPo.setGiftNum(roleChargeGiftPo.getGiftNum() + giftNum);
            sendAward(giftNum, chargeMoney);
        }
        context().update(roleChargeGiftPo);
    }

    /**
     * 发放奖励（通过邮件发放）
     */
    private void sendAward(int giftNum, final int chargeMoney) {
        if (roleChargeGiftPo == null) return;
        DropModule dropModule = module(MConst.Drop);
        Map<Integer, Integer> map = dropModule.executeDrop(ChargeGiftManager.CHARGE_GIFT_DROP_ID, giftNum, true);
        ServiceHelper.emailService().sendToSingle(id(), 25004, 0l, "系统", map,
                String.valueOf(chargeMoney), String.valueOf(roleChargeGiftPo.getTotalCharge()), String.valueOf(roleChargeGiftPo.getGiftNum()));
    }

    /**
     * 计算玩家本次可以获得的礼包数量
     *
     * @param chargeMoney
     * @return
     */
    private int computeGiftNum(final int chargeMoney) {
        // 记配置的最小充值额度为x || 角色本次充值额度n
        // 包含本次充值在内的当日总充值额度为m || 每日可以领取的礼包上限为a
        // 玩家本次可以获得的礼包数量y：y=min⁡(a,int(m/x))-int((m-n)/x)
        int x = ChargeGiftManager.CHARGE_GIFT_MIN_CHARGE, n = chargeMoney, m = roleChargeGiftPo.getTotalCharge(), a = ChargeGiftManager.CHARGE_GIFT_MAX_GIFT;
        int giftNum = Math.min(a, m / x) - (m - n) / x;
        return giftNum;
    }

    /**
     * 下发充值送礼数据给客户端
     */
    public void viewMainUI() {
        if (!isOpenActivity()) {
            warn(I18n.get("marry.wedding.inactivity"));
            return;
        }
        ClientChargeGift clientChargeGift = new ClientChargeGift(ChargeGiftManager.CHARGE_GIFT_DROP_ID, ChargeGiftManager.CHARGE_GIFT_MAX_GIFT - roleChargeGiftPo.getGiftNum(), ChargeGiftManager.CHARGE_GIFT_MAX_GIFT);
        send(clientChargeGift);
    }


    /**
     * 活动是否开启
     *
     * @return
     */
    public boolean isOpenActivity() {
        return getCurShowActivityId() != -1;
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_ChargeGift);
        if (curActivityId != -1 && isAccordWithLimit(curActivityId)) {
            return curActivityId;
        }
        return -1;
    }

    /**
     * 是否符合限定条件
     *
     * @return
     */
    private boolean isAccordWithLimit(int curActivityId) {
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
        if (operateActVo != null) {
            //判断角色属性限制
            if (operateActivityModule.isShow(operateActVo.getRoleLimitMap())) {
                //判断渠道标识限制
                if ("0".equals(operateActVo.getChannel()) || operateActVo.getChannel() == null) {
                    return true;
                } else {
                    LoginInfo loginInfo = accountRow.getLoginInfo();
                    if (loginInfo == null) {
                        return false;
                    }
                    if (loginInfo.getChannel() == null) {
                        return false;
                    }
                    String[] clinetChannel = loginInfo.getChannel().split("@");
                    String[] passChannelArray = operateActVo.getChannel().split(",");
                    if (clinetChannel.length > 0) {
                        for (String pass : passChannelArray) {
                            if (clinetChannel[0].equals(pass)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (roleChargeGiftPo == null)
            return;
        roleChargeGiftPo.setTotalCharge(0);
        roleChargeGiftPo.setGiftNum(0);
        context().update(roleChargeGiftPo);
    }

}
