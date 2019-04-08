package com.stars.modules.email.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;
import com.stars.services.mail.userdata.RoleEmailPo;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/2/27.
 */
public class SendItemGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        long otherRoleId = Long.parseLong(args[0]);
        Map<Integer, Integer> toolMap = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            MapUtil.add(toolMap, StringUtil.toMap(args[i], Integer.class, Integer.class, '=', ','));
        }
        RoleEmailPo roleEmailPo = new RoleEmailPo();
        roleEmailPo.setReceiverId(otherRoleId);
        roleEmailPo.setTitle("道具邮件");
        roleEmailPo.setText("");
        roleEmailPo.setAffixMap(toolMap);
        roleEmailPo.setEmailType(1);
        ServiceHelper.emailService().gmSend(roleEmailPo);
        PlayerUtil.send(roleId, new ClientText("OK"));
    }
}
