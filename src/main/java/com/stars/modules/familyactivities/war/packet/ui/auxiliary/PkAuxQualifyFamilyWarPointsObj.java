package com.stars.modules.familyactivities.war.packet.ui.auxiliary;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-05-27.
 */
public class PkAuxQualifyFamilyWarPointsObj {
    private int rank;
    private String familyId;
    private String familyName;
    private int victory;
    private int defeat;
    private long points;

    public PkAuxQualifyFamilyWarPointsObj(int rank, String familyId, String familyName, int victory, int defeat, long points) {
        this.rank = rank;
        this.familyId = familyId;
        this.familyName = familyName;
        this.victory = victory;
        this.defeat = defeat;
        this.points = points;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(rank);
        buff.writeString(familyId);
        buff.writeString(familyName);
        buff.writeInt(victory);
        buff.writeInt(defeat);
        buff.writeLong(points);
        LogUtil.info("familywar|跨服海选家族排名  rank:{},familyId:{},familyName:{},victory:{},defeat:{},points:{}",
                rank, familyId, familyName, victory, defeat, points);
    }

    public int getRank() {
        return rank;
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public int getVictory() {
        return victory;
    }

    public int getDefeat() {
        return defeat;
    }

    public long getPoints() {
        return points;
    }
}
