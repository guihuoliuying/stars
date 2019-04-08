package com.stars.modules.demologin.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.userdata.AccountTransfer;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;

import java.util.Map;

/**
 * Created by huwenjun on 2017/10/19.
 */
public class AccountTransferGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        LoginModule loginModule = (LoginModule) moduleMap.get(MConst.Login);
        try {

            String arg = args[0];
            String[] accounts = arg.split("=");
            switch (accounts[0]) {
                case "back": {
                    AccountTransfer accountTransfer=new AccountTransfer(accounts[1], accounts[2],System.currentTimeMillis());
                    ServiceHelper.accountTransferService().transferBack(accountTransfer);

                }
                break;
                case "transfer": {
                    ServiceHelper.accountTransferService().transfer(accounts[1], accounts[2], "账号转移",null);
                }
                break;
            }
            loginModule.warn("操作成功");
        } catch (Exception e) {
            loginModule.warn("操作失败");

        }

    }
}
