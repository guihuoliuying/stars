package com.stars.modules.chat.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.chat.ChatModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.util.DateUtil;

import java.util.Map;

/**
 * Created by zhangjiahua on 2016/2/25.
 */
public class ChatBanGmHandler implements GmHandler {


    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) {
        try {
            /**
             * 	chatban 1 角色普通禁言5分钟
             *	chatban 2 角色静默禁言5分钟
             *	chatban 0 解除所有禁言
             */
            int argsType = Integer.parseInt(args[0]);
            ChatModule chatModule = (ChatModule) moduleMap.get(MConst.Chat);
            long now = System.currentTimeMillis();
            switch (argsType){
                case 0:
                    chatModule.changRoleChatBan((byte)0,(byte)0,0l);
                    break;
                case 1:
                    chatModule.changRoleChatBan((byte)1,(byte)1,now + 5* DateUtil.MINUTE);
                    break;
                case 2:
                    chatModule.changRoleChatBan((byte)2,(byte)1,now + 5* DateUtil.MINUTE);
                    break;
                default:
                    PlayerUtil.send(roleId, new ClientText("执行失败,无此GM命令 chatban " + args[0]));
            }

            PlayerUtil.send(roleId, new ClientText("执行成功,chatban " + args[0]));
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败,chatban " + args[0]));
        }
    }
}
