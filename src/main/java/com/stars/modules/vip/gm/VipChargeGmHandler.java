package com.stars.modules.vip.gm;

import com.google.gson.Gson;
import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.prodata.ChargeVo;
import com.stars.services.ServiceHelper;
import com.stars.services.pay.PayExtent;
import com.stars.services.pay.PayOrderInfo;
import com.stars.startup.MainStartup;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/12/7.
 */
public class VipChargeGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            if (args.length == 1) {
                int chargeId = Integer.parseInt(args[0]);
                LoginModule lModule = (LoginModule) moduleMap.get(MConst.Login);
                doPayment(roleId, lModule.getAccount(), moduleMap, chargeId);
            } else if (args.length == 3) {
                String account = args[0];
                long otherRoleId = Long.parseLong(args[1]);
                int chargeId = Integer.parseInt(args[2]);
                doPayment(otherRoleId, account, moduleMap, chargeId);
            }
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败,vipcharge " + args[0]));
            e.printStackTrace();
        }
    }

    private void doPayment(long roleId, String account, Map<String, Module> moduleMap, int chargeId) {
        PayOrderInfo pInfo = new PayOrderInfo();
        pInfo.setCpTradeNo(String.valueOf(System.currentTimeMillis()));
        pInfo.setChannelId(-1);
        ChargeVo chargeVo = VipManager.getChargeVo(MainStartup.serverChannel, chargeId);
        pInfo.setMoney(chargeVo.getReqRmb() * 100);
        PayExtent PayEx = new PayExtent();
        PayEx.setId(chargeId);
        PayEx.setPoint((byte) 0);
        Gson gson = new Gson();
        String str = gson.toJson(PayEx);
        pInfo.setPrivateField(str);
        pInfo.setRoleId(String.valueOf(roleId));
        pInfo.setUserId(account);
        ServiceHelper.payService().recaivePayOrder(-1, -1, pInfo, false);
    }
}
