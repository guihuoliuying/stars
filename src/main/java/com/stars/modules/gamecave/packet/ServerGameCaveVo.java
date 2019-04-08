package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.gamecave.GameCaveModule;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaopeidian on 2017/1/16.
 */
public class ServerGameCaveVo extends PlayerPacket {
    private byte type;// 请求类型(子协议)

    @Override
    public short getType() {
        return GameCavePacketSet.S_GAMECAVE_VO;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.type = buff.readByte();
    }
    
    @Override
    public void execPacket(Player player) {
        GameCaveModule gameCaveModule = (GameCaveModule) module(MConst.GameCave);
        gameCaveModule.sendGameCaveVo(type);
    }
}
