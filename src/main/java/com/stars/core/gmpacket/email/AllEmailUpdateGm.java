package com.stars.core.gmpacket.email;

import com.stars.core.persist.DbRowDao;
import com.stars.core.gmpacket.WhiteListOpenOrCloseGm;
import com.stars.core.gmpacket.email.util.EmailUtils;
import com.stars.core.gmpacket.email.vo.AllEmailGmPo;
import com.stars.core.player.PlayerSystem;
import com.stars.core.db.DBUtil;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.mail.userdata.AllEmailPo;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全服邮件Gm 20015
 * Created by huwenjun on 2017/3/24.
 */
public class AllEmailUpdateGm extends GmPacketHandler {
    public static Map<Integer, AllEmailGmPo> allEmailVoMap = new ConcurrentHashMap<>();
    DbRowDao dbRowDao = new DbRowDao("allEmailSendGm");

    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        String taskIdStr = (String) args.get("taskId");
        String serverIdStr = (String) args.get("serverId");
        String statusStr = (String) args.get("status");
        long taskId = Long.parseLong(taskIdStr);
        int serverId = Integer.parseInt(serverIdStr);
        int status = Integer.parseInt(statusStr);
        String result = null;
        String sql = "select * from allemailgm where taskid=" + taskId + " and serverid=" + serverId;
        try {
            AllEmailGmPo allEmailGmPo = DBUtil.queryBean(DBUtil.DB_USER, AllEmailGmPo.class, sql);
            if (allEmailGmPo == null) {
                result = "serverId:" + serverIdStr + "服务器下不存在taskId：" + taskId + "的全服邮件任务";
            } else {
                if (allEmailGmPo.getStatus() == 1) {
                    result = "此全服邮件任务已经全服发放，请勿重复发放";
                }  else {
                    allEmailGmPo.setStatus(status);
                    dbRowDao.update(allEmailGmPo);

                    if (status == 1) {
                        AllEmailPo allEmailPo = EmailUtils.newAllEmail(allEmailGmPo, false);

                        dbRowDao.insert(allEmailPo);
                        checkAndSendCacheRole(allEmailPo);
                        result = "全服邮件已发放";
                    } else if (status == 2) {
                        result = "全服邮件已关闭";
                    } else {
                        result = "无效状态值";
                    }

                }
            }
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(result));
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            result = "操作异常";
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(result));
        } finally {
            dbRowDao.flush();
            return response.toString();
        }
    }

    /**
     * 检测并发送给缓存或在线的角色全服邮件
     *
     * @param allEmailPo
     */
    private void checkAndSendCacheRole(AllEmailPo allEmailPo) {
        Set<Long> roleIds = new HashSet<>();
        Set<String> roleIdSet = PlayerSystem.system().getActors().keySet();
        for (String roleId : roleIdSet) {
            roleIds.add(Long.parseLong(roleId));
        }
        /**
         * 移除白名单角色，防止二次接收
         */
        roleIds.removeAll(WhiteListOpenOrCloseGm.getWhiteRoleaccountSet());

        Map<Integer, Set<Long>> checkRoleStateMap = EmailUtils.checkRoleState(allEmailPo, roleIds);
        ServiceHelper.emailService().gmSendToAll(allEmailPo, checkRoleStateMap, false);
    }


}
