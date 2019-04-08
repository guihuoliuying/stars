package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.Map;


/**
 * Created by zhanghaizhen on 2017/5/22.
 */
public class ClientMarryBattleScore extends PlayerPacket {
    //    int marryBattleScore;
    private Map<String, Integer> marryBattleScoreMap;

    public ClientMarryBattleScore() {
    }


    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_MARRY_BATTLE_SCORE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte((byte) (marryBattleScoreMap != null ? marryBattleScoreMap.size() : 0));
        if (marryBattleScoreMap == null)
            return;
        for (Map.Entry<String, Integer> entry : marryBattleScoreMap.entrySet()) {
            buff.writeString(entry.getKey());//uid
            buff.writeInt(entry.getValue());//score
        }
        LogUtil.info("结婚组队，给客户端发的包| {} ", marryBattleScoreMap);
    }

    public void setMarryBattleScoreMap(Map<String, Integer> marryBattleScoreMap) {
        this.marryBattleScoreMap = marryBattleScoreMap;
    }
}
