package com.stars.modules.welfareaccount;

import com.google.gson.Gson;
import com.stars.AccountRow;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.demologin.AccountRowAware;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.prodata.ChargeVo;
import com.stars.modules.welfareaccount.packet.ClientWelfareAccountPacket;
import com.stars.services.ServiceHelper;
import com.stars.services.pay.PayExtent;
import com.stars.services.pay.PayOrderInfo;
import com.stars.startup.MainStartup;

import java.util.Map;

/**
 * Created by huwenjun on 2017/4/11.
 */
public class WelfareAccountModule extends AbstractModule implements AccountRowAware {
    private AccountRow accountRow;

    public WelfareAccountModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("福利号", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        super.onCreation(name, account);
    }

    @Override
    public void onDataReq() throws Throwable {
        super.onDataReq();
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        super.onInit(isCreation);
        queryWelfareAccount();
        queryAccountMoneyInner();
    }

    /**
     * 虚拟充值接口
     *
     * @param chargeId
     * @param payPoint
     */
    public void charge(int chargeId, byte payPoint) {
        if (!WelfareAccountManager.isWelfareAccount(accountRow.getName())) {
            return;
        }
        ChargeVo chargeVo = VipManager.getChargeVo(MainStartup.serverChannel, chargeId);
        ToolModule toolModule = module(MConst.Tool);
        /**
         * 充值先扣除虚拟币
         */
        boolean deleteItem = toolModule.gmDeleteItem(WelfareAccountManager.VIRTUAL_MONEY_ITEM_ID, chargeVo.getReqRmb());
        if (!deleteItem) {
            return;
        }
        PayOrderInfo pInfo = new PayOrderInfo();
        pInfo.setCpTradeNo(String.valueOf(System.currentTimeMillis()));
        pInfo.setChannelId(-1);
        pInfo.setMoney(chargeVo.getReqRmb()*100);
        pInfo.setActionType(1);//虚拟币充值
        PayExtent PayEx = new PayExtent();
        PayEx.setId(chargeId);
        PayEx.setPoint(payPoint);
        Gson gson = new Gson();
        String str = gson.toJson(PayEx);
        pInfo.setPrivateField(str);
        pInfo.setRoleId(String.valueOf(id()));
        pInfo.setUserId(accountRow.getName());
        ServiceHelper.payService().recaivePayOrder(-1, -1, pInfo, false);

    }


    /**
     * 下发当前剩余虚拟币数量(内部使用)
     */
    public void queryAccountMoneyInner() {
        ToolModule toolModule = module(MConst.Tool);
        long virtualMoney = toolModule.getCountByItemId(WelfareAccountManager.VIRTUAL_MONEY_ITEM_ID);
        ClientWelfareAccountPacket clientVirtualMoneryPacket = new ClientWelfareAccountPacket(ClientWelfareAccountPacket.QUERY_VIRTUAL_MONEY);
        if (WelfareAccountManager.isWelfareAccount(accountRow.getName())) {
            clientVirtualMoneryPacket.setVirtualMoney((int) virtualMoney);
        } else {
            clientVirtualMoneryPacket.setVirtualMoney(0);
        }
        send(clientVirtualMoneryPacket);
    }

    /**
     * 下发当前剩余虚拟币数量(外部使用，纯提示)
     */
    public void queryAccountMoneyOutter() {
        ToolModule toolModule = module(MConst.Tool);
        if (WelfareAccountManager.isWelfareAccount(accountRow.getName())) {
            long virtualMoney = toolModule.getCountByItemId(WelfareAccountManager.VIRTUAL_MONEY_ITEM_ID);
            warn("coin %s", virtualMoney + "");
        } else {
            warn("操作过于频繁");

        }
    }


    /**
     * 查看账户是否是福利号
     */
    public void queryWelfareAccount() {
        ClientWelfareAccountPacket clientVirtualMoneryPacket = new ClientWelfareAccountPacket(ClientWelfareAccountPacket.IS_WELFARE_ACCOUNT);
        clientVirtualMoneryPacket.setIsWelfareAccount(WelfareAccountManager.isWelfareAccount(accountRow.getName()) ? 1 : 0);
        send(clientVirtualMoneryPacket);
    }

    @Override
    public void setAccountRow(AccountRow accountRow) {
        this.accountRow = accountRow;
    }

}
