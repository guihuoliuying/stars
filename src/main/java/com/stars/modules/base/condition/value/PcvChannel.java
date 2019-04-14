package com.stars.modules.base.condition.value;

import com.stars.AccountRow;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.userdata.LoginInfo;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcvChannel extends BaseExprValue {

    @Override
    public Object eval(Map<String, Module> moduleMap) {
        try {
            LoginModule loginModule = module(moduleMap, MConst.Login);
            String account = loginModule.getAccount();
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account, null);
            LoginInfo loginInfo = accountRow.getLoginInfo();
            if (loginInfo != null && loginInfo.getChannel() != null && !"".equals(loginInfo.getChannel().trim())) {
                return Long.parseLong(loginInfo.getChannel().split("@")[0]);
            } else {
                return -1;
            }
        } catch (Throwable cause) {
            throw new RuntimeException();
        }
    }

    @Override
    public String toString() {
        return "渠道";
    }
}
