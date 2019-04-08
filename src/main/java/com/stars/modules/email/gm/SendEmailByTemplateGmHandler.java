package com.stars.modules.email.gm;

import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;
import com.stars.services.mail.userdata.AllEmailPo;
import com.stars.services.mail.userdata.RoleEmailPo;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/4.
 */
public class SendEmailByTemplateGmHandler implements GmHandler {

    /**
     * format: sendemailbytemplate self[roleid|all|online],templateid,senderId,senderName,params
     * @param roleId
     * @param moduleMap
     * @param args
     * @throws Exception
     */
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        if (args.length < 4) {
            throw new IllegalArgumentException();
        }
        int templateId = 0;
        Long senderId = null;
        String senderName = null;
        String[] paramArray = null;
        templateId = Integer.parseInt(args[1]);
        if (!args[2].trim().equals("")) {
            senderId = Long.parseLong(args[2]);
        }
        if (!args[3].trim().equals("")) {
            senderName = args[3];
        }
        if (args.length >= 5) {
            paramArray = new String[args.length-4];
            for (int i = 4; i < args.length; i++) {
                paramArray[i-4] = args[i];
            }
        }
        RoleEmailPo roleEmailPo = null;
        AllEmailPo allEmailPo = null;
        switch (args[0]) {
            case "self":
                ServiceHelper.emailService().sendToSingle(roleId, templateId, senderId, senderName, null, paramArray);
                break;
            case "online":
                ServiceHelper.emailService().sendToOnline(templateId, senderId, senderName, paramArray);
                break;
            case "all":
                ServiceHelper.emailService().sendToAll(templateId, senderId, senderName, paramArray);
                break;
            default:
                ServiceHelper.emailService().sendToSingle(Long.parseLong(args[0]), templateId, senderId, senderName, null, paramArray);
                break;
        }
    }
}
