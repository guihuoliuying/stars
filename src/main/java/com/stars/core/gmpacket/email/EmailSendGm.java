package com.stars.core.gmpacket.email;

import com.stars.core.gmpacket.email.util.EmailUtils;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.services.mail.userdata.RoleEmailPo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/1/10.
 */
public class EmailSendGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        long roleId = Long.parseLong((String) args.get("roleId"));
        String title = (String) args.get("title");
        String content = (String) args.get("content");
        List<Map<String, String>> itemDict = (List<Map<String, String>>) args.get("itemDict");
        int emailType = Integer.parseInt((String) args.get("emailType"));
        RoleEmailPo roleEmailPo = new RoleEmailPo();
        roleEmailPo.setReceiverId(roleId);
        roleEmailPo.setTitle(title);
        roleEmailPo.setText(content);
        roleEmailPo.setAffixMap(EmailUtils.toToolMap(itemDict));
        roleEmailPo.setEmailType(emailType);
        try {
            ServiceHelper.emailService().gmSend(roleEmailPo);
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        } catch (Exception e) {
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }


}
