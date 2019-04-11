package com.stars.core.gmpacket;

import com.stars.AccountRow;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.LogUtil;

import java.util.HashMap;

/**
 * Created by huwenjun on 2017/10/18.
 */
public class AccountTransferGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson("操作成功"));
        String currentAccount = (String) args.get("account");
        String newAccount = (String) args.get("newAccount");
        String currentRoleId = (String) args.get("roleId");
        String newRoleId = (String) args.get("newRoleId");
        String reason = (String) args.get("content");
        try {
            AccountRow currentAccountRow = LoginModuleHelper.getOrLoadAccount(currentAccount, null);
            AccountRow newAccountRow = LoginModuleHelper.getOrLoadAccount(newAccount, null);
            boolean currentCheck = false;//检测当前账号与角色id是否匹配
            boolean newCheck = false;//检测新账号与角色id是否匹配
            boolean isValid = false;//检测新账户是否是有效账号
            if (currentAccount == null) {
                response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson("旧账户不存在"));
                return null;
            }
            if (newAccountRow == null) {
                response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson("新账户不存在"));
                return null;
            }

        } catch (Exception e) {
            LogUtil.error("账号转移获取账号信息失败:" + newAccount, e);
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson("操作失败"));
        } finally {
            return response.toString();
        }


    }
}
