package com.stars.modules.tool.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropModule;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by wuyuxing on 2017/2/8.
 */
public class DropItemGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            int dropGroup = Integer.parseInt(args[0]);
            DropModule dropModule = (DropModule)moduleMap.get(MConst.Drop);
            Map<Integer, Integer> drops = dropModule.executeDrop(dropGroup, 1,true);
            ToolModule tm = (ToolModule)moduleMap.get(MConst.Tool);
            Map<Integer,Integer> map = tm.addAndSend(drops, EventType.USETOOL.getCode());
            tm.sendPacket(new ClientAward(map));

            PlayerUtil.send(roleId, new ClientText("执行成功,drop " + args[0]));
            LogUtil.info("执行成功,drop GM命令成功,dropGroup:"+args[0]+",掉落物品组:"+ StringUtil.makeString(map,'=',','));
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败,additem " + args[0]));
        }
    }
}
