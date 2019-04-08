package com.stars.modules.teampvpgame.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.teampvpgame.TeamPVPGamePacketSet;
import com.stars.multiserver.teamPVPGame.TPGTeam;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/12/24.
 */
public class ClientTPGScoreRank extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte SCORE_RANK = 1;// 积分赛排行榜
    public static final byte SCORE_RANKING_BYTEAMID = 2;// 根据teamid获得排名

    /* 参数 */
    private List<TPGTeam> scoreRank;// 积分赛排行榜
    private int myRanking;// 我的排名
    private Map<Integer, Integer> rankingMap;// <teamid, 排名>

    public ClientTPGScoreRank() {
    }

    public ClientTPGScoreRank(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return TeamPVPGamePacketSet.C_TPG_SCORERANK;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case SCORE_RANK:
                byte size = (byte) (scoreRank == null ? 0 : scoreRank.size());
                buff.writeByte(size);
                if (size == 0)
                    return;
                int ranking = 1;
                for (TPGTeam tpgTeam : scoreRank) {
                    buff.writeInt(ranking);// 排名
                    buff.writeInt(tpgTeam.getScore());// 积分
                    tpgTeam.writeToBuff(buff);
                    ranking++;
                }
                buff.writeInt(myRanking);// 我的排名
                break;
            case SCORE_RANKING_BYTEAMID:
                size = (byte) (rankingMap == null ? 0 : rankingMap.size());
                buff.writeByte(size);
                if (size == 0)
                    return;
                for (Map.Entry<Integer, Integer> entry : rankingMap.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
                break;
        }
    }

    public void setScoreRank(List<TPGTeam> scoreRank) {
        this.scoreRank = scoreRank;
    }

    public void setMyRank(int myRanking) {
        this.myRanking = myRanking;
    }

    public void setRankingMap(Map<Integer, Integer> rankingMap) {
        this.rankingMap = rankingMap;
    }
}
