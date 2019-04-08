package com.stars.core.gmpacket;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.demologin.message.AutoSaveMsg;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.services.ServiceHelper;
import com.stars.core.actor.Actor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 运维GM接口
 * 保存数据
 * Created by liuyuheng on 2016/12/10.
 */
public class SaveDataGm extends GmPacketHandler {
    public static byte status = 0;
    @Override
    public String handle(HashMap args) {
        List<Integer> resultList = new LinkedList<>();
        // 个人业务保存
        for (Actor actor : PlayerSystem.system().getActors().values()) {
            if (actor instanceof Player) {
                actor.tell(new AutoSaveMsg(null), actor);
                resultList.add(0);
            }
        }
        // 公共业务保存
        resultList.add(ServiceHelper.executeSave()?0:-1);
        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, resultList.size(), resultToJson(resultList.contains(-1)?-1:0));
        return response.toString();

    }
}
