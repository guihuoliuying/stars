package com.stars.core.gmpacket;

import com.stars.AccountRow;
import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.accounttransfer.AccountTransferServiceActor;
import com.stars.services.accounttransfer.po.AccountTransferCount;
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
            AccountTransferCount accountTransferCount = DBUtil.queryBean(DBUtil.DB_COMMON, AccountTransferCount.class, String.format("select * from accounttransfercount where newaccount='%s' or oldaccount='%s';", newAccount, currentAccount));
            if (accountTransferCount != null
                    && !(accountTransferCount.getOldAccount().equals(currentAccount)
                    && accountTransferCount.getNewAccount().equals(newAccount))) {
                response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson("专区内单个账号只能和一个账号构成转移关系，请确认或更换账号"));
                return null;
            }
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
            if (AccountTransferServiceActor.accountTransferCache.get(newAccount).isInvalid()) {
                isValid = true;
                for (AccountRole accountRole : currentAccountRow.getRelativeRoleList()) {
                    if (currentRoleId.equals(accountRole.getRoleId())) {
                        currentCheck = true;
                        break;
                    }
                }
                for (AccountRole accountRole : newAccountRow.getRelativeRoleList()) {
                    if (newRoleId.equals(accountRole.getRoleId())) {
                        newCheck = true;
                        break;
                    }
                }
            }

            if (isValid && currentCheck && newCheck) {
                ServiceHelper.accountTransferService().transfer(currentAccount, newAccount, reason, accountTransferCount);
                response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson("操作成功"));
            } else {
                String msg = "";
                if (!isValid) {
                    msg = "新账户已经存在转移记录，请换账号";
                } else if (!currentCheck) {
                    msg = "旧账号与角色id不匹配，请再次确认";
                } else if (!newCheck) {
                    msg = "新账号与角色id不匹配，请再次确认";

                }
                response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(msg));
            }
        } catch (Exception e) {
            LogUtil.error("账号转移获取账号信息失败:" + newAccount, e);
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson("操作失败"));
        } finally {
            return response.toString();
        }


    }
}
