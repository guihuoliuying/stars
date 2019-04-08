package com.stars.modules.chargeback;

import com.stars.AccountRow;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.chargeback.packet.ClientChargeBackPacket;
import com.stars.modules.chargeback.prodata.BackRule;
import com.stars.modules.chargeback.userdata.AccountChargeBack;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.AccountRowAware;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.tool.ToolManager;
import com.stars.services.ServiceHelper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/3/20.
 */
public class ChargeBackModule extends AbstractModule implements OpActivityModule, AccountRowAware {
    AccountRow accountRow;

    public ChargeBackModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    public ChargeBackModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        super("公测返利", id, self, eventDispatcher, map);
    }

    @Override
    public void onDataReq() throws Throwable {


    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        AccountChargeBack accountChargeBack = ChargeBackManager.accountChargeBackMap.get(account);
        if (accountChargeBack != null) {
            /**
             * 全区共享，操作前必须重新查询
             */
            String sql = "select * from accountchargeback where account='" + account + "'";
            accountChargeBack = DBUtil.queryBean(DBUtil.DB_COMMON, AccountChargeBack.class, sql);
            if (accountChargeBack != null) {
                handleChargeBack(accountChargeBack);
                DBUtil.execSql(DBUtil.DB_COMMON, "delete  from `accountchargeback` where accountchargeback.account='" + account + "'");
            }
            /**
             * 领取完毕删除以防多角色发放
             */
            ChargeBackManager.accountChargeBackMap.remove(account);
        }
    }


    /**
     * 充值返还活动
     */
    public void handleChargeBack(AccountChargeBack accountChargeBack) {


        Map<String, BackRule> backRuleMap = ChargeBackManager.backRuleMap;
        Map<Integer, Integer> chargeBackReward = new HashMap<>();
        Map<Integer, Integer> chargePackageBack = new HashMap<>();
        /**
         * 充值资源返还
         */
        if (accountChargeBack.getVipexp() != 0) {
            BackRule vipexpRule = backRuleMap.get("vipexp");
            String param = vipexpRule.getParam();
            float scale = (float) (Integer.parseInt(param) / 100.0);//vip经验的返还比例
            Integer exp = Math.round(accountChargeBack.getVipexp() * scale);
            chargeBackReward.put(ToolManager.VIP_EXP, exp);
        }
        if (accountChargeBack.getYb() != 0) {
            BackRule moneyRule = backRuleMap.get("money");
            String param = moneyRule.getParam();
            float scale = (float) (Integer.parseInt(param) / 100.0);//元宝的返还比例
            Integer yb = Math.round(accountChargeBack.getYb() * scale);
            chargeBackReward.put(ToolManager.BANDGOLD, yb);
        }
        if (accountChargeBack.getMonthcard() != 0) {
            BackRule cardRule = backRuleMap.get("card");
            String param = cardRule.getParam();
            int monthCard = Integer.parseInt(param);//月卡
            chargeBackReward.put(ChargeBackManager.MONTHCARD_ITEMID, monthCard);
        }
        if (chargeBackReward.size() > 0) {
            ServiceHelper.emailService().sendToSingle(id(), ChargeBackManager.CHARGE_BACK, id(), "系统", chargeBackReward);
        }
        /**
         *充值大礼包返还
         */
        if (accountChargeBack.getYb() > 0) {
            for (Map.Entry<Integer, Integer> entry : ChargeBackManager.moneyReward.entrySet()) {
                if (accountChargeBack.getYb() >= entry.getKey()) {
                    Integer itemId = entry.getValue();
                    chargePackageBack.put(itemId, 1);
                }
            }
        }
        if (chargePackageBack.size() > 0) {
            ServiceHelper.emailService().sendToSingle(id(), ChargeBackManager.CHARGE_PACKAGE_BACK, id(), "系统", chargePackageBack, accountChargeBack.getYb()/10 + "");
        }
    }

    @Override
    public int getCurShowActivityId() {
        return OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_ChargeBack);
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }


    /**
     * 客户端请求充值返利的规则数据
     */
    public void reqRule() {
        /**
         * 充值描述
         */
        String serverrebatedesc_charged = DataManager.getGametext("serverrebatedesc_charged");
        /**
         * 规则描述
         */
        String serverrebatedesc_desc = DataManager.getGametext("serverrebatedesc_desc");
        /**
         * 按钮文本
         */
        String serverrebatedesc_btn_charge = DataManager.getGametext("serverrebatedesc_btn_charge");
        String openWindow = "";
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(getCurShowActivityId());
        openWindow = operateActVo.getOpenwindow();
        ClientChargeBackPacket clientChargeBackPacket = new ClientChargeBackPacket(ClientChargeBackPacket.reqRule, ChargeBackManager.moneyReward,
                serverrebatedesc_desc, serverrebatedesc_charged, serverrebatedesc_btn_charge, openWindow);
        send(clientChargeBackPacket);
    }

    /**
     * 客户端请求获取当前的充值元宝数量
     */
    public void reqYb() {
        int chargeSum = 0;
        LoginModule loginModule = module(MConst.Login);
        try {
            AccountRow accountRow = loginModule.getAccountRow();
            chargeSum = accountRow.getChargeSum();
            int yb = chargeSum * 10;
            ClientChargeBackPacket clientChargeBackPacket = new ClientChargeBackPacket(ClientChargeBackPacket.reqYb, yb);
            send(clientChargeBackPacket);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setAccountRow(AccountRow accountRow) {
        this.accountRow = accountRow;
    }
}
