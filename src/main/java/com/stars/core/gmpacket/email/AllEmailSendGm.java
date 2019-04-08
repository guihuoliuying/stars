package com.stars.core.gmpacket.email;

import com.google.gson.Gson;
import com.stars.core.dao.DbRowDao;
import com.stars.core.gmpacket.WhiteListOpenOrCloseGm;
import com.stars.core.gmpacket.email.util.EmailUtils;
import com.stars.core.gmpacket.email.vo.AllEmailGmPo;
import com.stars.db.DBUtil;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.mail.EmailServiceActor;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 全服邮件Gm,此法送仅发送到白名单，真正的全服邮件发送为AllEmailUpdateGm
 * opType=20014
 * Created by huwenjun on 2017/3/24.
 */
public class AllEmailSendGm extends GmPacketHandler {
    DbRowDao dbRowDao = new DbRowDao("allEmailSendGm");

    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        try {
            AllEmailGmPo allEmailGmPo = buildAllEmailGmPo(args);
            /**
             * 判断是否已经存在此全服邮件任务
             */
            String sql = "select * from allemailgm where taskid=" + allEmailGmPo.getTaskId() + " and serverid=" + allEmailGmPo.getServerId();
            AllEmailGmPo existedAllEmailGmPo = DBUtil.queryBean(DBUtil.DB_USER, AllEmailGmPo.class, sql);
            String result;
            if (existedAllEmailGmPo != null) {
                result = "serverId:" + allEmailGmPo.getServerId() + "服务器下已经存在taskId：" + allEmailGmPo.getTaskId() + "的全服邮件任务，请勿重复操作";
                response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(result));
            } else {
                dbRowDao.insert(allEmailGmPo);
                /**
                 * 获取白名单并初始化账号下角色信息
                 */
                Set<Long> _roleIds = WhiteListOpenOrCloseGm.handleRoleInfoByAccounts();
                Map<Integer, Set<Long>> checkRoleStateMap = EmailUtils.checkRoleState(EmailUtils.newAllEmail(allEmailGmPo, true), _roleIds);
                ServiceHelper.emailService().gmSendToWhite(allEmailGmPo, checkRoleStateMap);
                response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson("操作完成"));
            }

        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson("操作异常"));
        } finally {
            dbRowDao.flush();
            return response.toString();
        }
    }


    /**
     * 将全服邮件组装成对象
     *
     * @param args
     * @return
     */
    private AllEmailGmPo buildAllEmailGmPo(HashMap args) {
        AllEmailGmPo allEmailGmPo = new AllEmailGmPo();
        String title = (String) args.get("title");
        String taskIdStr = (String) args.get("taskId");
        String serverIdStr = (String) args.get("serverId");
        String content = (String) args.get("content");
        List<Map<String, String>> itemDict = (List<Map<String, String>>) args.get("itemDict");
        Map<Integer, Integer> toolMap = EmailUtils.toToolMap(itemDict);
        String coolTimeStr = (String) args.get("coolTime");
        String expireTimeStr = (String) args.get("expireTime");
        String channelIdsStr = (String) args.get("channelIds");
        int coolTime = 0;
        if (coolTimeStr != null) {
            coolTime = Integer.parseInt(coolTimeStr);

        }
        int serverId = 0;
        if (serverIdStr != null) {
            serverId = Integer.parseInt(serverIdStr);
        }
        long taskId = 0;
        if (taskIdStr != null) {
            taskId = Long.parseLong(taskIdStr);
        }
        allEmailGmPo.setCoolTime(coolTime);
        int expireTime = 0;
        if (expireTimeStr != null) {
            expireTime = Integer.parseInt(expireTimeStr);
        }
        allEmailGmPo.setExpireTime(expireTime);
        List<Map<String, String>> conditionList = (List<Map<String, String>>) args.get("conditionList");
        AtomicInteger allEmailId = EmailServiceActor.getAllEmailIdGenerator();
        allEmailGmPo.setAllEmailGmId(allEmailId.addAndGet(1));
        allEmailGmPo.setConditionList(new Gson().toJson(conditionList));
        allEmailGmPo.setTaskId(taskId);
        allEmailGmPo.setServerId(serverId);
        allEmailGmPo.setSenderName("系统");
        allEmailGmPo.setItemDict(new Gson().toJson(toolMap));
        allEmailGmPo.setTitle(title);
        allEmailGmPo.setText(content);
        allEmailGmPo.setContent(content);
        allEmailGmPo.setStatus(0);
        allEmailGmPo.setItemDict(new Gson().toJson(itemDict));
        allEmailGmPo.setConditionList(new Gson().toJson(conditionList));
        allEmailGmPo.setChannelIds(channelIdsStr);
        return allEmailGmPo;
    }
}
