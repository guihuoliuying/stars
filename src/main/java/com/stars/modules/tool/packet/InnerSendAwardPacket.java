package com.stars.modules.tool.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.tool.ToolModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/12/27.
 */
public class InnerSendAwardPacket extends PlayerPacket {

    private short eventType;
    private Map<Integer, Integer> toolMap;

    public InnerSendAwardPacket(short eventType, Map<Integer, Integer> toolMap) {
        this.eventType = eventType;
        this.toolMap = toolMap;
    }

    @Override
    public void execPacket(Player player) {
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(toolMap, eventType);
    }

    @Override
    public short getType() {
        return 0;
    }
}
