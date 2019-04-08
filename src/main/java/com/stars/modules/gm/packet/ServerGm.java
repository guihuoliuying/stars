package com.stars.modules.gm.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmModule;
import com.stars.modules.gm.GmPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.startup.MainStartup;

/**
 * Created by zhaowenshuo on 2016/1/14.
 */
public class ServerGm extends PlayerPacket {

    private String commandLine;

    public String getCommandLine() {
        return commandLine;
    }

    @Override
    public void execPacket(Player player) {
        if (MainStartup.isOpenGameGm || commandLine.startsWith("sudo")) { // sudo只用作查看
            GmModule module = module(MConst.Gm);
            module.handle(this);
        }
    }

    @Override
    public short getType() {
        return GmPacketSet.S_GM;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.commandLine = buff.readString();
    }
}
