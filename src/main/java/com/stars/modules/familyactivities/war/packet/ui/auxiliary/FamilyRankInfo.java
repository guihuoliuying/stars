package com.stars.modules.familyactivities.war.packet.ui.auxiliary;

import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-06-29.
 */
public class FamilyRankInfo {
    private int rank;
    private String familyId;
    private String familyName;
    private String masterName;
    private int serverId;
    private long fightScore;
    private String type;

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(rank);
        buff.writeString(familyId);
        buff.writeString(familyName);
        buff.writeString(masterName);
        buff.writeString(MultiServerHelper.getServerName(serverId));
        buff.writeLong(fightScore);
        buff.writeString(type);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public long getFightScore() {
        return fightScore;
    }

    public void setFightScore(long fightScore) {
        this.fightScore = fightScore;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "FamilyRankInfo{" +
                "rank=" + rank +
                ", familyId='" + familyId + '\'' +
                ", familyName='" + familyName + '\'' +
                ", masterName='" + masterName + '\'' +
                ", serverId=" + serverId +
                ", fightScore=" + fightScore +
                ", type='" + type + '\'' +
                '}';
    }
}
