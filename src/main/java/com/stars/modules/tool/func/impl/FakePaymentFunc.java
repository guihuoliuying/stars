package com.stars.modules.tool.func.impl;

import com.google.gson.Gson;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.role.RoleModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.prodata.ChargeVo;
import com.stars.services.ServiceHelper;
import com.stars.services.pay.PayExtent;
import com.stars.services.pay.PayOrderInfo;
import com.stars.startup.MainStartup;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/23.
 */
public class FakePaymentFunc extends ToolFunc {
    private int chargeId;

    public FakePaymentFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        if (StringUtil.isEmpty(function)) return;
        String[] args = function.split("\\|");

        if (args.length == 2) {
            chargeId = Integer.parseInt(args[1]);
        }
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
        if (count <= 0) {
            return new ToolFuncResult(false, new ClientText("道具数量为零"));
        }

        ToolFuncResult tr = super.useCondition(moduleMap, args);
        if (tr == null) {
            tr = new ToolFuncResult(true, null);
        }

        return tr;
    }

    @Override
    public Map<Integer, Integer> use(Map<String, Module> moduleMap, int count, Object... args) {

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
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        pInfo.setPrivateField(str);
        pInfo.setRoleId(String.valueOf(roleModule.id()));
        LoginModule lModule = (LoginModule) moduleMap.get(MConst.Login);
        pInfo.setUserId(lModule.getAccount());
        for(int i = 1; i <= count; i++) {
            ServiceHelper.payService().recaivePayOrder(-1, -1, pInfo, true);
        }
        return null;
    }

}
