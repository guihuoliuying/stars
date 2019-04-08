package com.stars.modules.gamecave.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.gamecave.GameCavePacketSet;
import com.stars.modules.gamecave.prodata.GameCaveQuestionVo;
import com.stars.modules.gamecave.prodata.GameCaveShootOldVo;
import com.stars.modules.gamecave.prodata.GameCaveVo;
import com.stars.modules.gamecave.tinygame.TinyGameBase;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by gaopeidian on 2016/6/21.
 */
public class ClientGameCaveVo extends PlayerPacket {
    private byte type;// 下发类型

    private Map<Integer, GameCaveVo> gameCaveVoMap;
    private Map<Integer, GameCaveQuestionVo> questionVoMap;
    private Map<Integer, GameCaveShootOldVo> shootVoMap;

    public ClientGameCaveVo() {
    }

    public ClientGameCaveVo(byte type) {
        this.type = type;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GameCavePacketSet.C_GAMECAVE_VO;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(type);
        switch (type) {
            case 0:
                writeGameCave(buff);
                break;
            case TinyGameBase.AnswerType:
                writeGameCaveQuestion(buff);
                break;
            case TinyGameBase.ArcherType:
                writeGameCaveShootOld(buff);
                break;
            default:
                break;
        }
    }

    public void writeGameCave(com.stars.network.server.buffer.NewByteBuffer buff){
        short size = (short) (gameCaveVoMap == null ? 0 : gameCaveVoMap.size());
        buff.writeShort(size);
        if (size != 0) {
            for (GameCaveVo vo : gameCaveVoMap.values()) {
            	vo.writeToBuff(buff);
            }
        }
    }
    
    public void writeGameCaveQuestion(com.stars.network.server.buffer.NewByteBuffer buff){
    	short size = (short) (questionVoMap == null ? 0 : questionVoMap.size());
        buff.writeShort(size);
        if (size != 0) {
            for (GameCaveQuestionVo vo : questionVoMap.values()) {
            	vo.writeToBuff(buff);
            }
        }
    }

    public void writeGameCaveShootOld(NewByteBuffer buff){
    	short size = (short) (shootVoMap == null ? 0 : shootVoMap.size());
        buff.writeShort(size);
        if (size != 0) {
            for (GameCaveShootOldVo vo : shootVoMap.values()) {
            	vo.writeToBuff(buff);
            }
        }
    }
 
    public void setGameCaveVoMap(Map<Integer, GameCaveVo> value) {
    	gameCaveVoMap = value;
    }
    
    public void setQuestionVoMap(Map<Integer, GameCaveQuestionVo> value) {
    	questionVoMap = value;
    }
    
    public void setShootVoMap(Map<Integer, GameCaveShootOldVo> value) {
    	shootVoMap = value;
    }
}
