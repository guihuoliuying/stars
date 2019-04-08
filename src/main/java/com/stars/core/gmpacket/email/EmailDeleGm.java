package com.stars.core.gmpacket.email;

import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhaowenshuo on 2017/1/10.
 */
public class EmailDeleGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        long roleId = Long.parseLong((String) args.get("roleId"));
        List<String> mailIdList = (List<String>) args.get("mailId");
        List<Integer> emailIdList = toEmailIdList(mailIdList);
        try {
            List<Integer> failureEmailIdList = ServiceHelper.emailService().gmDele(roleId, emailIdList);
            List<String> res = new ArrayList<>();
            for (Integer emailId : failureEmailIdList) {
                res.add(emailId.toString() + "删除失败");
            }
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(res));
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, "");
        } finally {
            return response.toString();
        }
    }

    private List<Integer> toEmailIdList(List<String> mailIdList) {
        List<Integer> list = new ArrayList<>();
        for (String mailId : mailIdList) {
            list.add(Integer.parseInt(mailId));
        }
        return list;
    }
}
