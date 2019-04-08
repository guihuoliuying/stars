package com.stars.modules.chat.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.services.ServiceHelper;

import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/1.
 */
public class AnnounceGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            /**
             * announce message 走马灯播放10次
             */
            String message = args[0];
            for(int i = 0; i <= 10; i ++) {
                ServiceHelper.chatService().announce(message);//发送全服公告
            }
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败,announce " + args[0]));
        }
    }
}
