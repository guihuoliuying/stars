package com.stars.core.gmpacket;

import com.stars.AccountRow;
import com.stars.db.DBUtil;
import com.stars.modules.demologin.userdata.BlockAccount;
import com.stars.modules.family.summary.FamilySummaryComponent;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.userdata.ForbiddenChater;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;
import com.stars.startup.MainStartup;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util._HashMap;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/1/6.
 */
public class QueryPlayerBasicInfoGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        long roleId = 0;
        String roleName = "";
        String account = "";
        if(args.containsKey("roleId")){
            roleId = Long.valueOf((String) args.get("roleId"));
        }
        if(args.containsKey("roleName")){
            roleName = (String) args.get("roleName");
        }
        if(args.containsKey("account")){
            account = (String) args.get("account");
        }
        if(roleId == 0 && roleName.equals("") && account.equals("")){
            GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(getResultMap(null)));
            return response.toString();
        }
        String sql = getQuerySql(roleId,roleName,account);
        List<_HashMap> list = new ArrayList<>();
        try {
            list = DBUtil.queryList(DBUtil.DB_USER, _HashMap.class, sql);
        }catch (SQLException e){
            LogUtil.error(e.getMessage(),e);
        }
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, list.size(), resultToJson(getResultMap(list)));
        return response.toString();
    }

    private String getQuerySql(long roleId,String roleName,String account){
        StringBuilder sb = new StringBuilder();
        sb.append("select ar.account, ar.time, a.channel, a.viplevel, r.* from role r,accountrole ar,account a where");
        if(roleId!=0){
            sb.append(" r.roleid = '").append(roleId).append("' and");
        }
        if(StringUtil.isNotEmpty(roleName)){
            sb.append(" r.name = '").append(roleName).append("' and");
        }
        if(StringUtil.isNotEmpty(account)){
            sb.append(" ar.account = '").append(account).append("' and");
        }
        sb.append(" r.roleid = ar.roleid and a.name = ar.account order by r.level desc limit 10");
        return sb.toString();
    }

    private List<Map> getResultMap(List<_HashMap> list){
        List<Map> resultList = new ArrayList<>();
        if(list == null) return resultList;
        Map<String,Object> map;
        int onlineStatue;
        long roleId;
        AccountRow accountRow;
        String account;
        for(_HashMap vo : list) {
            if(vo == null) continue;
            map = new HashMap<>();
            account = vo.get("ar.account").toString();
            roleId = Long.parseLong(vo.get("r.roleid").toString());
            Summary summary = ServiceHelper.summaryService().getSummary(roleId);
            accountRow = MainStartup.accountMap == null ? null : MainStartup.accountMap.get(account);
            if(accountRow != null && roleId == accountRow.getCurrentRoleId()){
                onlineStatue = 1;
            }else{
                onlineStatue = 0;
            }
            map.put("account",vo.get("ar.account"));
            map.put("roleId",vo.get("r.roleid"));
            map.put("roleName",vo.get("r.name"));
            map.put("level",vo.get("r.level"));
            map.put("exp",vo.get("r.exp"));
            map.put("coin",vo.get("r.gold"));
            map.put("giftCoin", vo.get("r.bandgold"));
            map.put("money",vo.get("r.money"));
            map.put("vipLevel",vo.get("a.viplevel"));
            map.put("channel",vo.get("a.channel").toString().split("@")[0]);
            map.put("roleCreateTime", ((Timestamp) vo.get("ar.time")).getTime() / 1000);
            // 封号
            BlockAccount blockAccount = BlockAccountGm.getBlockAccount(account);
            map.put("blockStatus", BlockAccountGm.isAccountBlock(account) ? 1 : 0);
            map.put("blockTime", blockAccount == null ? 0 : blockAccount.getExpireTime() / 1000);
            // 禁言
            ForbiddenChater forbiddenChater = ServiceHelper.chatService().getForbiddenChater(roleId);
            int talkStatus = forbiddenChater == null ? 0 :
                    (System.currentTimeMillis() >= forbiddenChater.getExpireTime() ? 0 : 1);
            map.put("talkStatus", talkStatus);
            map.put("banTime", forbiddenChater == null ? 0 : forbiddenChater.getExpireTime() / 1000);
            map.put("onlineStatus",onlineStatue);

            // 家族id
            FamilySummaryComponent familySummaryComponent = summary.getComponent(SummaryConst.C_FAMILY);
            map.put("faction_id", familySummaryComponent.getFamilyId());
            map.put("faction_name", familySummaryComponent.getFamilyName());
            resultList.add(map);
        }
        return resultList;
    }

//    private String results(List<_HashMap> list){
//        StringBuilder builder = new StringBuilder();
//        builder.append("{");
//        int onlineStatue;
//        long roleId;
//        AccountRow accountRow;
//        String account;
//        for(_HashMap vo : list) {
//            if(vo == null) continue;
//            account = vo.get("ar.account").toString();
//            roleId = (long) vo.get("r.roleid");
//            accountRow = MainStartup.accountMap.get(account);
//            if(accountRow != null && roleId == accountRow.getCurrentRoleId()){
//                onlineStatue = 1;
//            }else{
//                onlineStatue = 0;
//            }
//
//            builder.append("\"account\":")
//                    .append(vo.get("ar.account"))
//                    .append(",")
//                    .append("\"roleId\":")
//                    .append(vo.get("r.roleid"))
//                    .append(",")
//                    .append("\"roleName\":")
//                    .append(vo.get("r.name"))
//                    .append(",")
//                    .append("\"level\":")
//                    .append(vo.get("r.level"))
//                    .append(",")
//                    .append("\"exp\":")
//                    .append(vo.get("r.exp"))
//                    .append(",")
//                    .append("\"coin\":")
//                    .append(vo.get("r.gold"))
//                    .append(",")
//                    .append("\"money\":")
//                    .append(vo.get("r.money"))
//                    .append(",")
//                    .append("\"channel\":")
//                    .append(vo.get("a.channel").toString().split("@")[0])
//                    .append(",")
//                    .append("\"blockStatus\":")
//                    .append("0")
//                    .append(",")
//                    .append("\"talkStatus\":")
//                    .append("0")
//                    .append(",")
//                    .append("\"blockTime\":")
//                    .append("0")
//                    .append(",")
//                    .append("\"banTime\":")
//                    .append("0")
//                    .append(",")
//                    .append("\"onlineStatus\":")
//                    .append(onlineStatue);
//        }
//        builder.append("}");
//        return builder.toString();
//    }

}
