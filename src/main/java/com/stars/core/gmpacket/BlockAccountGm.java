package com.stars.core.gmpacket;

import com.stars.AccountRow;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.demologin.userdata.BlockAccount;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.startup.MainStartup;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liuyuheng on 2017/1/16.
 */
public class BlockAccountGm extends GmPacketHandler {
    private static Map<String, BlockAccount> blockAccountMap;

    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        int serverId = 0;
        String account = null;
        long expireTime = 0;
        String reason = "";
        if (args.containsKey("serverId")) {
            serverId = Integer.parseInt((String) args.get("serverId"));
        }
        if (args.containsKey("blockAccount")) {
            account = (String) args.get("blockAccount");
        }
        if (args.containsKey("expiresTime")) {// 运营传过来单位是秒,转成毫秒
            expireTime = Long.parseLong((String) args.get("expiresTime")) * 1000L;
        }
        if (args.containsKey("blockReason")) {
            reason = (String) args.get("blockReason");
        }
        BlockAccount blockAccount = blockAccountMap.get(account);
        if (blockAccount == null) {
            blockAccount = new BlockAccount(account, System.currentTimeMillis(), expireTime, reason);
            blockAccount.setInsertStatus();
            blockAccountMap.put(blockAccount.getAccount(), blockAccount);
        } else {
            blockAccount.setStartTime(System.currentTimeMillis());
            blockAccount.setExpireTime(expireTime);
            blockAccount.setReason(reason);
            blockAccount.setUpdateStatus();
        }
        try {
            DBUtil.execSql(DBUtil.DB_USER, blockAccount.getChangeSql());
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
            AccountRow accountRow = MainStartup.accountMap.get(account);
            if (accountRow != null) {
                List<Long> currentOnlineRoleIdList = new ArrayList<>();
                List<AccountRole> relativeRoleList = accountRow.getRelativeRoleList();
                //获得当前账号的在线角色列表
                for (AccountRole accountRole : relativeRoleList) {
                    Player player = PlayerSystem.get(Long.parseLong(accountRole.getRoleId()));
                    if (player != null) {
                        currentOnlineRoleIdList.add(player.id());
                    }
                }
                for (Long currentRoleId:currentOnlineRoleIdList){
                    LogUtil.info("封号GM踢人下线的RoleId:" + currentRoleId);
                    if (currentRoleId != 0) {
                        if (KickOffPlayerGm.kickOffPlayer(currentRoleId) == -1) {
                            LogUtil.error("封号GM调用踢角色下线失败" + currentRoleId);
                            throw new RuntimeException();
                        }
                    }
                }
            }
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
            ServerLogConst.console.error("增加封号账号错误", e);
        } finally {
            blockAccount.setSaveStatus();
            return response.toString();
        }
    }

    public static void loadBlockAccount() throws SQLException {
        blockAccountMap = new ConcurrentHashMap<>();
        String sql = "select * from `blockaccount`;";
        blockAccountMap = DBUtil.queryConcurrentMap(DBUtil.DB_USER, "account", BlockAccount.class, sql);
    }

    public static boolean isAccountBlock(String account) {
        if (!blockAccountMap.containsKey(account))
            return false;
        return System.currentTimeMillis() < blockAccountMap.get(account).getExpireTime();
    }

    public static BlockAccount getBlockAccount(String account) {
        return blockAccountMap.get(account);
    }

    public static void removeBlockAccount(String account) {
        blockAccountMap.remove(account);
    }
    
    public static void putBlockAccount(String account,BlockAccount bAccount){
    	blockAccountMap.put(account, bAccount);
    }
}
