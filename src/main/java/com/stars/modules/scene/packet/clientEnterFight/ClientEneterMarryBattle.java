package com.stars.modules.scene.packet.clientEnterFight;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhanghaizhen on 2017/5/23.
 */
public class ClientEneterMarryBattle  extends ClientEnterDungeon {

    private int totalMarryBattleScore = 0; //情义副本过关积分
    private int failTime = 0;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        super.writeToBuffer(buff);
        buff.writeInt(totalMarryBattleScore);
//        buff.writeInt(failTime);

    }

    public void setTotalMarryBattleScore(int totalMarryBattleScore) {
        this.totalMarryBattleScore = totalMarryBattleScore;
    }

}
