package com.stars.core.gmpacket;

import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.userdata.AccountTransfer;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by huwenjun on 2017/10/18.
 */
public class AccountTransferBackGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson("操作成功"));
        String oldAccount = (String) args.get("oldAccount");
        String newAccount = (String) args.get("newAccount");
        try {
            AccountTransfer accountTransfer = null;
            try {
                accountTransfer = DBUtil.queryBean(DBUtil.DB_USER, AccountTransfer.class, String.format("select * from accounttransfer where newaccount='%s' and oldaccount='%s';", newAccount, oldAccount));
                if (accountTransfer == null) {
                    response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson("无此转移记录"));
                    return null;
                }
            } catch (SQLException e) {
                LogUtil.error("查询账号转移信息失败:" + newAccount, e);
                response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson("查询账号转移信息失败"));
                return null;
            }

        } catch (Exception e) {
            LogUtil.error("账号转移撤回失败:" + newAccount, e);
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson("操作失败"));

        } finally {
            return response.toString();
        }

    }
}
